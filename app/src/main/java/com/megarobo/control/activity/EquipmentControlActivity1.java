package com.megarobo.control.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
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
import com.megarobo.control.bean.Config;
import com.megarobo.control.bean.DataSet;
import com.megarobo.control.bean.DeviceStatus;
import com.megarobo.control.bean.LinkStatus;
import com.megarobo.control.bean.Point;
import com.megarobo.control.bean.Pose;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.NetUtil;
import com.megarobo.control.utils.Utils;
import com.megarobo.control.view.DiscView;
import com.megarobo.control.view.RightControl;


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
public class EquipmentControlActivity1 extends BaseActivity implements View.OnClickListener,View.OnLongClickListener,View.OnTouchListener{

    private TextView textView;
    private LocalBroadcastManager manager;

    private Handler handler;
    private SocketClientManager controlClient;
    private Context mContext;

    @ViewInject(R.id.open_btn)
    private TextView openBtn;

    @ViewInject(R.id.close_btn)
    private TextView closeBtn;

    @ViewInject(R.id.up_btn)
    private TextView upBtn;

    @ViewInject(R.id.down_btn)
    private TextView downBtn;

    @ViewInject(R.id.restore)
    private TextView restoreBtn;

    @ViewInject(R.id.set)
    private TextView setBtn;

    @ViewInject(R.id.mark)
    private TextView markBtn;

    @ViewInject(R.id.stop)
    private TextView stopBtn;

    @ViewInject(R.id.set_view_stub)
    private ViewStub setViewStub;

    @ViewInject(R.id.mark_view_stub)
    private ViewStub markViewStub;

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

    //右边控制按钮
    @ViewInject(R.id.right_bg)
    private ImageView rightBg;


    //前后左右
    @ViewInject(R.id.right_btn_front)
    private TextView rightBtnFront;

    @ViewInject(R.id.right_btn_back)
    private TextView rightBtnBack;

    @ViewInject(R.id.right_btn_left)
    private TextView rightBtnLeft;

    @ViewInject(R.id.right_btn_right)
    private TextView rightBtnRight;

    //四个角度
    @ViewInject(R.id.right_northwest)
    private ImageButton rightNorthwest;

    @ViewInject(R.id.right_northeast)
    private ImageButton rightNortheast;

    @ViewInject(R.id.right_southeast)
    private ImageButton rightSoutheast;

    @ViewInject(R.id.right_southwest)
    private ImageButton rightSouthwest;

    @ViewInject(R.id.right_center)
    private View rightCenter;

    @ViewInject(R.id.position)
    private TextView position;

    @ViewInject(R.id.angle)
    private TextView angle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_control1);
        ViewUtils.inject(this);
        mContext = this;

        textView = findViewById(R.id.postion);

        setClickListener();

        initHandler();
        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();

        /**
         * 注册本地广播接收器就，接收广播
         **/
