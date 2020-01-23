package com.project.videoeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class VideoEditBar extends View {


    private int mLeft; // TODO: use a default from R.string...
    private int mRight; // TODO: use a default from R.color...
    private int mTop; // TODO: use a default from R.dimen...
    private int mBottom; // TODO: use a default from R.dimen...

    private Paint paint;
    private Rect rect;
    private TypedArray a;

    public VideoEditBar(Context context) {
        super(context);
        init(null, 0);
    }

    public VideoEditBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public VideoEditBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        a = getContext().obtainStyledAttributes(
                attrs, R.styleable.VideoEditBar, defStyle, 0);

        mLeft = a.getInt(R.styleable.VideoEditBar_piv_Left,0);
        mRight = a.getInt(R.styleable.VideoEditBar_piv_Right,0);
        mTop = a.getInt(R.styleable.VideoEditBar_piv_Top,100);
        mBottom = a.getInt(R.styleable.VideoEditBar_piv_Bottom,100);

        paint = new Paint();
        rect = new Rect(0,0,0,0);
        a.recycle();

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.


        paint.setColor(Color.BLUE);
        paint.setAlpha(60);

        rect.bottom = mBottom;
        rect.left = mLeft;
        rect.top = mTop;
        rect.right = mRight;

        canvas.drawRect(rect,paint);

    }
    public int getmLeft() {
        return mLeft;
    }

    public void setmLeft(int mLeft) {
        this.mLeft = mLeft;
    }

    public int getmRight() {
        return mRight;
    }

    public void setmRight(int mRight) {
        this.mRight = mRight;
    }

    public int getmTop() {
        return mTop;
    }

    public void setmTop(int mTop) {
        this.mTop = mTop;
    }

    public int getmBottom() {
        return mBottom;
    }

    public void setmBottom(int mBottom) {
        this.mBottom = mBottom;
    }


}
