package com.wsy.glcamerademo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.Nullable;

public class RoundBorderView extends View {
    private Paint paint;
    private int radius = 0;


    public RoundBorderView(Context context) {
        this(context, null);
    }

    public RoundBorderView(Context context, @Nullable AttributeSet attrs) {
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            SweepGradient sweepGradient = new SweepGradient(((float) getWidth() / 2), ((float) getHeight() / 2),
                    new int[]{Color.GREEN, Color.CYAN, Color.BLUE, Color.CYAN, Color.GREEN}, null);
            paint.setShader(sweepGradient);
        }
        drawBorder(canvas, 6);
    }


    private void drawBorder(Canvas canvas, int rectThickness) {
        if (canvas == null) {
            return;
        }
        paint.setStrokeWidth(rectThickness);
        Path drawPath = new Path();
        drawPath.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), radius, radius, Path.Direction.CW);
        canvas.drawPath(drawPath, paint);
    }

    public void turnRound() {
        invalidateOutline();
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}