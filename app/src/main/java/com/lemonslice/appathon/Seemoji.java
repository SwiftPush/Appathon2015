package com.lemonslice.appathon;

import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Camera;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Sam on 07/03/2015.
 */
public class Seemoji extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private CameraPreview cameraPreview;

    private NoScrollView cameraLayout;

    private FrameLayout containerView;
    private boolean isCameraMode = false;
    private boolean caps = false;

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        String codex;
        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE :
                int delcount = 1;
                String tmp = (String)ic.getTextBeforeCursor(2,0);
                if (tmp.length() >0 ) {
                    if (tmp.charAt(0) == '\uD83D')
                        delcount++;
                }
                ic.deleteSurroundingText(delcount, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                containerView.removeAllViews();
                if (isCameraMode) {
                    containerView.addView(kv);
                } else {
                    containerView.addView(cameraLayout);
                }
                isCameraMode = !isCameraMode;

                /*caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();*/
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case 1337 :
                //smile
                codex = "\uD83D\uDE03";
                ic.commitText(codex,1);
                break;
            case 1338 :
                //grin
                codex = "\uD83D\uDE04";
                ic.commitText(codex,1);
                break;
            case 1339 :
                //tongue
                codex = "\uD83D\uDE1B";
                ic.commitText(codex,1);
                break;
            case 1340 :
                //wink-smile
                codex = "\uD83D\uDE09";
                ic.commitText(codex,1);
                break;
            case 1341 :
                //wink-grin
                codex = "\uD83D\uDE06";
                ic.commitText(codex,1);
                break;
            case 1342 :
                //wink-tongue
                codex = "\uD83D\uDE1C";
                ic.commitText(codex,1);
                break;
            case 1343 :
                //sunglasses
                codex = "\uD83D\uDE0E";
                ic.commitText(codex,1);
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code),1);
        }
    }

    @Override
    public void onPress(int primaryCode) {
        int keyCodeSpace = 32;
        if((primaryCode == Keyboard.KEYCODE_DELETE)
                || (primaryCode==Keyboard.KEYCODE_DONE)
                || (primaryCode==Keyboard.KEYCODE_SHIFT)
                || (primaryCode==keyCodeSpace)
                ) {
            kv.setPreviewEnabled(false);
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        kv.setPreviewEnabled(true);
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }
    @Override
    public View onCreateInputView() {
        LayoutInflater layoutInflater = getLayoutInflater();

        containerView = new FrameLayout(this);

        cameraPreview = new CameraPreview(this, openFrontCamera());
        cameraLayout = (NoScrollView) layoutInflater.inflate(R.layout.camera_keyboard, null);
        FrameLayout cameraContainer = (FrameLayout) cameraLayout.findViewById(R.id.camera_container);
        cameraContainer.addView(cameraPreview);

        kv = (KeyboardView) layoutInflater.inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        final int keyboardHeight = kv.getHeight();

        ViewTreeObserver viewTreeObserver = cameraPreview.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            boolean changedLayout = false;
            @Override
            public void onGlobalLayout() {
                if (!changedLayout) {
                    changedLayout = true;
                    Camera.Size size = cameraPreview.camera.getParameters().getPreviewSize();
                    float density = Resources.getSystem().getDisplayMetrics().density;
                    float dpHeight = size.height / density;
                    float dpWidth = size.width / density;
                    Log.d("EMOJI", String.format("dp (%f,%f)", dpWidth, dpHeight));

                    // n.b it may seem like I'm using the wrong width vs height values, this is because the camera view is in landscape
                    float previewWidth = cameraPreview.getWidth();
                    float scale = previewWidth / dpHeight;
                    Log.d("EMOJI", String.format("ph: %s scale: %s", previewWidth, Float.toString(scale)));

                    ScrollView.LayoutParams slp = new ScrollView.LayoutParams((int) previewWidth, 1200);
                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams((int) previewWidth, (int) (dpWidth * scale));
                    cameraPreview.setLayoutParams(flp);
                    cameraLayout.setLayoutParams(slp);
                    cameraLayout.scrollTo(0, cameraLayout.getBottom() / 2);
                    final TextView button = (TextView) cameraLayout.findViewById(R.id.backToKeyboardtxt);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            containerView.removeAllViews();
                            if (isCameraMode) {
                                containerView.addView(kv);
                            } else {
                                containerView.addView(cameraLayout);
                            }
                            isCameraMode = !isCameraMode;
                        }
                    });
                }
            }
        });


        containerView.addView(kv);



        return containerView;
    }

    public void switchKeyboardMode() {
        containerView.removeAllViews();
        if (isCameraMode) {
            containerView.addView(kv);
        } else {
            containerView.addView(cameraLayout);
        }
        isCameraMode = !isCameraMode;

    }

    private Camera openFrontCamera() {
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    c = Camera.open(i);
                    c.setDisplayOrientation(90);
                } catch (RuntimeException e) {
                    Log.e("EMOJI", "Front camera did not open");
                }
                break;
            }
        }
        return c;
    }

}