//        manager = LocalBroadcastManager.getInstance(this);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(DiscView.DISC_BROADCAST);
//        receiver = new MyBroadCastReceiver();
//        manager.registerReceiver(receiver,filter);

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
//        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("link_status"));
    }

    /**
     * TODO
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,false,4));
                break;
            case R.id.close_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,false,4));
                break;
            case R.id.up_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,1,false));
                break;
            case R.id.down_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,-1,false));
                break;
            case R.id.restore:
                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("home"));
                break;
            case R.id.set:
                //先读取，后设置
                doSetAction();
                break;
            case R.id.mark:
                doMarkAction();
                break;
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
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(90,false,3));
                break;
            case R.id.left_control_down:
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(270,false,3));
                break;
            case R.id.left_control_front:
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(180,false,3));
                break;
            case R.id.left_control_back:
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(0,false,3));
                break;
            //顺时针，逆时针单击事件
            case R.id.clockwise:
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,false,3));
                break;
            case R.id.counterclockwise:
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,false,3));
                break;
            //前后左右
            case R.id.right_btn_front:
                float x = v.getX();
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(90,0,false));
                break;
            case R.id.right_btn_back:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(270,0,false));
                break;
            case R.id.right_btn_left:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(180,0,false));
                break;
            case R.id.right_btn_right:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,0,false));
                break;

            case R.id.right_northeast:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(45,0,false));
                break;
            case R.id.right_northwest:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(135,0,false));
                break;
            case R.id.right_southeast:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(225,0,false));
                break;
            case R.id.right_southwest:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(315,0,false));
                break;
        }
    }

    //设置右边是否可点击
    private void setRightEnable(boolean isEnable){
//        rightBtnFront.setEnabled(isEnable);
//        rightBtnBack.setEnabled(isEnable);
//        rightBtnLeft.setEnabled(isEnable);
//        rightBtnRight.setEnabled(isEnable);
//
//        rightNorthwest.setEnabled(isEnable);
//        rightNortheast.setEnabled(isEnable);
//        rightSouthwest.setEnabled(isEnable);
//        rightSoutheast.setEnabled(isEnable);
    }

    //设置左边是否可点击
    private void setLeftEnable(boolean isEnable){
//        leftControlFront.setEnabled(isEnable);
//        leftControlBack.setEnabled(isEnable);
//        leftControlUp.setEnabled(isEnable);
//        leftControlDown.setEnabled(isEnable);
//
//        clockWise.setEnabled(isEnable);
//        counterClockWise.setEnabled(isEnable);
    }

    private TextView markConfirm;
    private TextView markCancel;
    private ImageView markClose;
    private EditText name;
    /**
     * 处理记录该点逻辑
     */
    private void doMarkAction() {
        //点击先弹出页面展示已经保存的点位置
        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("dataset"));
        //同时要查询目前该点位置
        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("pose"));
        point = new Point();

        showMarkView(R.layout.control_mark_layout);
        markConfirm = findViewById(R.id.markConfirm);
        markCancel = findViewById(R.id.markCancel);
        markClose = findViewById(R.id.mark_close);
        name = findViewById(R.id.name);

        markClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneBottomView();
            }
        });
        markCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneBottomView();
            }
        });
        markConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataSet!=null && dataSet.getPointMap() != null){
                    if(dataSet.getPointMap().containsKey(name.getText().toString())){
                        Utils.MakeToast(EquipmentControlActivity1.this,"已经存在该名字的点，请重新命名");
                    }else{
                        controlClient.sendMsgToServer(CommandHelper.getInstance().addPointCommand(setPointName()));
                        goneBottomView();
                    }
                }
            }
        });

        //记录该点
    }


    private TextView speed;
    private TextView setConfirm;
    private TextView setCancel;
    private ImageView setClose;
    private EditText stepEdit;
    private EditText jointStepEdit;
    private double step;
    private double jointStep;
    private double speedNum;
    /**
     * 处理设置逻辑
     */
    private void doSetAction() {
        showSetView(R.layout.control_set_layout);
        speed = findViewById(R.id.speed);
        setConfirm = findViewById(R.id.confirm);
        setCancel = findViewById(R.id.cancel);
        setClose = findViewById(R.id.img_close);
        stepEdit = findViewById(R.id.step);
        jointStepEdit = findViewById(R.id.joint_step);
        setClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneBottomView();
            }
        });
        /**
         * 点击设置的确定按钮，向服务器发起设置命令
         */
        setConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isNotEmptyString(stepEdit.getText().toString())
                        || !Utils.isNotEmptyString(jointStepEdit.getText().toString())){
                    Utils.MakeToast(EquipmentControlActivity1.this,"步距和开合步距不能为空");
                    return;
                }
                double stepValue = Double.parseDouble(stepEdit.getText().toString());
                double jointStepValue = Double.parseDouble(jointStepEdit.getText().toString());
                if(stepValue > 100 || jointStepValue > 100){
                    Utils.MakeToast(EquipmentControlActivity1.this,"步距和开合步距不能超过100");
                    return;
                }
                speedNum = parseSpeed(speed.getText().toString());
                step = stepValue;
                jointStep = jointStepValue;
                controlClient.sendMsgToServer(CommandHelper.getInstance().configCommand(step,jointStep,speedNum));
                goneBottomView();
            }
        });
        setCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneBottomView();
            }
        });
        speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog();
            }
        });
        //读取用户设置的信息，速度，步距
        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("config"));
    }

    Config config;
    private void initSpeed() {
        if(config != null) {
            speed.setText(config.getSpeed() * 100 + "%");
            speedWhich = getSpeedwhich(config.getSpeed() * 100);
            stepEdit.setText(config.getStep() * 100 + "%");
            jointStepEdit.setText(config.getJointStep() * 100 + "%");
        }
    }

    private int getSpeedwhich(double speedPercent){
        if(20 == speedPercent){
            return 0;
        }else if(50 == speedPercent){
            return 1;
        }else if(100 == speedPercent){
            return 2;
        }
        return 2;
    }

    private double parseSpeed(String speed){
        if("20%".equals(speed)){
            return 0.2;
        }else if("50%".equals(speed)){
            return 0.5;
        }else if("100%".equals(speed)){
            return 1;
        }
        return 1;
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.open_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,true,4));
                break;
            case R.id.close_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,true,4));
                break;
            case R.id.up_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,1,true));
                break;
            case R.id.down_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,-1,true));
                break;

            case R.id.clockwise:
                setRightEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,true,3));
                break;
            case R.id.counterclockwise:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,true,3));
                break;

            //前后左右
            case R.id.right_btn_front:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(90,0,true));
                break;
            case R.id.right_btn_back:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(270,0,true));
                break;
            case R.id.right_btn_left:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(180,0,true));
                break;
            case R.id.right_btn_right:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,0,true));
                break;

            //四个角度
            case R.id.right_northeast:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(45,0,true));
                break;
            case R.id.right_northwest:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(135,0,true));
                break;
            case R.id.right_southeast:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(225,0,true));
                break;
            case R.id.right_southwest:
                setLeftEnable(false);
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(315,0,true));
                break;
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.open_btn:
            case R.id.close_btn:
            case R.id.up_btn:
            case R.id.down_btn:

            case R.id.clockwise:
            case R.id.counterclockwise:

            case R.id.left_control_up:
            case R.id.left_control_down:
            case R.id.left_control_front:
            case R.id.left_control_back:

            case R.id.right_btn_back:
            case R.id.right_btn_front:
            case R.id.right_btn_left:
            case R.id.right_btn_right:

            case R.id.right_northeast:
            case R.id.right_northwest:
            case R.id.right_southeast:
            case R.id.right_southwest:

            if(event.getAction() == MotionEvent.ACTION_UP){
                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("stop"));
                controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("pose"));
                setLeftEnable(true);
                setRightEnable(true);
            }
            break;
        }
        return false;
    }

    DataSet dataSet;
    Point point;
    Pose pose;
    /**
     * @return
     */
    private Point setPointName() {
        if(point == null){
            return null;
        }
        point.setName(name.getText().toString());
        point.setPose(pose);
        return point;
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
                        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("pose"));
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        processMsg(msg);
                        break;
                }

            }
        };
    }

    @Override
    public void onBackPressed() {
        if(mSetView!=null && mSetView.isShown()){
            goneBottomView();
            return;
        }
        if(mMarkView!=null && mMarkView.isShown()){
            goneBottomView();
            return;
        }
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

        }else if("add".equals(command)){
            Utils.MakeToast(EquipmentControlActivity1.this,"记录成功");
            goneBottomView();
        }else if("dataset".equals(command)){
            dataSet = DataSet.parseDataSet(content);
        }else if("pose".equals(command)){
            JSONObject result = JSON.parseObject(content);
            if(result != null || !result.isEmpty()){
                pose = Pose.parsePose(result.getString("pose"));
                angle.setText(Math.round(pose.getW())+"°");
                position.setText(Math.round(pose.getX())+","+
                        Math.round(pose.getY())+","+
                        Math.round(pose.getZ())+",");
            }
        }else if("device_status".equals(command)){
            DeviceStatus deviceStatus = DeviceStatus.parseDeviceStatus(content);
            setStatus(deviceStatus);
        }
    }

    private RelativeLayout mSetView;
    private RelativeLayout mMarkView;

    /**
     * 点击设置和记录该点时弹出底部隐藏view
     * @param layoutId
     */
    public void showSetView(int layoutId){
        Utils.hiddenKeyBoard(mContext);
        if(mSetView == null){//为空则初始化控件
            setViewStub.setLayoutResource(layoutId);
            mSetView = (RelativeLayout) setViewStub.inflate();
            mSetView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
        }else{//显示或隐藏View
            mSetView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
            mSetView.setVisibility(View.VISIBLE);
        }
    }

    public void showMarkView(int layoutId){
        Utils.hiddenKeyBoard(mContext);
        if(mMarkView == null){//为空则初始化控件
            markViewStub.setLayoutResource(layoutId);
            mMarkView = (RelativeLayout) markViewStub.inflate();
            mMarkView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
        }else{//显示或隐藏View
            mMarkView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_in));
            mMarkView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 隐藏底部弹出的view
     * @return
     */
    public boolean goneBottomView(){
        if(mSetView != null && mSetView.getVisibility() == View.VISIBLE){
            mSetView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_out));
            mSetView.setVisibility(View.GONE);
            return true;
        }
        if(mMarkView != null && mMarkView.getVisibility() == View.VISIBLE){
            mMarkView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_out));
            mMarkView.setVisibility(View.GONE);
            return true;
        }

        return false;
    }


    private String speedPercent[] = new String[] { "20%", "50%", "100%"};
    private int speedWhich = 2;

    /**
     * 显示选择
     */
    public void showSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择速度");
        // 选择下标
        builder.setSingleChoiceItems(speedPercent, speedWhich,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        speedWhich = which;
                        speed.setText(speedPercent[which]);
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }


    /**
     * 为页面上所有的按钮添加点击事件
     */
    private void setClickListener(){
        openBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        upBtn.setOnClickListener(this);
        downBtn.setOnClickListener(this);
        restoreBtn.setOnClickListener(this);
        setBtn.setOnClickListener(this);
        markBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        openBtn.setOnLongClickListener(this);
        closeBtn.setOnLongClickListener(this);
        upBtn.setOnLongClickListener(this);
        downBtn.setOnLongClickListener(this);
        backImg.setOnClickListener(this);

        openBtn.setOnTouchListener(this);
        closeBtn.setOnTouchListener(this);
        upBtn.setOnTouchListener(this);
        downBtn.setOnTouchListener(this);
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

        //右边点击事件
        rightBtnFront.setOnClickListener(this);
        rightBtnFront.setOnLongClickListener(this);
        rightBtnFront.setOnTouchListener(this);

        rightBtnBack.setOnClickListener(this);
        rightBtnBack.setOnLongClickListener(this);
        rightBtnBack.setOnTouchListener(this);

        rightBtnLeft.setOnClickListener(this);
        rightBtnLeft.setOnLongClickListener(this);
        rightBtnLeft.setOnTouchListener(this);

        rightBtnRight.setOnClickListener(this);
        rightBtnRight.setOnLongClickListener(this);
        rightBtnRight.setOnTouchListener(this);

        rightNortheast.setOnClickListener(this);
        rightNortheast.setOnLongClickListener(this);
        rightNortheast.setOnTouchListener(this);

        rightNorthwest.setOnClickListener(this);
        rightNorthwest.setOnLongClickListener(this);
        rightNorthwest.setOnTouchListener(this);

        rightSoutheast.setOnClickListener(this);
        rightSoutheast.setOnLongClickListener(this);
        rightSoutheast.setOnTouchListener(this);

        rightSouthwest.setOnClickListener(this);
        rightSouthwest.setOnLongClickListener(this);
        rightSouthwest.setOnTouchListener(this);

        linkStatus.setOnClickListener(this);
    }


}
