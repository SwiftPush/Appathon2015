package com.lemonslice.appathon;

import android.util.Log;

import android.app.Activity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class CvStuff extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    private CameraBridgeViewBase   mOpenCvCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("testicles", "2");
        super.onCreate(savedInstanceState);
        Log.i("testicles", "hello");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.delivery_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("Testicles", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    public Mat onCameraFrame(Mat inputFrame)
    {

        return inputFrame;
    }

    public void onCameraViewStopped()
    {

    }

    public void onCameraViewStarted(int width, int height)
    {
        //mGray = new Mat();
    }
}