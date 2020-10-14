package com.endoc.phtotapplication.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;




public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    public Context mContext = this;
    //static long touchTime;//触摸时间,static修饰,只有一份
    static String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

    }

    public abstract void initData();

    public abstract void initView();








    @Override
    protected void onRestart() {
        super.onRestart();

    }

}
