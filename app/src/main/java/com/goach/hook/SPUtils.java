package com.goach.hook;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * author: Goach.zhong
 * Date: 2018/12/4 18:17.
 * Des:
 */
public class SPUtils {
    private static volatile SPUtils instance;
    private SharedPreferences preferences;
    private SPUtils(Context ctx){
        preferences = ctx.getSharedPreferences("hook",Context.MODE_PRIVATE);
    }
    public static SPUtils getInstance(Context ctx){
        if(instance == null){
            synchronized (SPUtils.class){
                if(instance == null){
                    instance = new SPUtils(ctx);
                }
            }
        }
        return instance;
    }
    public void saveBoolean(String key,boolean value){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    public boolean getBoolean(String key){
        return preferences.getBoolean(key, false);
    }
}
