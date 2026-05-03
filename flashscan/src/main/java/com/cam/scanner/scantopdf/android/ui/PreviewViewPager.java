package com.cam.scanner.scantopdf.android.ui;

import android.widget.*;
import android.content.*;
import android.util.*;
import android.view.animation.*;
import android.view.*;

public class PreviewViewPager extends MyViewPager
{
    private PreviewViewPager.a a;
    private int b;
    private Scroller c;
    
    public PreviewViewPager(final Context context, final AttributeSet set) {
        super(context, set);
    }

    public interface a
    {
        boolean c();
    }


    static /* synthetic */ int a(final PreviewViewPager previewViewPager) {
        return previewViewPager.getScrollX();
    }
    
    private /* synthetic */ void a(final boolean b) {
        final Scroller c = this.c;
        final int a = a(this);
        int b2;
        if (b) {
            b2 = this.b;
        }
        else {
            b2 = -this.b;
        }
        c.startScroll(a, 0, b2, 0, 500);
        this.invalidate();
    }
    
    public void a(int b, final boolean b2) {
        if (this.b <= 0 || this.c == null) {
            this.b = b;
            this.c = new Scroller(this.getContext(), (Interpolator)new AccelerateDecelerateInterpolator());
        }
        final Scroller c = this.c;
        final int scrollX = this.getScrollX();
        if (b2) {
            b = -this.b;
        }
        else {
            b = this.b;
        }
        c.startScroll(scrollX, 0, b, 0, 500);
        this.invalidate();
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                a(b2);
            }
        }, 500L);
    }
    
    public void computeScroll() {
        super.computeScroll();
        final Scroller c = this.c;
        if (c != null && c.computeScrollOffset()) {
            this.scrollTo(this.c.getCurrX(), this.c.getCurrY());
            this.invalidate();
        }
    }
    
    public boolean onInterceptTouchEvent(final MotionEvent motionEvent) {
        final PreviewViewPager.a a = this.a;
        boolean onInterceptTouchEvent;
        if (a != null) {
            onInterceptTouchEvent = (a.c() ^ true);
        }
        else {
            try {
                onInterceptTouchEvent = super.onInterceptTouchEvent(motionEvent);
            }
            catch (IllegalArgumentException ex) {
                onInterceptTouchEvent = false;
            }
        }
        return onInterceptTouchEvent;
    }

    public void setListener(final PreviewViewPager.a a) {
        this.a = a;
    }
}
