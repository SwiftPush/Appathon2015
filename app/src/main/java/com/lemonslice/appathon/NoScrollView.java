package com.lemonslice.appathon;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Created by alexander on 3/7/15.
 *
 * Basically you can't disable scrolling on a scroll view so this class just makes it unscrollable by the user.
 * Scroll halfway along the view to crop to the center of the frame with view.scrollTo(0, view.getBottom()/2)
 */
public class NoScrollView extends ScrollView {

    public NoScrollView(Context context) {
        super(context);
    }

    public NoScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NoScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev)
    {
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
        return false;
    }
}
