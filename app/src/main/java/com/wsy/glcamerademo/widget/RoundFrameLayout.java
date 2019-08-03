package com.wsy.glcamerademo.widget;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

public class RoundFrameLayout extends FrameLayout {
    private int radius = 0;

    public RoundFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public RoundFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                Rect rect = new Rect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                outline.setRoundRect(rect, radius);
            }
        });
        setClipToOutline(true);
    }

    public void turnRound() {
        invalidateOutline();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }
}
