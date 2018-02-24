package com.example.administrator.yymusic.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


/**
 * @author yysleep
 */
public class CircularProgressView extends View {

    private int mMaxProgress = 100;

    private int mProgress = 30;

    // 画圆所在的距形区域
    private final RectF mRectF;

    private final Paint mPaint;

    private boolean isOnlyProgress;

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRectF = new RectF();
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();
        final int mCircleLineStrokeWidth = 6;

        if (!isOnlyProgress) {
            if (width != height) {
                int min = Math.min(width, height);
                width = min;
                height = min;
            }

            // 设置画笔相关属性
            // 圆形宽度
            mPaint.setAntiAlias(true);
            // Color.rgb(0xe9, 0xe9, 0xe9)  默认颜色（未开始）
            mPaint.setColor(Color.rgb(222, 222, 222));
            canvas.drawColor(Color.TRANSPARENT);
            mPaint.setStrokeWidth(mCircleLineStrokeWidth);
            mPaint.setStyle(Paint.Style.STROKE);
            // 位置
            mRectF.left = mCircleLineStrokeWidth / 2; // 左上角x
            mRectF.top = mCircleLineStrokeWidth / 2; // 左上角y
            mRectF.right = width - mCircleLineStrokeWidth / 2; // 左下角x
            mRectF.bottom = height - mCircleLineStrokeWidth / 2; // 右下角y
            canvas.drawArc(mRectF, -90, 360, false, mPaint);
        }

        // 设置进度颜
        float left = (mCircleLineStrokeWidth / 2); // 左上角x
        float top = (mCircleLineStrokeWidth / 2); // 左上角y
        float right = (width - mCircleLineStrokeWidth / 2); // 左下角x
        float bottom = (height - mCircleLineStrokeWidth / 2); // 右下角y

        mPaint.setColor(Color.rgb(0x67, 0xc8, 0xae));
        canvas.drawArc(left, top, right, bottom, -90, ((float) mProgress / mMaxProgress) * 360, false, mPaint);

        //  下面这段 是绘制进度文案显示
//        final int mTxtStrokeWidth = 2;
//        mPaint.setStrokeWidth(mTxtStrokeWidth);
//        String text = mProgress + "%";
//        int textHeight = height / 4;
//        mPaint.setTextSize(textHeight);
//        int textWidth = (int) mPaint.measureText(text, 0, text.length());
//        mPaint.setStyle(Paint.Style.FILL);
//        canvas.drawText(text, width / 2 - textWidth / 2, height / 2 + textHeight / 2, mPaint);
//
//        if (!TextUtils.isEmpty(mTxtHint1)) {
//            mPaint.setStrokeWidth(mTxtStrokeWidth);
//            text = mTxtHint1;
//            textHeight = height / 8;
//            mPaint.setTextSize(textHeight);
//            mPaint.setColor(Color.rgb(0x99, 0x99, 0x99));
//            textWidth = (int) mPaint.measureText(text, 0, text.length());
//            mPaint.setStyle(Paint.Style.FILL);
//            canvas.drawText(text, width / 2 - textWidth / 2, height / 4 + textHeight / 2, mPaint);
//        }
//
//        if (!TextUtils.isEmpty(mTxtHint2)) {
//            mPaint.setStrokeWidth(mTxtStrokeWidth);
//            text = mTxtHint2;
//            textHeight = height / 8;
//            mPaint.setTextSize(textHeight);
//            textWidth = (int) mPaint.measureText(text, 0, text.length());
//            mPaint.setStyle(Paint.Style.FILL);
//            canvas.drawText(text, width / 2 - textWidth / 2, 3 * height / 4 + textHeight / 2, mPaint);
//        }
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        isOnlyProgress = true;
        this.invalidate();
        isOnlyProgress = false;
    }

}
