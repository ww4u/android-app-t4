package com.megarobo.control.blockly;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.activity.BaseActivity;
import com.megarobo.control.activity.EquipmentStatusActivity;
import com.megarobo.control.bean.Config;
import com.megarobo.control.bean.DataSet;
import com.megarobo.control.bean.DeviceStatus;
import com.megarobo.control.bean.LinkStatus;
import com.megarobo.control.bean.Parameter;
import com.megarobo.control.bean.Point;
import com.megarobo.control.bean.Pose;
import com.megarobo.control.event.ParameterReceiveEvent;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.NetUtil;
import com.megarobo.control.utils.Utils;

import org.greenrobot.eventbus.EventBus;


/**
 * 点击左边的圆形区域调用jointCommand（）
 * 界面上的开和闭代表爪子，点击开则value = 1，点击闭则value = -1，按住则continous=true，同时joint=4
 *
 * 左边的圆形代表腕运动,joint = 3, continous=false, value=角度
 *
 *
 * 点击右边的圆形区域调用stepCommand（）
 *
 * z等于0，表示是有角度的，z=1或者-1，角度没有意义，两个是互斥的操作
 *
 */
public class SetWristActivity extends BaseActivity implements View.OnClickListener,View.OnLongClickListener,View.OnTouchListener{

    private Handler handler;
    private SocketClientManager controlClient;

    private Context mContext;

    @ViewInject(R.id.play)
    private TextView playBtn;

    @ViewInject(R.id.stop)
    private TextView stopBtn;


    @ViewInject(R.id.back)
    private ImageView backImg;

    @ViewInject(R.id.link_status)
    private TextView linkStatus;

    @ViewInject(R.id.net_status)
    private TextView netStatus;

    @ViewInject(R.id.left_control_up)
    private TextView leftControlUp;

    @ViewInject(R.id.left_control_down)
    private TextView leftControlDown;

    @ViewInject(R.id.left_control_front)
    private TextView leftControlFront;

    @ViewInject(R.id.left_control_back)
    private TextView leftControlBack;

    @ViewInject(R.id.clockwise)
    private ImageButton clockWise;

    @ViewInject(R.id.counterclockwise)
    private ImageButton counterClockWise;

    @ViewInject(R.id.angle)
    private TextView angle;

    @ViewInject(R.id.step)
    private TextView step;

    @ViewInject(R.id.confirm)
    private TextView confirm;

