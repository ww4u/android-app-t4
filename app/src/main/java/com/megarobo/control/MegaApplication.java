package com.megarobo.control;


import android.app.Application;


public class MegaApplication extends Application {


    //保存当前连接机器的ip，用于在每个页面进行连接用
    public static String ip;
    
    @Override
    public void onCreate() {
        super.onCreate();
    }
    
}
