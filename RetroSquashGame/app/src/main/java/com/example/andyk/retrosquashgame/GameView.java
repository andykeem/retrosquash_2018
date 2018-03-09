package com.example.andyk.retrosquashgame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by andyk on 3/5/18.
 */

public class GameView extends SurfaceView implements Runnable, SurfaceView.OnTouchListener {

    protected static final String TAG = GameView.class.getSimpleName();
    protected static final float BALL_RADIUS = 16f;
    protected static final int RACKET_HEIGHT = 16;

    protected Context mContext;
    protected SurfaceHolder mHolder;
    protected Paint mBallPaint;
    protected Thread mTask;
    protected boolean mRunning;
    protected float mBallX;
    protected float mBallY;
    protected int mDeviceWidth;
    protected int mDeviceHeight;
    protected boolean mBallMoveDown;
    protected boolean mBallMoveLeft;
    protected boolean mBallMoveRight;
    protected boolean mBallMoveUp;
    protected int mRacketWidth;
    protected Paint mRacketPaint;
    protected float mRacketX;
    protected float mRacketY;
    protected float mRacketLeft;
    protected float mRacketTop;
    protected float mRacketRight;
    protected float mRacketBottom;
    protected float mTouchX;

    public GameView(Context context) {
        super(context, null);
        mContext = context;
        this.init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        this.init();
    }

    @Override
    public void run() {
        while (mRunning) {
            this.updateUI();
            this.drawUI();
            try {
                Thread.sleep(2);
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage(), ie);
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mTouchX = event.getX();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.updateRacketPosition();
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_MOVE:
                this.updateRacketPosition();
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    protected void init() {

        this.setOnTouchListener(this);
        mHolder = this.getHolder();
        mBallPaint = new Paint();
        mBallPaint.setColor(Color.GREEN);
        mRacketPaint = new Paint();
        mRacketPaint.setColor(Color.rgb(255, 165, 0));
        mTask = new Thread(this);

        WindowManager winMgr = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        winMgr.getDefaultDisplay().getSize(outSize);
        mDeviceWidth = outSize.x;
        mDeviceHeight = outSize.y;

        // ball init position
        mBallX = (mDeviceWidth / 2);
        mBallY = 0;

        mBallMoveDown = true;
        mBallMoveRight = true;

        // racket init position
        mRacketWidth = (mDeviceWidth / 6);
        mRacketX = (mDeviceWidth / 2) - (mRacketWidth / 2);
        mRacketY = (mDeviceHeight - 200);

        mRacketLeft = mRacketX;
        mRacketTop = mRacketY;
        mRacketRight = (mRacketX + mRacketWidth);
        mRacketBottom = (mRacketY + RACKET_HEIGHT);

        this.drawUI();
    }

    protected void updateRacketPosition() {
        mRacketLeft = mTouchX - (mRacketWidth / 2);
        if (mRacketLeft < 0) {
            mRacketLeft = 0;
        }
        mRacketRight = (mRacketLeft + mRacketWidth);
        if (mRacketRight > mDeviceWidth) {
            mRacketLeft = (mDeviceWidth - mRacketWidth);
        }
    }

    protected void updateUI() {

        int dx = 5;
        int dy = 5;

        // check ball with device border collition
        float ballLeft = mBallX;
//        float ballTop = mBallY;
        float ballRight = (mBallX + BALL_RADIUS);
        float ballBottom = (mBallY + BALL_RADIUS);
        if (ballRight > mDeviceWidth) {
            mBallMoveRight = false;
            mBallMoveLeft = true;
        } else if (mBallX <= 0) {
            mBallMoveLeft = false;
            mBallMoveRight = true;
        }
        if (ballBottom > mDeviceHeight) {
            mBallMoveDown = false;
            mBallMoveUp = true;
        } else if (mBallY <= 0) {
            mBallMoveUp = false;
            mBallMoveDown = true;
        }

        // check ball with racket collision
        if ((mRacketTop < ballBottom) && (ballBottom < mRacketBottom)) {
            if (((mRacketLeft < ballLeft) && (ballLeft < mRacketRight)) ||
                ((mRacketLeft < ballRight) && (ballRight < mRacketRight))) {
                mBallMoveUp = true;
            }
        }

        if (mBallMoveLeft) {
            dx = -5;
        }
        if (mBallMoveUp) {
            dy = -5;
        }

        mBallX += dx;
        mBallY += dy;
    }

    protected void drawUI() {
        if (mHolder == null) {
            return;
        }
        if (!mHolder.getSurface().isValid()) {
            return;
        }
        Canvas canvas = mHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        // draw logic..
        canvas.drawColor(Color.BLACK);

        // draw ball..
        canvas.drawCircle(mBallX, mBallY, BALL_RADIUS, mBallPaint);

        // draw racket..
        canvas.drawRect(mRacketLeft, mRacketTop, mRacketRight, mRacketBottom, mRacketPaint);

        mHolder.unlockCanvasAndPost(canvas);
    }

    public void startGame() {
        mRunning = true;
        mTask.start();
    }

    public void resumeGame() {
        if (!mRunning) {
            mRunning = true;
        }
    }

    public void pauseGame() {
        if (mRunning) {
            mRunning = false;
        }
    }

    public void stopGame() {
        mRunning = false;
        if (mTask != null) {
            try {
                mTask.join();
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage(), ie);
            }
        }
    }
}