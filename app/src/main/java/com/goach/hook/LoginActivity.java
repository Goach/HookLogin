package com.goach.hook;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by yueban on 2018/8/6.
 */

@SuppressLint("Registered")
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        SPUtils.getInstance(this).saveBoolean("isLogin",true);
        //登录成功跳转目标界面
        String className = getIntent().getStringExtra(HookUtils.INTENT_PARAMS);
        if (className != null) {
            ComponentName componentName = new ComponentName(LoginActivity.this, className);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            startActivity(intent);
            finish();
        }
    }

}
