package com.project.videoeditor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class VideoEditBar extends View {


    private int mLeftBackground; // TODO: use a default from R.string...
    private int mRightBackground; // TODO: use a default from R.color...
    private int mTopBackground; // TODO: use a default from R.dimen...
    private int mBottomBackground; // TODO: use a default from R.dimen...

    private final int WIDTH_BAND = 5;
    private int mLeft_LeftBand; // TODO: use a default from R.string...
    private int mRight_LeftBand; // TODO: use a default from R.color...
    private int mTop_LeftBand; // TODO: use a default from R.dimen...
    private int mBottom_LeftBand; // TODO: use a default from R.dimen...

    private int mLeft_RightBand; // TODO: use a default from R.string...
    private int mRight_RightBand; // TODO: use a default from R.color...
    private int mTop_RightBand; // TODO: use a default from R.dimen...
    private int mBottom_RightBand; // TODO: use a default from R.dimen...

    private Paint paintBackgroundColor;
    private Paint paintBandColor;
    private Rect rectBackground;
    private Rect leftBand;
    private Rect rightBand;
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

        mLeftBackground = a.getInt(R.styleable.VideoEditBar_piv_LeftBackground,0);
        mRightBackground = a.getInt(R.styleable.VideoEditBar_piv_RightBackground,0);
        mTopBackground = a.getInt(R.styleable.VideoEditBar_piv_TopBackground,100);
        mBottomBackground = a.getInt(R.styleable.VideoEditBar_piv_BottomBackground,100);

        mLeft_LeftBand =  a.getInt(R.styleable.VideoEditBar_piv_Left_LeftBand,0);
        mRight_LeftBand =  a.getInt(R.styleable.VideoEditBar_piv_Right_LeftBand,0);

        mLeft_RightBand =  a.getInt(R.styleable.VideoEditBar_piv_Left_RightBand,0);
        mRight_RightBand =  a.getInt(R.styleable.VideoEditBar_piv_Right_RightBand,0);

        paintBackgroundColor = new Paint();
        paintBandColor = new Paint();

        rectBackground = new Rect(0,0,0,0);
        leftBand = new Rect(0,0,0,0);
        rightBand = new Rect(0,0,0,0);
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


        paintBackgroundColor.setColor(Color.BLUE);
        paintBandColor.setColor(Color.GRAY);
        paintBackgroundColor.setAlpha(60);

        rectBackground.bottom = mBottomBackground;
        rectBackground.left = mLeftBackground;
        rectBackground.top = mTopBackground;
        rectBackground.right = mRightBackground;

        rightBand.left = mRightBackground + WIDTH_BAND ;
        rightBand.top = mTopBackground;
        rightBand.bottom = mBottomBackground;
        rightBand.right = mRightBackground;

        leftBand.left = mLeftBackground;
        leftBand.top = mTopBackground;
        leftBand.bottom = mBottomBackground;
        leftBand.right = mLeftBackground - WIDTH_BAND;

        canvas.drawRect(rectBackground,paintBackgroundColor);
        canvas.drawRect(rightBand,paintBandColor);
        canvas.drawRect(leftBand,paintBandColor);

    }
    public int getmLeftBackground() {
        return mLeftBackground;
    }

    public void setmLeftBackground(int mLeftBackground) {
        this.mLeftBackground = mLeftBackground;
    }

    public int getmRightBackground() {
        return mRightBackground;
    }

    public void setmRightBackground(int mRightBackground) {
        this.mRightBackground = mRightBackground;
    }

    public int getmTopBackground() {
        return mTopBackground;
    }

    public void setmTopBackground(int mTopBackground) {
        this.mTopBackground = mTopBackground;
    }

    public int getmBottomBackground() {
        return mBottomBackground;
    }

    public void setmBottomBackground(int mBottomBackground) {
        this.mBottomBackground = mBottomBackground;
    }


}
