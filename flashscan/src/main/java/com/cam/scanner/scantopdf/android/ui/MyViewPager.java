package com.cam.scanner.scantopdf.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class MyViewPager extends ViewPager
{
    public MyViewPager(final Context context, final AttributeSet set) {
        super(context, set);
    }
    
    public boolean dispatchTouchEvent(final MotionEvent motionEvent) {
        try {
            return super.dispatchTouchEvent(motionEvent);
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        catch (IllegalArgumentException ex2) {
            return false;
        }
    }
    
    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {
        try {
            return super.onInterceptTouchEvent(motionEvent);
        }
        catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
