package com.lemonslice.appathon;

import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Camera;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Created by Sam on 07/03/2015.
 */
public class Seemoji extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private FrameLayout outerLayout;
    private FrameLayout overLayout;
    private NoScrollView cameraLayout;
    private CameraPreview cameraPreview;

    private FrameLayout containerView;
    private boolean isCameraMode = false;
    private boolean caps = false;
    private String prevCodex = "";

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
                    containerView.addView(outerLayout);
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

        cameraPreview = new CameraPreview(this, CameraPreview.openFrontCamera());
        outerLayout = (FrameLayout) layoutInflater.inflate(R.layout.camera_keyboard, null);

        cameraLayout = (NoScrollView) outerLayout.findViewById(R.id.camera_layout);
        overLayout = (FrameLayout) outerLayout.findViewById(R.id.camera_overlay);
        FrameLayout cameraContainer = (FrameLayout) cameraLayout.findViewById(R.id.camera_container);
        cameraContainer.addView(cameraPreview);

        kv = (KeyboardView) layoutInflater.inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);

        // set up the emoji delete button
        ImageButton emojiDelete = (ImageButton) overLayout.findViewById(R.id.emoji_delete_key);
        emojiDelete.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKey(Keyboard.KEYCODE_DELETE, null);
            }
        });


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

                    Resources r = getResources();
                    final int targetDP = 380;
                    final int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, targetDP, r.getDisplayMetrics());
                    Log.d("tsneirao", String.valueOf(px));
                    ScrollView.LayoutParams slp = new ScrollView.LayoutParams((int) previewWidth, px);
                    FrameLayout.LayoutParams overlayoutParams = new FrameLayout.LayoutParams((int) previewWidth, px);
                    FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams((int) previewWidth, (int) (dpWidth * scale));
                    cameraPreview.setLayoutParams(flp);
                    cameraLayout.setLayoutParams(slp);
                    overLayout.setLayoutParams(overlayoutParams);
                    cameraLayout.scrollTo(0, (int) ((float)cameraLayout.getBottom()  / 1.5));

                    final ImageButton imgButton = (ImageButton) outerLayout.findViewById(R.id.backToKeyBoardImg);
                    imgButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            containerView.removeAllViews();
                            if (isCameraMode) {
                                containerView.addView(kv);
                            } else {
                                containerView.addView(outerLayout);
                            }
                            isCameraMode = !isCameraMode;
                        }
                    });

                    final TextView chooseEmoji = (TextView) outerLayout.findViewById(R.id.emojiDisplayTxt);
                    chooseEmoji.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Log.d("James","Emoji clicked within keyboard");
                            InputConnection ic = getCurrentInputConnection();
                            String codex;
                            codex = (String)chooseEmoji.getText();
                            Log.d("SAM1",codex);
                            //codex = stringToEmoji(codex);
                            Log.d("SAM2", codex);
                            ic.commitText(codex,1);
                            //chooseEmoji.setText("Emoji");
                        }
                    });
                }
            }
        });

        containerView.addView(kv);

        final Handler handler = new Handler();
        final TextView chooseEmoji = (TextView) outerLayout.findViewById(R.id.emojiDisplayTxt);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                chooseEmoji.setText(stringToEmoji(CameraPreview.currEmoji));
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 100);

//        final Button buttonSmile = (Button) outerLayout.findViewById(R.id.buttonSmile);
//        buttonSmile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraPreview.smileButton();
//            }
//        });
//        final Button buttonWink = (Button) outerLayout.findViewById(R.id.buttonWink);
//        buttonWink.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraPreview.winkButton();
//            }
//        });
//        final Button buttonTounge = (Button) outerLayout.findViewById(R.id.buttonTounge);
//        buttonTounge.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraPreview.toungeButton();
//            }
//        });
//        final Button buttonMouth = (Button) outerLayout.findViewById(R.id.buttonMouth);
//        buttonMouth.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraPreview.mouthButton();
//            }
//        });
//        final Button buttonSun = (Button) outerLayout.findViewById(R.id.buttonSun);
//        buttonSun.setOnClickListener( new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraPreview.sunButton();
//            }
//        });

        return containerView;
    }

    public static String stringToEmoji(String inp) {
        String codex = "";
        switch(inp) {
            case "E_SMILE" :
            codex = "\uD83D\uDE03";
            break;
            case "E_SUPER_SMILE" :
                codex = "\uD83D\uDE04";
                break;
            case "E_WINK" :
                codex = "\uD83D\uDE09";
                break;
            case "E_TONGUE" :
                codex = "\uD83D\uDE1B";
                break;
            case "E_SUPER_WINK" :
                codex = "\uD83D\uDE06";
                break;
            case "E_TONGUE_WINK" :
                codex = "\uD83D\uDE1C";
                break;
            case "E_SUNGLASSES" :
                codex = "\uD83D\uDE0E";
                break;
            default:
                break;
        }
        return codex;
    }

}
