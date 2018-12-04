package com.goach.hook;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * author: Goach.zhong
 * Date: 2018/12/4 11:10.
 * Des:
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HookUtils hookUtil=new HookUtils(this, new HookUtils.LoginDelegate(){
            @Override
            boolean isLogin() {
                // TODO 判断是否登录
              return SPUtils.getInstance(App.this.getApplicationContext()).getBoolean("isLogin");
            }
        });
        hookUtil.hookStartActivity().hookLaunchActivity();
    }
}
