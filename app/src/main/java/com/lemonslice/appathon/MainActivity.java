package com.lemonslice.appathon;

import android.app.Activity;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {

    private Camera camera = null;
    private CameraPreview cameraPreview;
    private ScrollView cameraLayout;
    private FrameLayout cameraContainer;
    public String currEmoji = "sd";
    static int hello = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = CameraPreview.openFrontCamera();
        cameraPreview = new CameraPreview(this, camera);

        //cameraLayout = (ScrollView) findViewById(R.id.camera_layout);
        cameraContainer = (FrameLayout) findViewById(R.id.camera_container);
        TextView tv = (TextView) findViewById(R.id.yomoji);
        tv.setText("\uD83D\uDE0E");
        setCurrEmoji((String)tv.getText());
        cameraContainer.addView(cameraPreview);


        ImageView u1 = (ImageView) findViewById(R.id.u1);
        ImageView u2 = (ImageView) findViewById(R.id.u2);
        ImageView u3 = (ImageView) findViewById(R.id.u3);

        u1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BTLE", "Send " + currEmoji + " to U1");
            }
        });

        u2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BTLE", "Send " + currEmoji + " to U2");
            }
        });

        u3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("BTLE", "Send " + currEmoji + " to U3");
            }
        });

        // view tree observer lets us set the size of the camera preview view at runtime
        ViewTreeObserver viewTreeObserver = cameraPreview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean changedLayout = false;
            @Override
            public void onGlobalLayout() {
                if (cameraLayout != null && !changedLayout) {
                    changedLayout = true;
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    float density = Resources.getSystem().getDisplayMetrics().density;
                    float dpHeight = size.height / density;
                    float dpWidth = size.width / density;
                    Log.d("EMOJI", String.format("dp (%f,%f)", dpWidth, dpHeight));

                    // n.b it may seem like I'm using the wrong width vs height values, this is because the camera view is in landscape
                    float previewWidth = cameraPreview.getWidth();
                    float scale = previewWidth / dpHeight;
                    Log.d("EMOJI", String.format("ph: %s scale: %s", previewWidth, Float.toString(scale)));

                    ScrollView.LayoutParams slp = new ScrollView.LayoutParams((int) previewWidth, 500);
                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams((int) previewWidth, 500);
                    cameraPreview.setLayoutParams(flp);
                    //cameraLayout.setLayoutParams(slp);
                    //cameraLayout.scrollTo(0, (int) ((float)cameraLayout.getBottom()/1.5));

                    cameraPreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hello = 1;

                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getCurrEmoji() {

        return currEmoji;
    }

    public void setCurrEmoji(String newEmoji) {
        currEmoji = newEmoji;

    }
}
