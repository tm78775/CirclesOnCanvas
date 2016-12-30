package com.trmdevelopment.circledragging;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;

/**
 * Created by TMiller on 12/27/2016.
 */

public class CirclesDrawingView extends View {

    private static final String TAG           = "CirclesDrawingView";
    private static final int    RADIUS        = 90;
    private static final int    CIRCLES_LIMIT = 1;

    /** Main bitmap */
    private Bitmap mBitmap = null;
    private Rect   mMeasuredRect;
    /** Paint to draw circles */
    private Paint  mCirclePaint;

    /** All available circles */
    private HashSet<Circle> mCircles = new HashSet<Circle>( CIRCLES_LIMIT );
    private SparseArray<Circle> mCirclePointer = new SparseArray<Circle>( CIRCLES_LIMIT );
    private int xCenterOfScreen;
    private int yCenterOfScreen;


    /**
     * Default constructor
     *
     * @param ct {@link android.content.Context}
     */
    public CirclesDrawingView(final Context ct) {
        super(ct);
        init(ct);
    }

    public CirclesDrawingView(final Context ct, final AttributeSet attrs) {
        super(ct, attrs);
        init(ct);
    }

    public CirclesDrawingView(final Context ct, final AttributeSet attrs, final int defStyle) {
        super(ct, attrs, defStyle);
        init(ct);
    }

    private void init(final Context ct) {
        // Generate bitmap used for background
        mBitmap = BitmapFactory.decodeResource(ct.getResources(), R.drawable.up_image);

        mCirclePaint = new Paint();

        mCirclePaint.setColor( getResources().getColor( R.color.colorAccent ) );
        mCirclePaint.setStrokeWidth(40);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(final Canvas canv) {
        // background bitmap to cover all area
        canv.drawBitmap(mBitmap, null, mMeasuredRect, null);

        for (Circle circle : mCircles) {
           canv.drawCircle(circle.centerX, circle.centerY, circle.radius, mCirclePaint);
        }
    }


    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        boolean handled = false;

        Circle touchedCircle;
        int xTouch;
        int yTouch;
        int pointerId;
        int actionIndex = event.getActionIndex();

        // get touch event coordinates and make transparent circle from it
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                // it's the first pointer, so clear all existing pointers data
                clearCirclePointer();

                xTouch = (int) event.getX(0);
                yTouch = (int) event.getY(0);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;
                mCirclePointer.put(event.getPointerId(0), touchedCircle);

                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.w(TAG, "Pointer down");
                // It secondary pointers, so obtain their ids and check circles
                pointerId = event.getPointerId(actionIndex);

                xTouch = (int) event.getX(actionIndex);
                yTouch = (int) event.getY(actionIndex);

                // check if we've touched inside some circle
                touchedCircle = obtainTouchedCircle(xTouch, yTouch);

                mCirclePointer.put(pointerId, touchedCircle);
                touchedCircle.centerX = xTouch;
                touchedCircle.centerY = yTouch;
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerCount = event.getPointerCount();

                Log.w(TAG, "Move");

                for (actionIndex = 0; actionIndex < pointerCount; actionIndex++) {
                    // Some pointer has moved, search it by pointer id
                    pointerId = event.getPointerId(actionIndex);

                    xTouch = (int) event.getX(actionIndex);
                    yTouch = (int) event.getY(actionIndex);

                    touchedCircle = mCirclePointer.get(pointerId);

                    if (touchedCircle != null) {
                        touchedCircle.centerX = xTouch;
                        touchedCircle.centerY = yTouch;
                    }
                }
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_UP:
                xTouch = (int) event.getX( actionIndex );
                yTouch = (int) event.getY( actionIndex );
                touchedCircle = obtainTouchedCircle( xTouch, yTouch );
                touchedCircle.returnToOrigin();

                clearCirclePointer();
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // not general pointer was up
                pointerId = event.getPointerId(actionIndex);

                mCirclePointer.remove(pointerId);
                invalidate();
                handled = true;
                break;

            case MotionEvent.ACTION_CANCEL:
                handled = true;
                break;

            default:
                // do nothing
                break;
        }

        return super.onTouchEvent(event) || handled;
    }

    /**
     * Clears all Circle - pointer id relations
     */
    private void clearCirclePointer() {
        Log.w(TAG, "clearCirclePointer");
        mCirclePointer.clear();
    }

    /**
     * Search and creates new (if needed) circle based on touch area
     *
     * @param xTouch int x of touch
     * @param yTouch int y of touch
     *
     * @return obtained {@link Circle}
     */
    private Circle obtainTouchedCircle(final int xTouch, final int yTouch) {
        Circle touchedCircle = getTouchedCircle(xTouch, yTouch);

        if (null == touchedCircle) {
            touchedCircle = new Circle(xTouch, yTouch, RADIUS);

            if (mCircles.size() == CIRCLES_LIMIT) {
                Log.w(TAG, "Clear all circles, size is " + mCircles.size());
                // remove first circle
                mCircles.clear();
            }

            Log.w(TAG, "Added circle " + touchedCircle);
            mCircles.add(touchedCircle);
        }

        return touchedCircle;
    }

    /**
     * Determines touched circle
     *
     * @param xTouch int x touch coordinate
     * @param yTouch int y touch coordinate
     *
     * @return {@link Circle} touched circle or null if no circle has been touched
     */
    private Circle getTouchedCircle(final int xTouch, final int yTouch) {
        Circle touched = null;

        for (Circle circle : mCircles) {
            if ((circle.centerX - xTouch) * (circle.centerX - xTouch) + (circle.centerY - yTouch) * (circle.centerY - yTouch) <= circle.radius * circle.radius) {
                touched = circle;
                break;
            }
        }

        return touched;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        xCenterOfScreen = widthMeasureSpec / 2;
        yCenterOfScreen = heightMeasureSpec / 2;
        mMeasuredRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }





    /**
     * Model for storing Circle data.
     */
    private static class Circle {
        private int radius;
        private int centerX;
        private int centerY;
        private int originX;
        private int originY;

        Circle(int centerX, int centerY, int radius) {
            this.radius  = radius;
            this.centerX = centerX;
            this.centerY = centerY;
            this.originX = centerX;
            this.originY = centerY;


        }

        @Override
        public String toString() {
            return "Circle[" + centerX + ", " + centerY + "]";
        }

        public void returnToOrigin() {
            centerX = originX;
            centerY = originY;
        }
    }
}
