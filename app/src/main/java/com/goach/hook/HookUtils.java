package com.goach.hook;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * author: Goach.zhong
 * Date: 2018/11/13 09:43.
 * Des:
 */
public class HookUtils {
    private Context mCxt;
    public static String INTENT_PARAMS = "extraRealIntent";
    private LoginDelegate delegate;
    public HookUtils(Context ctx,LoginDelegate delegate) {
        this.mCxt = ctx;
        this.delegate = delegate;
    }

    public HookUtils hookStartActivity() {
        try {
            Object singletonObj;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                Class<?> actManagerClazz = ActivityManager.class;
                Field iActManagerSingleton = actManagerClazz.getDeclaredField("IActivityManagerSingleton");
                iActManagerSingleton.setAccessible(true);
                singletonObj = iActManagerSingleton.get(null);
            }else{
                Class<?> actManagerNative = Class.forName("android.app.ActivityManagerNative");
                Field gDefault = actManagerNative.getDeclaredField("gDefault");
                gDefault.setAccessible(true);
                singletonObj = gDefault.get(null);
            }
            Class<?> singleClazz = Class.forName("android.util.Singleton");
            Field mInstanceField = singleClazz.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object oldIActManager = mInstanceField.get(singletonObj);
            Class<?> newIActManager = Class.forName("android.app.IActivityManager");
            InvocationHandler invocationHandler = new StartActivityInvocationHandler(oldIActManager);
            Object iActManagerProxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{newIActManager}, invocationHandler);
            mInstanceField.set(singletonObj,iActManagerProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
    public void hookLaunchActivity(){
        try{
            // 获取ActivityThread实例
            Class<?> actThreadClazz = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThread = actThreadClazz.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThread.setAccessible(true);
            Object mCurrentActivityThread = sCurrentActivityThread.get(null);
            // 获取ActivityThread里的mH对象
            Field mHField = actThreadClazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(mCurrentActivityThread);
            // 使用静态代理，替换接口mCallback
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH, new HandlerCallback());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private class StartActivityInvocationHandler implements InvocationHandler{
        private Object oldActManager;

        public StartActivityInvocationHandler(Object oldActManager) {
            this.oldActManager = oldActManager;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Object resultVal;
            if("startActivity".equals(methodName)){
                Log.d("hookUtil","执行前调用......");
                Intent realIntent = null;
                int index = 0 ;
                for(int i = 0 ; i < args.length ; i ++){
                    if(args[i] instanceof Intent){
                        realIntent = (Intent) args[i];
                        index = i ;
                        break;
                    }
                }
                if(realIntent != null){
                    // 把意图定向到代理界面，绕过AMS的检查
                    ComponentName loginComponentName = new ComponentName(mCxt, ProxyActivity.class);
                    Intent newIntent = new Intent();
                    newIntent.setComponent(loginComponentName);
                    // 把原意图，隐藏到新意图中
                    newIntent.putExtra(INTENT_PARAMS, realIntent);
                    args[index] = newIntent;
                }
            }
            // 3. 调用原方法执行
            resultVal = method.invoke(oldActManager, args);
            return resultVal;
        }
    }
    private class HandlerCallback implements Handler.Callback{

        @Override
        public boolean handleMessage(Message msg) {
            // LAUNCH_ACTIVITY ==100 启动一个activity
            if (msg.what == 100) {
                handleLaunchActivity(msg);
            }
            // mH自己去启动activity
            return false;
        }
    }
    private void handleLaunchActivity(Message msg){
        try {
            // 获取Intent（msg.obj属于类ActivityClientRecord）
            Field intentField = msg.obj.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent intent = (Intent) intentField.get(msg.obj);
            // 获取要Intent要跳转的Activity名称
            ComponentName component = intent.getComponent();
            if(component != null){
                String className = intent.getComponent().getClassName();
                // 非代理Activity，无需处理
                if (!ProxyActivity.class.getName().equals(className)) {
                    return;
                }
                Intent realIntent = intent.getParcelableExtra(INTENT_PARAMS);
                ComponentName realComponent = realIntent.getComponent();
                if(realComponent != null){
                    // 还原真实intent: ComponentName和Bundle（可能携带数据）
                    String realClassName = realComponent.getClassName();
                    intent.setComponent(realIntent.getComponent());
                    if(realIntent.getExtras()!=null)
                    intent.putExtras(realIntent.getExtras());
                    BindLogin bindLoginClass = Class.forName(realComponent.getClassName()).getAnnotation(BindLogin.class);
                    if(bindLoginClass != null && bindLoginClass.isNeedLogin()){
                        if (!delegate.isLogin()) {
                            // 未登录：跳到LoginActivity，并把要实际要跳转的intent，封装到内部
                            ComponentName loginComponent = new ComponentName(mCxt, LoginActivity.class);
                            intent.setComponent(loginComponent);
                            intent.putExtra(INTENT_PARAMS, realClassName);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    abstract static class LoginDelegate{
        abstract boolean isLogin();
    }
}
