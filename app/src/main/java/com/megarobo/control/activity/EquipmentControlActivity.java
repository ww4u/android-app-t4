package com.megarobo.control.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;
import com.megarobo.control.view.DiscView;
import com.megarobo.control.R;

public class EquipmentControlActivity extends BaseActivity implements View.OnClickListener{

    private TextView textView;
    private MyBroadCastReceiver receiver;
    private LocalBroadcastManager manager;
    private DiscView leftControlView;

    private SocketClientManager clientManager;


    @ViewInject(R.id.open_btn)
    private Button openBtn;

    @ViewInject(R.id.close_btn)
    private Button closeBtn;

    @ViewInject(R.id.up_btn)
    private Button upBtn;

    @ViewInject(R.id.down_btn)
    private Button downBtn;

    @ViewInject(R.id.restore)
    private Button restoreBtn;

    @ViewInject(R.id.set)
    private Button setBtn;

    @ViewInject(R.id.mark)
    private Button markBtn;

    @ViewInject(R.id.stop)
    private Button stopBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_control);
        ViewUtils.inject(this);
        setClickListener();
        leftControlView = findViewById(R.id.leftControlView);
        textView = findViewById(R.id.postion);

        /**
         * 注册本地广播接收器就，接收广播
         **/
        manager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(leftControlView.DISC_BROADCAST);
        receiver = new MyBroadCastReceiver();
        manager.registerReceiver(receiver,filter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"开");
                break;
            case R.id.close_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"闭");
                break;
            case R.id.up_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"上");
                break;
            case R.id.down_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"下");
                break;
            case R.id.restore:
                clientManager.sendMsgToServer(CommandHelper.getInstance().actionCommand("home"));
                break;
            case R.id.set:
                Utils.MakeToast(EquipmentControlActivity.this,"设置");
                break;
            case R.id.mark:
                Utils.MakeToast(EquipmentControlActivity.this,"记录该点");
                break;
            case R.id.stop:
                clientManager.sendMsgToServer(CommandHelper.getInstance().actionCommand("emergency_stop"));
                break;

        }
    }

    private void setClickListener(){
        openBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        upBtn.setOnClickListener(this);
        downBtn.setOnClickListener(this);
        restoreBtn.setOnClickListener(this);
        setBtn.setOnClickListener(this);
        markBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
    }

    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String string = intent.getStringExtra("move");
            textView.setText("当前坐标位置："+string);
        }
    }

}
