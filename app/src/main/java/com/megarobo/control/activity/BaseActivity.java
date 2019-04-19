package com.megarobo.control.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.net.NetBroadcastReceiver;
import com.megarobo.control.utils.Utils;


/**
 * baseActivity
 * 
 * @author huangli
 */
public abstract class BaseActivity extends FragmentActivity implements NetBroadcastReceiver.NetChangeListener {

	protected TextView back;
    private FrameLayout cover;
    private ImageView progress;
    protected RelativeLayout nonet;
    protected Button refresh;
    private OnClickListener clickListener;

    private NetBroadcastReceiver netBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //强制横屏
        if(this.getResources().getConfiguration().orientation ==Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        setContentView(R.layout.activity_base);
        cover =(FrameLayout) findViewById(R.id.cover);
        progress=(ImageView) findViewById(R.id.rotate_progress);
        nonet=(RelativeLayout) findViewById(R.id.no_net);
        refresh=(Button) findViewById(R.id.refresh);
        nonet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

            }
        });
        cover.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return true;
            }
        });

        MegaApplication.list.add(this);


        //实例化IntentFilter对象
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcastReceiver = new NetBroadcastReceiver(this);
        //注册广播接收
        registerReceiver(netBroadcastReceiver, filter);

    }

    @Override
    public void onChange(int networkState) {
    }

    private boolean isNetworkAvailable() {
        return Utils.isNetwokAvailable(this);
    }

    protected void checkNet(OnClickListener listener) {
        if (!isNetworkAvailable()) {
            this.clickListener=listener;
            nonet.setVisibility(View.VISIBLE);
            refresh.setOnClickListener(listener);
            if (back!=null) {
                back.setEnabled(false);
            }
        } else {
            this.clickListener=null;
            nonet.setVisibility(View.GONE);
            if (back!=null) {
                back.setEnabled(true);
            }
        }
    }


    protected void setMask(boolean b) {
    	
		if (b) {
			cover.setVisibility(View.VISIBLE);
//			progress.startAnimation(animation);
			progress.setVisibility(View.VISIBLE);
		} else {
            cover.setVisibility(View.GONE);
//            progress.clearAnimation();
            progress.setVisibility(View.GONE);
		}
	}
    
    protected void toActivity(Class<?> activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    protected void toActivityForResult(Class<?> activity, int requestCode) {
        Intent intent = new Intent(this, activity);
        startActivityForResult(intent,requestCode);
    }


    protected boolean hasBack() {
        return true;
    }


    protected void onResume() {
        super.onResume();

        
    }

    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netBroadcastReceiver != null){
            unregisterReceiver(netBroadcastReceiver);
        }
        MegaApplication.list.remove(this);
    }


}
