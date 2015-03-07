package com.lemonslice.appathon;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by alexander on 3/7/15.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback{
    public Camera camera;
    private SurfaceHolder viewHolder;
    private MyFaceDetectionListener myFaceDetectionListener;
    public String currEmoji = "";
    Camera.Parameters cameraParameters;

    static Rect faceDetected;
    static Boolean bFace;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        viewHolder = getHolder();
        viewHolder.addCallback(this);
        camera.setPreviewCallback(this);
        myFaceDetectionListener = new MyFaceDetectionListener();

        faceDetected = new Rect();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(camera==null) {
            camera = openFrontCamera();
        }
        try {
            camera.setPreviewDisplay(holder);
            cameraParameters = camera.getParameters();

            Log.d("EMOJI", "CP HEIGHT: " + getHeight());

//            Camera.Size optimalPreviewSize = getOptimalPreviewSize(getWidth(), getHeight());
//            cameraParameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
//            camera.setParameters(cameraParameters); // turning this on breaks face detection

            camera.setFaceDetectionListener(myFaceDetectionListener);
            camera.setPreviewCallback(this);
            camera.startPreview();
            camera.startFaceDetection();
        } catch (IOException e) {
            Log.d("EMOJI", "ERROR SETTING CAMERA PREVIEW " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) return; // preview surface does not exist

        try {
            camera.stopPreview();
        } catch (Exception ignored) {}
        surfaceCreated(holder);

        Log.d("Test", "Surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        assert(camera != null);
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;

    }

    // stolen from the internet because I am cool
    private Camera.Size getOptimalPreviewSize(int w, int h) {
        List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
        StringBuilder sizeStr = new StringBuilder();
        sizeStr.append("SIZES: [");
        for (Camera.Size size : sizes) {
            sizeStr.append(String.format("(%d,%d),", size.width, size.height));
        }
        sizeStr.append("]");
        Log.d("EMOJI", sizeStr.toString());


        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - h);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - h);
                }
            }
        }
        return optimalSize;
    }

    public void onPreviewFrame(byte[] data, Camera camera) {
        if (cameraParameters.getPreviewFormat() == ImageFormat.NV21) {
            Camera.Size previewSize = cameraParameters.getPreviewSize();

            YuvImage img = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);

            EmojiDetector.get_emoji_from_image(img, previewSize.width, previewSize.height);

            if(MainActivity.hello == 1) {
                MainActivity.hello = 0;

                if(bFace)
                {
                    Log.d("James","Saving new face");
                    bFace = false;
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    img.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 80, out);

                    File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) , "/test.jpg");

                    Log.d("James", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString());
                    FileOutputStream s = null;
                    try {
                        s = new FileOutputStream(outputFile);
                        s.write(out.toByteArray());
                        s.flush();
                        s.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    s = null;
                }

            }

//            EmojiDetector.emoji xxx = EmojiDetector.get_emoji_from_image(img, previewSize.width, previewSize.height);
//            Log.d("Sam", xxx.toString());
//
//            setCurrEmoji(xxx.toString());

//            YuvImage img = new YuvImage(data, ImageFormat.NV21, previewSize.width, previewSize.height, null);
//            byte[] yuvData = img.getYuvData();
        }

        Log.d("Testicles", "on preview frame");
    }

    public class MyFaceDetectionListener implements Camera.FaceDetectionListener {

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            Log.d("EMOJI", "onFaceDetection");
            if (faces.length > 0) {
                Log.d("EMOJI", "" + faces.length + "Faces detected");
                for (Camera.Face face : faces) {

                    Log.d("EMOJI", "FACE FOUND AT:");
                    currEmoji = "\uD83D\uDE03";
                    Log.d("EMOJI", String.format("l %d r %d top %d bottom %d", face.rect.left, face.rect.right, face.rect.top, face.rect.bottom));

                    faceDetected = face.rect;
                    bFace = true;
                }
            }
        }
    }

    public static Camera openFrontCamera() {
        Camera c = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                Log.d("orientation", String.valueOf(cameraInfo.orientation));
                try {
                    c = Camera.open(i);
//                    Camera.Parameters p = c.getParameters();
//                    p.setRotation(cameraInfo.orientation);
//                    c.setParameters(p);
                    c.setDisplayOrientation(360-cameraInfo.orientation);
//                    c.setDisplayOrientation(270);
                } catch (RuntimeException e) {
                    Log.e("EMOJI", "Front camera did not open");
                }
                break;
            }
        }
        return c;
    }

    public String getCurrEmoji() {

        return currEmoji;
    }

    public void setCurrEmoji(String newEmoji) {
        currEmoji = newEmoji;

    }
}
