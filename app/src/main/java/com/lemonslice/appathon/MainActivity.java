package com.lemonslice.appathon;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;

public class MainActivity extends Activity {

    private Camera camera = null;
    private CameraPreview cameraPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = openFrontCamera();
        cameraPreview = new CameraPreview(this, camera);

        FrameLayout cameraLayout = (FrameLayout) findViewById(R.id.camera_layout);
        cameraLayout.addView(cameraPreview);
    }

    private Camera openFrontCamera() {
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    c = Camera.open(i);
                    c.setDisplayOrientation(270);
                    Camera.Parameters p = c.getParameters();
                    Camera.Size previewSize = p.getPreferredPreviewSizeForVideo();
                    Log.d("EMOJI", "" + previewSize.width + " " + previewSize.height);
                    p.setPreviewSize(previewSize.width - 100, previewSize.height);
                } catch (RuntimeException e) {
                    Log.e("EMOJI", "Front camera did not open");
                }
                break;
            }
        }
        return c;
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
}