    @ViewInject(R.id.cancel)
    private TextView cancel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wrist);
        ViewUtils.inject(this);
        mContext = this;

        setClickListener();

        initHandler();
        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();


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
        }else if(networkState == NetUtil.NETWORK_MOBILE){
            netStatus.setText("断开");
            netStatus.setTextColor(getResources().getColor(R.color.orange));
        }else{
            netStatus.setText("正常");
            netStatus.setTextColor(getResources().getColor(R.color.blue_status));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(controlClient!=null && controlClient.isConnected()){
            Logger.e("onResume","isConnected............");
            controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("device_status"));
        }
    }

    /**
     * TODO
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.stop:
                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("emergency_stop"));
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.link_status:
                Intent intent = new Intent(mContext, EquipmentStatusActivity.class);
                startActivity(intent);
                break;
            //4个方向点击事件
            case R.id.left_control_up:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(90,false,3));
                break;
            case R.id.left_control_down:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(270,false,3));
                break;
            case R.id.left_control_front:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(180,false,3));
                break;
            case R.id.left_control_back:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(0,false,3));
                break;
            //顺时针，逆时针单击事件
            case R.id.clockwise:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,false,3));
                break;
            case R.id.counterclockwise:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,false,3));
                break;
            case R.id.play:
                controlClient.sendMsgToServer(
                                    CommandHelper.getInstance().scriptCommand(generateCode()));
                break;
            case R.id.confirm:
                if(pose == null){
                    break;
                }
                Intent intent1 = new Intent();
                intent1.putExtra("angle",Math.round(pose.getW())+"");
                setResult(RESULT_OK,intent1);
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.step:
                showSelectDialog(step);
                break;
        }
    }

    private String generateCode() {
        String code = "wrist("+Math.round(pose.getW())+")";
        return code;
    }


    Config config;
    private void initSpeed() {
        if(config != null) {
            stepWhich = getStepWhich(config.getStep());
            step.setText(config.getStep() + "");
//            stepEdit.setSelection(stepEdit.getText().length());
        }
    }

    private int getStepWhich(double step){
        if(1 == step){
            return 0;
        }else if(20 == step){
            return 1;
        }else if(50 == step){
            return 2;
        }else if(100 == step){
            return 3;
        }
        return 2;
    }


    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.clockwise:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,true,3));
                break;
            case R.id.counterclockwise:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,true,3));
                break;
//            case R.id.step:
//                showSelectDialog(step);
//                break;

        }
        return true;
    }

    private String stepPercent[] = new String[] { "1","20", "50", "100"};
    private int stepWhich = 2;

    /**
     * 显示选择
     */
    public void showSelectDialog(final TextView editText) {
        editText.requestFocus();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择：");
        // 选择下标
        builder.setSingleChoiceItems(stepPercent, stepWhich,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stepWhich = which;
                        editText.setText(stepPercent[which]);
//                        editText.setSelection(editText.getText().length());
                        dialog.cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {

            case R.id.clockwise:
            case R.id.counterclockwise:

            case R.id.left_control_up:
            case R.id.left_control_down:
            case R.id.left_control_front:
            case R.id.left_control_back:


            if(event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL){
                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("stop"));
            }
            break;
        }
        return false;
    }

    DataSet dataSet;
    Point point;
    Pose pose;


    boolean isFirstIn = true;
    @SuppressLint("HandlerLeak")
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ConstantUtil.SOCKET_CONNECTED:
                        Logger.e("control","socket_connected");
                        if(isFirstIn) {
                            controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("device_status"));
                            controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("pose"));
                            //读取用户设置的信息，速度，步距
                            controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("config"));
                            isFirstIn = false;
                        }
                        netStatus.setText("正常");
                        netStatus.setTextColor(getResources().getColor(R.color.blue_status));
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        processMsg(msg);
                        break;
                    case ConstantUtil.SOCKET_DISCONNECTED:
                        netStatus.setText("断开");
                        netStatus.setTextColor(getResources().getColor(R.color.orange));
                        break;
                }

            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * 处理所有服务器发过来的消息
     * @param msg
     */
    private void processMsg(Message msg){
        Bundle bundle = msg.getData();
        String content = bundle.getString("content");
        String command = bundle.getString("command");
        if("config".equals(command)){
            config = Config.parseConfig(content);
            initSpeed();
        }else if("package".equals(command)){

        }else if("link_status".equals(command)){
            LinkStatus linkStatus = LinkStatus.parseLinkStatus(content);

        }else if("dataset".equals(command)){
            dataSet = DataSet.parseDataSet(content);
        }else if("pose".equals(command)){
            JSONObject result = JSON.parseObject(content);
            if(result != null || !result.isEmpty()){
                pose = Pose.parsePose(result.getString("pose"));
                angle.setText(Math.round(pose.getW())+"°");
            }
        }else if("device_status".equals(command)){
            DeviceStatus deviceStatus = DeviceStatus.parseDeviceStatus(content);
            setStatus(deviceStatus);
        }else if("notify".equals(command)){
            controlClient.sendMsgToServer(CommandHelper.getInstance().linkCommand(false));
            setResult(ConstantUtil.RESULT_CODE_NOTIFY);
            onBackPressed();
        }else if("parameter".equals(command)){
            Parameter parameter = Parameter.parseParameter(content);
            EventBus.getDefault().postSticky(new ParameterReceiveEvent(parameter));
        }
    }



    /**
     * 为页面上所有的按钮添加点击事件
     */
    private void setClickListener(){

        playBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);

        clockWise.setOnTouchListener(this);
        counterClockWise.setOnTouchListener(this);

        //上下，前后点击事件
        leftControlBack.setOnClickListener(this);
        leftControlBack.setOnTouchListener(this);
        leftControlUp.setOnClickListener(this);
        leftControlUp.setOnTouchListener(this);
        leftControlDown.setOnClickListener(this);
        leftControlDown.setOnTouchListener(this);
        leftControlFront.setOnClickListener(this);
        leftControlFront.setOnTouchListener(this);

        //顺时针，逆时针事件
        clockWise.setOnClickListener(this);
        counterClockWise.setOnClickListener(this);
        //长按事件
        clockWise.setOnLongClickListener(this);
        counterClockWise.setOnLongClickListener(this);

        clockWise.setOnTouchListener(this);
        counterClockWise.setOnTouchListener(this);

        linkStatus.setOnClickListener(this);

//        stepEdit.setOnLongClickListener(this);
        step.setOnClickListener(this);

        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(controlClient != null && controlClient.isConnected()){
            controlClient.exit();
        }
        Logger.e("controlActivity","onDestroy.........");
    }
}
