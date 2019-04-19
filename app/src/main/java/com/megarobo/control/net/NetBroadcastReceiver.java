package com.megarobo.control.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.megarobo.control.utils.NetUtil;

public class NetBroadcastReceiver extends BroadcastReceiver {

    public NetBroadcastReceiver(NetChangeListener netChangeListener){
        this.netChangeListener = netChangeListener;
    }

    public NetChangeListener netChangeListener;


    public interface NetChangeListener{
        void onChange(int networkState);
    }


    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = NetUtil.getNetWorkState(context);
            if (netChangeListener!=null) {
                netChangeListener.onChange(netWorkState);
            }
        }
    }
}
