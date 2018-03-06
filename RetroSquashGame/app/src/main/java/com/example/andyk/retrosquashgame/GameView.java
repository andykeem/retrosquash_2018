package com.example.andyk.retrosquashgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * Created by andyk on 3/5/18.
 */

public class GameView extends SurfaceView implements Runnable {

    protected static final String TAG = GameView.class.getSimpleName();
    protected static final float BALL_RADIUS = 16f;
    protected static final int RACKET_WIDTH = 160;
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
    protected float mBallDiameter;
    protected boolean mBallDown;
    protected boolean mBallLeft;
    protected boolean mBallRight;
    protected boolean mBallUp;
    protected Paint mRacketPaint;
    protected float mRacketX;
    protected float mRacketY;
    protected float mRacketLeft;
    protected float mRacketTop;
    protected float mRacketRight;
    protected float mRacketBottom;

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

    protected void init() {
        mHolder = this.getHolder();
        mBallPaint = new Paint();
        mBallPaint.setColor(Color.GREEN);
        mRacketPaint = new Paint();
        mRacketPaint.setColor(Color.rgb(255, 165, 0));
        mTask = new Thread(this);

        mBallDiameter = (BALL_RADIUS * 2);

        WindowManager winMgr = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        winMgr.getDefaultDisplay().getSize(outSize);
        mDeviceWidth = outSize.x;
        mDeviceHeight = outSize.y;

        // ball init position
        mBallX = (mDeviceWidth / 2);
        mBallY = 0;

        mBallDown = true;
        mBallRight = true;

        // racket init position
        mRacketX = (mDeviceWidth / 2) - (RACKET_WIDTH / 2);
        mRacketY = (mDeviceHeight - 200);

        mRacketLeft = mRacketX;
        mRacketTop = mRacketY;
        mRacketRight = (mRacketX + RACKET_WIDTH);
        mRacketBottom = (mRacketY + RACKET_HEIGHT);

        this.drawUI();
    }

    protected void updateUI() {

        int dx = 5;
        int dy = 5;
        if ((mBallX + mBallDiameter) > mDeviceWidth) {
            mBallRight = false;
            mBallLeft = true;
        } else if (mBallX <= 0) {
            mBallLeft = false;
            mBallRight = true;
        }
        if ((mBallY + mBallDiameter) > mDeviceHeight) {
            mBallDown = false;
            mBallUp = true;
        } else if (mBallY <= 0) {
            mBallUp = false;
            mBallDown = true;
        }

        if (mBallLeft) {
            dx = -5;
        }
        if (mBallUp) {
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