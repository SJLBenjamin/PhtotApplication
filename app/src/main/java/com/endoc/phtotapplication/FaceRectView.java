package com.endoc.phtotapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/*
 * 描述：
 * 创建：huangmuquan
 * 日志：2018/12/25
 */public class FaceRectView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private Rect rect;
    private String mCorlor = "#00ff00";
    private String mShowInfo = null;

    public FaceRectView(Context context) {
        super(context);
        initPaint(context);
    }
    public FaceRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initPaint(context);
    }

    public FaceRectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint(context);
    }

    private void initPaint(Context context) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.parseColor(mCorlor));

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStrokeWidth(5);
        mTextPaint.setTextSize(40);
        mTextPaint.setColor(Color.GREEN);
    }

    public void drawFaceRect(Rect facerect) {
        this.rect = facerect;
        postInvalidate();
    }

    public void drawFaceInfo(String faceInfo) {
        this.mShowInfo = faceInfo;
        postInvalidate();
    }

    public void clearRect() {
        rect = null;
        mShowInfo = null;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rect != null) {
            /**
             * 左上竖线
             */
            canvas.drawLine(rect.left, rect.top, rect.left, rect.top + 22, mPaint);
            /**
             * 左上横线
             */
            canvas.drawLine(rect.left, rect.top, rect.left + 22, rect.top, mPaint);

            /**
             * 右上竖线
             */
            canvas.drawLine(rect.right, rect.top, rect.right - 22, rect.top, mPaint);
            /**
             * 右上横线
             */
            canvas.drawLine(rect.right, rect.top, rect.right, rect.top + 22, mPaint);
            /**
             * 左下竖线
             */
            canvas.drawLine(rect.left, rect.bottom, rect.left, rect.bottom - 22, mPaint);
            /**
             * 左下横线
             */
            canvas.drawLine(rect.left, rect.bottom, rect.left + 22, rect.bottom, mPaint);

            /**
             * 右下竖线
             */
            canvas.drawLine(rect.right, rect.bottom, rect.right, rect.bottom-22, mPaint);
            /**
             * 右下横线
             */
            canvas.drawLine(rect.right, rect.bottom, rect.right-22, rect.bottom , mPaint);

            if (mShowInfo != null){
                canvas.drawText(mShowInfo, rect.left, rect.top - 10, mTextPaint);
            }
        }

    }

    public Rect transForm(Rect faceRect, int sfW, int sfH, boolean mirror){
        Matrix matrix = new Matrix();
        matrix.setScale(1f, mirror ? -1f : 1f); //mirror Front (-1f, 1f), back (1f, 1f)
        matrix.postRotate(90f);//Camera Rotation

        //Camera FaceDetection range from (-1000, -1000) to (1000, 1000).
        //UI  range from (0, 0) to (width, height).
        matrix.postScale(sfW / 2000f, sfH / 2000f);
        matrix.postTranslate(sfW / 2f, sfH / 2f);

        RectF srcRect = new RectF(faceRect);
        RectF dstRect = new RectF(0f, 0f, 0f, 0f);
        matrix.mapRect(dstRect, srcRect);

        return new Rect((int)dstRect.left,(int)dstRect.top, (int)dstRect.right, (int)dstRect.bottom);
    }

}
