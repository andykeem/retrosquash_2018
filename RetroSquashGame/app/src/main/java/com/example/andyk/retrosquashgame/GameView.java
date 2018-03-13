package com.example.andyk.retrosquashgame;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by andyk on 3/5/18.
 */

public class GameView extends SurfaceView implements Runnable, SurfaceView.OnTouchListener {

    protected static final String TAG = GameView.class.getSimpleName();
    protected static final float BALL_RADIUS = 16f;
    protected static final int RACKET_HEIGHT = 16;
    protected static final float TEXT_SIZE = 48f; // 200f;
    protected static final float TEXT_TOP = 64f; // 240f;
    protected static final float TEXT_LEFT = 8f;
    protected static final int NUM_LIVES = 3;
    protected static final int MILLIS_TO_SLEEP = 15; // 15 milliseconds

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
    protected int mLives;
    protected int mScore;
    protected Paint mTextPaint;
    protected float mScoreTextLeft;
    protected float mLiveTextLeft;
    protected boolean mFinished;
    protected float mUnitsToMove;
    protected long mLastRunMillis;
    protected int mFps;

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
            this.controlFPS();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mTouchX = event.getX();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                this.updateRacketPosition();
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
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(TEXT_SIZE);

        mTask = new Thread(this);

        WindowManager winMgr = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        winMgr.getDefaultDisplay().getSize(outSize);
        mDeviceWidth = outSize.x;
        mDeviceHeight = outSize.y;

        // ball init position
        mBallX = (mDeviceWidth / 2);
        this.resetBacllPosition();

        mBallMoveDown = true;
        mBallMoveRight = true;

        // racket init position
        mRacketWidth = (mDeviceWidth / 6);
        mRacketX = (mDeviceWidth / 2) - (mRacketWidth / 2);
        mRacketY = (mDeviceHeight - (mDeviceHeight / 10));

        mRacketLeft = mRacketX;
        mRacketTop = mRacketY;
        mRacketRight = (mRacketX + mRacketWidth);
        mRacketBottom = (mRacketY + RACKET_HEIGHT);

        mLives = NUM_LIVES;
        mScoreTextLeft = 80f;
        mLiveTextLeft = (mDeviceWidth - TEXT_SIZE);

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

        mUnitsToMove = (mDeviceHeight / 150);
        if (mBallMoveUp) {
            mUnitsToMove += 5;
        }

        float dx = mUnitsToMove;
        float dy = mUnitsToMove;

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

        if (mBallY <= 0) {
            mBallMoveUp = false;
            mBallMoveDown = true;
        }

        // check ball with racket collision
        if ((mRacketTop < ballBottom) && (ballBottom < mRacketBottom)) {
            if (((mRacketLeft < ballLeft) && (ballLeft < mRacketRight)) ||
                    ((mRacketLeft < ballRight) && (ballRight < mRacketRight))) {
                mBallMoveUp = true;
                mScore++;
            }
        }

        if (mBallMoveLeft) {
            dx *= -1;
        }
        if (mBallMoveUp) {
            dy *= -1;
        }

        mBallX += dx;
        mBallY += dy;

        if ((mBallY + BALL_RADIUS) >= mDeviceHeight) {
            mLives--;
            this.resetBacllPosition();
            if (mLives <= 0) {
                mFinished = true;
                this.pauseGame();
            }
        }
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

        // draw score
//        canvas.drawText(String.valueOf(mScore), mScoreTextLeft, TEXT_TOP, mTextPaint);

        // draw lives
//        canvas.drawText(String.valueOf(mLives), mLiveTextLeft, TEXT_TOP, mTextPaint);

        String info = String.format("Score: %d Lives: %d FPS: %d", mScore, mLives, mFps);

        canvas.drawText(info, TEXT_LEFT, TEXT_TOP, mTextPaint);

        mHolder.unlockCanvasAndPost(canvas);

        if (mFinished) {
            this.handleGameOver();
        }
    }

    protected void controlFPS() {
        long elapsedMillis = (System.currentTimeMillis() - mLastRunMillis);
        if (elapsedMillis > 0) {
            mFps = (int) (1000 / elapsedMillis);
        }
        long millisToSleep = (MILLIS_TO_SLEEP - elapsedMillis);
        if (millisToSleep > 0) {
            try {
                Thread.sleep(millisToSleep);
            } catch (InterruptedException ie) {
                Log.e(TAG, ie.getMessage(), ie);
            }
        }
        mLastRunMillis = System.currentTimeMillis();
    }

    protected void resetBacllPosition() {
        mBallY = (0 - BALL_RADIUS);
    }

    protected void handleGameOver() {
        this.pauseGame();
        GameView.logThread();
        this.showAlertDialog();
    }

    protected void resetLive() {
        mLives = NUM_LIVES;
    }

    protected void resetScore() {
        mScore = 0;
    }

    protected void restartGame() {
        GameView.logThread();
        mFinished = false;
        this.resetLive();
        this.resetScore();
        this.stopGame();
        this.init();
        this.startGame();
    }

    public void startGame() {
        mRunning = true;
        if (mTask == null) {
            mTask = new Thread();
        }
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
            } finally {
                mTask = null;
            }
        }
    }

    protected void showAlertDialog() {
        ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(mContext)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.game_over_alert_title)
                        .setMessage(R.string.game_over_alert_message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                restartGame();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ((AppCompatActivity) mContext).finish();
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }

    protected void log(Object val, String text) {
        String s = text + val;
        Log.d(TAG, s);
    }

    public static void logThread() {
        Log.d(TAG, "curr thread: " + Thread.currentThread().getName());
    }
}