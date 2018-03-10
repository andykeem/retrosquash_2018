package com.example.andyk.retrosquashgame;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG = MainActivity.class.getSimpleName();
    protected GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        mGameView = new GameView(this);
        this.setContentView(mGameView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGameView.startGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGameView.resumeGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameView.pauseGame();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGameView.stopGame();
    }
}
