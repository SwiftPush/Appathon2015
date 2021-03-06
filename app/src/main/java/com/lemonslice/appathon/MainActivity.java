package com.lemonslice.appathon;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.HashSet;

public class MainActivity extends Activity {

    private Camera camera = null;
    private CameraPreview cameraPreview;
    private ScrollView cameraLayout;
    private FrameLayout cameraContainer;
    public String currEmoji = "sd";
    static int hello = 0;

    private String SERVICE_NAME = "Yomoji";
    private String SERVICE_TYPE = "_http._tcp";
    private NsdManager mNsdManager;

    HashSet<InetAddress> IPAddresses;

    NsdManager.RegistrationListener registrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d("NSD", "fail reg");
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d("NSD", "fail unreg");
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            SERVICE_NAME = serviceInfo.getServiceName();
            Log.d("NSD", "Registered name: " + SERVICE_NAME);
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.d("NSD", "Unregistered name: " + SERVICE_NAME);
        }
    };

    NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {
        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.d("NSD", String.format("disc start fail for %s err %d", serviceType, errorCode));
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.d("NSD", "stop fail");
        }

        @Override
        public void onDiscoveryStarted(String serviceType) {
            Log.d("NSD", "disc start " + serviceType);
        }

        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.d("NSD", "disc stop " + serviceType);
        }

        @Override
        public void onServiceFound(NsdServiceInfo serviceInfo) {
            String TAG = "NSD";

            if (serviceInfo.getServiceName().equals(SERVICE_NAME)) {
                Log.d(TAG, "lol found myself ;)");
            } else {
                Log.d(TAG, "SERVICE FOUND BITCH: " + serviceInfo.toString());
                Log.d(TAG, "Service discovery success : " + serviceInfo);
                Log.d(TAG, "Host = " + serviceInfo.getServiceName());
                mNsdManager.resolveService(serviceInfo, resolveListener);
            }

        }

        @Override
        public void onServiceLost(NsdServiceInfo serviceInfo) {
            Log.d("NSD", "Service lost");
        }
    };

    NsdManager.ResolveListener resolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d("NSD", "Resolve failed");
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            Log.d("NSD", "Resolve worked???!!");

            InetAddress hostAddress = serviceInfo.getHost();
            int hostPort = serviceInfo.getPort();

            IPAddresses.add(hostAddress);
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    updateConnections();
                }
            });

            Log.d("NSD", "addr: " + hostAddress.toString() + " port: " + hostPort);
        }
    };

    ChatConnection chatConnection;
    private Handler updateHandler;

    LinearLayout connectionsLayout;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IPAddresses = new HashSet<>();

        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        registerService(9000);
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);

        updateHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String emoji = msg.getData().getString("msg");
                Log.d("NSD", "got emoji " + emoji);
                receiveEmoji(emoji);
            }
        };

        chatConnection = new ChatConnection(updateHandler);

        setContentView(R.layout.activity_main);

        camera = CameraPreview.openFrontCamera();
        cameraPreview = new CameraPreview(this, camera);

        //cameraLayout = (ScrollView) findViewById(R.id.camera_layout);
        cameraContainer = (FrameLayout) findViewById(R.id.camera_container);
        final TextView tv = (TextView) findViewById(R.id.yomoji);
        tv.setText("\uD83D\uDE0E");
        setCurrEmoji((String)tv.getText());
        cameraContainer.addView(cameraPreview);

        connectionsLayout = (LinearLayout) findViewById(R.id.connection_bar);

        ImageView u1 = (ImageView) findViewById(R.id.u1);
        ImageView u2 = (ImageView) findViewById(R.id.u2);
        ImageView u3 = (ImageView) findViewById(R.id.u3);
        ImageView u4 = (ImageView) findViewById(R.id.u4);

        View.OnClickListener startConnection = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CLICK", "OH HEYYYY");
                if (chatConnection == null || IPAddresses == null || IPAddresses.size() == 0)
                    return;

                InetAddress addr = IPAddresses.iterator().next();
                Log.d("CLICK", addr.toString());
                TextView tv = (TextView) findViewById(R.id.yomoji);
                final String emoji = (String) tv.getText();
                Log.d("CLICK", "SENT MESSAGE WOOP");
//                chatConnection.sendMessage("THIS IS A TEST :D");
                chatConnection.sendMessage(addr.toString() + "," + emoji);

            }
        };

        u1.setOnClickListener(startConnection);
        u2.setOnClickListener(startConnection);
        u3.setOnClickListener(startConnection);
        u4.setOnClickListener(startConnection);

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

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                tv.setText(Seemoji.stringToEmoji(CameraPreview.currEmoji));
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);
    }

    public void receiveEmoji(String text) {
        String[] split = text.split(",");
        if (!IPAddresses.isEmpty()) {
            if (split[0].equals(IPAddresses.iterator().next().toString())) {
                Log.d("CLICK", "IGNORING THING SENT FROM ME");
                return;
            }
        }
        Toast.makeText(this, "RECEIVED: " + split[1], Toast.LENGTH_LONG).show();
    }

    private void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);
        serviceInfo.setPort(port);

        mNsdManager.registerService(serviceInfo,
                NsdManager.PROTOCOL_DNS_SD,
                registrationListener);
    }

    public void updateConnections() {
        if (!isConnected) {
            isConnected = true;
            chatConnection.connectToServer(IPAddresses.iterator().next(), 9000);
        }
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
