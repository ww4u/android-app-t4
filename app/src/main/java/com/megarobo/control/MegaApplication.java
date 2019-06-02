package com.megarobo.control;


import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.SystemClock;

import com.megarobo.control.bean.AreaDeviceBean;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.mqtt.MQTTService;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.AllUitls;
import com.megarobo.control.utils.ThreadPoolWrap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MegaApplication extends Application {


    //保存当前连接机器的ip，用于在每个页面进行连接用
    public static String ip;
    public static String myIp;
    public static String name;
    private static MegaApplication mContext;


    public static List<Activity> list;
    public static List<AreaDeviceBean> beans = new ArrayList<>();

    public static List<Robot> robotList;
    public static Set<String> latestRoboSet = new HashSet<String>();

    public MegaApplication() {
        mContext = this;
    }

    public static MegaApplication getInstance() {
        return mContext;
    }

    public SocketClientManager controlClient;


    @Override
    public void onCreate() {
        super.onCreate();

        list=new ArrayList<Activity>();

        myIp = AllUitls.getIPAddressStr(MegaApplication.this);

        startService(new Intent(this, MQTTService.class));

        ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
            @Override
            public void run() {
                AllUitls.initAreaIp(MegaApplication.this);
                int sum = 0;
                while (beans.size() == 0 && sum < 8) {
                    beans.addAll(AllUitls.getAllCacheMac(MegaApplication.myIp));
                    SystemClock.sleep(beans.size()>0?0:1000);
                    sum++;
                }
            }
        });

    }

    public void exit(){
        if(robotList != null){
            robotList.clear();
            robotList = null;
        }
    }
    
}
