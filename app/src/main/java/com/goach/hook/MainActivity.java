package com.goach.hook;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * author: Goach.zhong
 * Date: 2018/8/8 13:56.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void clickPage1(View view){
        startActivity(new Intent(MainActivity.this,Page1Activity.class));
    }
    public void clickPage2(View view){
        startActivity(new Intent(MainActivity.this,Page2Activity.class));
    }
    public void clickPage3(View view){
        startActivity(new Intent(MainActivity.this,Page3Activity.class));
    }
    public void clickPage4(View view){
        startActivity(new Intent(MainActivity.this,Page4Activity.class));
    }
    public void clickLoginOut(View view){
       SPUtils.getInstance(this).saveBoolean("isLogin",false);
    }
}

