package com.megarobo.control.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.bean.Parameter;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.Utils;
import com.megarobo.control.R;

public class EquipmentStatusActivity extends BaseActivity implements View.OnClickListener{

    @ViewInject(R.id.restoreBtn)
    private TextView restoreBtn;

    @ViewInject(R.id.refresh)
    private TextView refreshBtn;

    @ViewInject(R.id.back)
    private ImageView backImg;

    private Handler handler;
    private SocketClientManager controlClient;
    private Context mContext;

    @ViewInject(R.id.driver_current_1)
    private TextView driverCurrent1;
    @ViewInject(R.id.driver_current_2)
    private TextView driverCurrent2;
    @ViewInject(R.id.driver_current_3)
    private TextView driverCurrent3;
    @ViewInject(R.id.driver_current_4)
    private TextView driverCurrent4;
    @ViewInject(R.id.driver_current_5)
    private TextView driverCurrent5;

    @ViewInject(R.id.idle_current_1)
    private TextView idleCurrent1;
    @ViewInject(R.id.idle_current_2)
    private TextView idleCurrent2;
    @ViewInject(R.id.idle_current_3)
    private TextView idleCurrent3;
    @ViewInject(R.id.idle_current_4)
    private TextView idleCurrent4;
    @ViewInject(R.id.idle_current_5)
    private TextView idleCurrent5;

    @ViewInject(R.id.segment_1)
    private TextView segment1;
    @ViewInject(R.id.segment_2)
    private TextView segment2;
    @ViewInject(R.id.segment_3)
    private TextView segment3;
    @ViewInject(R.id.segment_4)
    private TextView segment4;
    @ViewInject(R.id.segment_5)
    private TextView segment5;

    @ViewInject(R.id.slow_1)
    private TextView slow1;
    @ViewInject(R.id.slow_2)
    private TextView slow2;
    @ViewInject(R.id.slow_3)
    private TextView slow3;
    @ViewInject(R.id.slow_4)
    private TextView slow4;
    @ViewInject(R.id.slow_5)
    private TextView slow5;

    @ViewInject(R.id.machine_value)
    private TextView machineValue;
    @ViewInject(R.id.position_value)
    private TextView positionValue;
    @ViewInject(R.id.sensor_value)
    private TextView sensorValue;
    @ViewInject(R.id.zero_value)
    private TextView zeroValue;
    @ViewInject(R.id.collision_value)
    private TextView collisionValue;
    @ViewInject(R.id.speed_value)
    private TextView speedValue;
    @ViewInject(R.id.energy_value)
    private TextView energyValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = EquipmentStatusActivity.this;
        setContentView(R.layout.activity_equipment_status);
        ViewUtils.inject(this);

        initHandler();
        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();

        setListener();
    }

    private void setListener() {
        refreshBtn.setOnClickListener(this);
        restoreBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);
    }

    @SuppressLint("HandlerLeak")
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ConstantUtil.SOCKET_CONNECTED:
                        if(Utils.isNotEmptyString(MegaApplication.ip)) {
                            controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("parameter"));
                        }
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        Bundle bundle = msg.getData();
                        String content = bundle.getString("content");
                        String command = bundle.getString("command");
                        if("parameter".equals(command)){
                            Parameter parameter = Parameter.parseParameter(content);
                            setEquipmentStatus(parameter);
                        }else if("package".equals(command)){
                            Utils.MakeToast(EquipmentStatusActivity.this,"恢复出厂设置成功");
                        }
                        break;
                }

            }
        };
    }

    private void setEquipmentStatus(Parameter parameter) {
        driverCurrent1.setText(parameter.getCurrents()[0].toString());
        driverCurrent2.setText(parameter.getCurrents()[1].toString());
        driverCurrent3.setText(parameter.getCurrents()[2].toString());
        driverCurrent4.setText(parameter.getCurrents()[3].toString());
        driverCurrent5.setText(parameter.getCurrents()[4].toString());

        idleCurrent1.setText(parameter.getIdleCurrents()[0].toString());
        idleCurrent2.setText(parameter.getIdleCurrents()[1].toString());
        idleCurrent3.setText(parameter.getIdleCurrents()[2].toString());
        idleCurrent4.setText(parameter.getIdleCurrents()[3].toString());
        idleCurrent5.setText(parameter.getIdleCurrents()[4].toString());

        segment1.setText(Math.round(parameter.getMicroSteps()[0])+"");
        segment2.setText(Math.round(parameter.getMicroSteps()[1])+"");
        segment3.setText(Math.round(parameter.getMicroSteps()[2])+"");
        segment4.setText(Math.round(parameter.getMicroSteps()[3])+"");
        segment5.setText(Math.round(parameter.getMicroSteps()[4])+"");

        slow1.setText(parameter.getSlowRatio()[0].toString());
        slow2.setText(parameter.getSlowRatio()[1].toString());
        slow3.setText(parameter.getSlowRatio()[2].toString());
        slow4.setText(parameter.getSlowRatio()[3].toString());
        slow5.setText(parameter.getSlowRatio()[4].toString());

        machineValue.setText(format(parameter.getHandIo()));
        sensorValue.setText(format(parameter.getDistanceSensors()));
        collisionValue.setText(parameter.isCollide() ? "开" : "关");
        energyValue.setText(format(parameter.getTunning()));
        positionValue.setText(
                Math.round(parameter.getPose().getX())+","+
                Math.round(parameter.getPose().getY())+","+
                Math.round(parameter.getPose().getZ())+","+
                Math.round(parameter.getPose().getW())+","+
                Math.round(parameter.getPose().getH()));
        zeroValue.setText(parameter.isMechanicalIo()?"开":"关");
        speedValue.setText(parameter.getMaxSpeed()+"mm/s,"+parameter.getMaxJointSpeed()+"");

    }

    private String format(Boolean[] args){
        StringBuffer sb = new StringBuffer();
        for(Boolean b : args){
            if(b){
                sb.append("开，");
            }else{
                sb.append("关，");
            }
        }
        return sb.toString().substring(0,sb.lastIndexOf("，"));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.restoreBtn:
                Utils.customDialog(mContext, "此操作不可逆", new Utils.DialogListenner() {
                    @Override
                    public void confirm() {
                        if(Utils.isNotEmptyString(MegaApplication.ip)){
                            controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("package"));
                        }
                    }
                },"确定将机器恢复出厂姿态吗?");
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.refresh:
                if(Utils.isNotEmptyString(MegaApplication.ip)) {
                    controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("parameter"));
                }
                break;

        }
    }
}
