package com.megarobo.control.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.bean.DeviceStatus;
import com.megarobo.control.bean.Meta;
import com.megarobo.control.bean.Parameter;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.NetUtil;
import com.megarobo.control.utils.Utils;

public class EquipmentActivity extends BaseActivity implements View.OnClickListener{


    @ViewInject(R.id.switchBtn)
    private TextView switchBtn;

    @ViewInject(R.id.remoteControlBtn)
    private TextView remoteControlBtn;

    @ViewInject(R.id.back)
    private ImageView backImg;

    @ViewInject(R.id.link_status)
    private TextView linkStatus;

    @ViewInject(R.id.net_status)
    private TextView netStatus;

    @ViewInject(R.id.equipment_name)
    private TextView equipmentName;

    private boolean isExit;
    private static final int MSG_EXIT_ROOM = 5001;
    private static final int MSG_EXIT_ROOM_DELAY = 2000;

    private Handler handler;
    private SocketClientManager controlClient;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        ViewUtils.inject(this);


        backImg.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        remoteControlBtn.setOnClickListener(this);
        linkStatus.setOnClickListener(this);


        initHandler();
        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();

        equipmentName.setText(MegaApplication.name);

    }

    @SuppressLint("HandlerLeak")
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ConstantUtil.SOCKET_CONNECTED:
                        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("device_status"));
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        Bundle bundle = msg.getData();
                        String content = bundle.getString("content");
                        String command = bundle.getString("command");
                        if("device_status".equals(command)){
                            DeviceStatus deviceStatus = DeviceStatus.parseDeviceStatus(content);
                            setStatus(deviceStatus);
                        }
                        break;
                    case MSG_EXIT_ROOM:
                        isExit = false;
                        break;
                }

            }
        };
    }

    private void setStatus(DeviceStatus deviceStatus) {
        if(deviceStatus.getStatus().equals(DeviceStatus.RUNNING)){
            linkStatus.setText("运动中");
            linkStatus.setTextColor(getResources().getColor(R.color.blue_status));
        }else if(deviceStatus.getStatus().equals(DeviceStatus.STOPED)){
            linkStatus.setText("停止");
            linkStatus.setTextColor(getResources().getColor(R.color.blue_status));
        }else if(deviceStatus.getStatus().equals(DeviceStatus.EXCEPTION_STOPED)){
            linkStatus.setText("异常");
            linkStatus.setTextColor(getResources().getColor(R.color.orange));
        }

    }

    @Override
    public void onChange(int networkState) {
        super.onChange(networkState);
        if(networkState == NetUtil.NETWORK_NONE){
            netStatus.setText("断开");
            netStatus.setTextColor(getResources().getColor(R.color.orange));
            remoteControlBtn.setEnabled(false);
        }else if(networkState == NetUtil.NETWORK_MOBILE){
            netStatus.setText("断开");
            netStatus.setTextColor(getResources().getColor(R.color.orange));
            remoteControlBtn.setEnabled(false);
        }else{
            netStatus.setText("正常");
            netStatus.setTextColor(getResources().getColor(R.color.blue_status));
            remoteControlBtn.setEnabled(true);
        }
    }

    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back:
                onBackPressed();
                break;
            case R.id.switchBtn:
                Intent intent = new Intent(EquipmentActivity.this, ConnectActivity.class);
                startActivity(intent);
                break;
            case R.id.remoteControlBtn:
                Intent intent1 = new Intent(EquipmentActivity.this, EquipmentControlActivity1.class);
                startActivity(intent1);
                break;
            case R.id.link_status:
                Intent intent2 = new Intent(EquipmentActivity.this, EquipmentStatusActivity.class);
                startActivity(intent2);
                break;
        }
    }



    @SuppressLint("NewApi")
    @Override
    public void onBackPressed() {
        if(!isExit){
            isExit = true;
            Utils.MakeToast(EquipmentActivity.this,"再按一次退出程序");
            handler.sendEmptyMessageDelayed(MSG_EXIT_ROOM, MSG_EXIT_ROOM_DELAY);
        }else{
            if(MegaApplication.list!=null && MegaApplication.list.size()>0){
                for (Activity activity : MegaApplication.list){
                    if(activity!=null && !activity.isDestroyed()){
                        activity.finish();
                    }
                }
            }
            super.onBackPressed();
        }
    }
}
