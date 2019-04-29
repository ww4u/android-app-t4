package com.megarobo.control.activity.subview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.bean.Parameter;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;

public class StatusSubView implements View.OnClickListener{

    private TextView restoreBtn;

    private TextView refreshBtn;

    private ImageView backImg;

    private TextView driverCurrent1;
    private TextView driverCurrent2;
    private TextView driverCurrent3;
    private TextView driverCurrent4;
    private TextView driverCurrent5;

    private TextView idleCurrent1;
    private TextView idleCurrent2;
    private TextView idleCurrent3;
    private TextView idleCurrent4;
    private TextView idleCurrent5;

    private TextView segment1;
    private TextView segment2;
    private TextView segment3;
    private TextView segment4;
    private TextView segment5;

    private TextView slow1;
    private TextView slow2;
    private TextView slow3;
    private TextView slow4;
    private TextView slow5;

    private TextView machineValue;
    private TextView positionValue;
    private TextView sensorValue;
    private TextView zeroValue;
    private TextView collisionValue;
    private TextView speedValue;
    private TextView energyValue;

    private SocketClientManager controlClient;
    private Context mContext;
    private RelativeLayout mStatusView;


    private static StatusSubView instance;

    private StatusSubView(){

    }

    public static StatusSubView getInstance(){
        if(instance == null){
            instance = new StatusSubView();
        }
        return instance;
    }

    public void initStatusSubView(Activity activity,SocketClientManager controlClient){
        this.controlClient = controlClient;
        this.mContext = activity;
        initViews(activity);
        refreshBtn.setOnClickListener(this);
        restoreBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);
    }

    private void initViews(Activity activity) {
        restoreBtn = activity.findViewById(R.id.restoreBtn);
        refreshBtn = activity.findViewById(R.id.refresh);
        backImg = activity.findViewById(R.id.back);
        driverCurrent1 = activity.findViewById(R.id.driver_current_1);
        driverCurrent2 = activity.findViewById(R.id.driver_current_2);
        driverCurrent3 = activity.findViewById(R.id.driver_current_3);
        driverCurrent4 = activity.findViewById(R.id.driver_current_4);
        driverCurrent5 = activity.findViewById(R.id.driver_current_5);

        idleCurrent1 = activity.findViewById(R.id.idle_current_1);
        idleCurrent2 = activity.findViewById(R.id.idle_current_2);
        idleCurrent3 = activity.findViewById(R.id.idle_current_3);
        idleCurrent4 = activity.findViewById(R.id.idle_current_4);
        idleCurrent5 = activity.findViewById(R.id.idle_current_5);

        segment1 = activity.findViewById(R.id.segment_1);
        segment2 = activity.findViewById(R.id.segment_2);
        segment3 = activity.findViewById(R.id.segment_3);
        segment4 = activity.findViewById(R.id.segment_4);
        segment5 = activity.findViewById(R.id.segment_5);

        slow1 = activity.findViewById(R.id.slow_1);
        slow2 = activity.findViewById(R.id.slow_2);
        slow3 = activity.findViewById(R.id.slow_3);
        slow4 = activity.findViewById(R.id.slow_4);
        slow5 = activity.findViewById(R.id.slow_5);

        machineValue = activity.findViewById(R.id.machine_value);
        positionValue = activity.findViewById(R.id.position_value);
        sensorValue = activity.findViewById(R.id.sensor_value);
        zeroValue = activity.findViewById(R.id.zero_value);
        collisionValue = activity.findViewById(R.id.collision_value);
        speedValue = activity.findViewById(R.id.speed_value);
        energyValue = activity.findViewById(R.id.energy_value);


    }

    public void showStatusView(int layoutId, ViewStub statusViewStub){
        Utils.hiddenKeyBoard(mContext);
        if(mStatusView == null){//为空则初始化控件
            statusViewStub.setLayoutResource(layoutId);
            this.mStatusView = (RelativeLayout) statusViewStub.inflate();
//            mStatusView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
        }else{//显示或隐藏View
//            mStatusView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
            this.mStatusView.setVisibility(View.VISIBLE);
        }
    }


    public boolean isStatusViewShown(){
        if(mStatusView == null || !mStatusView.isShown()){
            return false;
        }
        return true;
    }

    public void goneStatusView(){
        if(mStatusView != null && mStatusView.isShown()){
            mStatusView.setVisibility(View.GONE);
        }
    }


    public void setEquipmentStatus(Parameter parameter) {
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
        speedValue.setText(parameter.getMaxSpeed()+"mm/s,"+parameter.getMaxJointSpeed()+"度/s");

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
//                onBackPressed();
                goneStatusView();
                break;
            case R.id.refresh:
                if(Utils.isNotEmptyString(MegaApplication.ip)) {
                    controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("parameter"));
                }
                break;

        }
    }
}
