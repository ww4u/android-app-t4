package com.megarobo.control.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.custom.widgt.jazzyviewpager.Util;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.bean.Config;
import com.megarobo.control.bean.DataSet;
import com.megarobo.control.bean.LinkStatus;
import com.megarobo.control.bean.Point;
import com.megarobo.control.bean.Pose;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;
import com.megarobo.control.view.DiscView;
import com.megarobo.control.R;
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
public class EquipmentControlActivity extends BaseActivity implements View.OnClickListener,View.OnLongClickListener,View.OnTouchListener{

    private TextView textView;
    private MyBroadCastReceiver receiver;
    private LocalBroadcastManager manager;
    private DiscView leftControlView;
    private RightControl rightControlView;

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

    @ViewInject(R.id.angle)
    private TextView angle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_control);
        ViewUtils.inject(this);
        mContext = this;

        leftControlView = findViewById(R.id.leftControlView);
        rightControlView = findViewById(R.id.rightControlView);
        textView = findViewById(R.id.postion);

        setClickListener();

        initHandler();
        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();

        /**
         * 注册本地广播接收器就，接收广播
         **/
        manager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(DiscView.DISC_BROADCAST);
        receiver = new MyBroadCastReceiver();
        manager.registerReceiver(receiver,filter);

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
                Utils.MakeToast(EquipmentControlActivity.this,"开");
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,false,4));
                break;
            case R.id.close_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"闭");
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,false,4));
                break;
            case R.id.up_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"上");
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,1,false));
                break;
            case R.id.down_btn:
                Utils.MakeToast(EquipmentControlActivity.this,"下");
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

        }
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
                Utils.MakeToast(EquipmentControlActivity.this,"确定");

                if(dataSet!=null && dataSet.getPointMap() != null &&
                        dataSet.getPointMap().containsKey(name.getText().toString())){
                    controlClient.sendMsgToServer(CommandHelper.getInstance().addPointCommand(point));
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
                    Utils.MakeToast(EquipmentControlActivity.this,"步距和开合步距不能为空");
                    return;
                }
                speedNum = parseSpeed(speed.getText().toString());
                step = Double.parseDouble(stepEdit.getText().toString());
                jointStep = Double.parseDouble(jointStepEdit.getText().toString());
                controlClient.sendMsgToServer(CommandHelper.getInstance().configCommand(step,jointStep,speedNum));
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
                Utils.MakeToast(EquipmentControlActivity.this, "开开开开开开");
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,true,4));
                break;
            case R.id.close_btn:
                Utils.MakeToast(EquipmentControlActivity.this, "闭闭闭闭");
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,true,4));
                break;
            case R.id.up_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,1,true));
                break;
            case R.id.down_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,-1,true));
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
                if(event.getAction() == MotionEvent.ACTION_UP){
                    Utils.MakeToast(EquipmentControlActivity.this, "停止运行");
                    controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("stop"));
                }
                break;
            case R.id.leftControlView://控制左侧圆形按钮的操作
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        System.out.println(event.getX() + " " + event.getY()+"angle"+leftControlView.getAngle());
                        leftControlView.sendAction();//实时更新页面上的角度信息
                        leftControlView.setBtnX((int) event.getX());
                        leftControlView.setBtnY((int) event.getY());
                        if (leftControlView.isRevoke()){
                            leftControlView.changeBtnLocation();
                        }
                        leftControlView.invalidate();
                        controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(leftControlView.getAngle(),false,3));
                        break;
                    case MotionEvent.ACTION_UP:
                        System.out.println(event.getX() + " " + event.getY() + "TAG angle"+leftControlView.getAngle());
                        leftControlView.sendAction();//实时更新页面上的角度信息
                        leftControlView.setBtnX(0);
                        leftControlView.setBtnY(0);
                        leftControlView.invalidate();
                        break;
                }
                break;
            case R.id.rightControlView:
                switch (event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        System.out.println(event.getX() + " " + event.getY()+"rangle"+rightControlView.getAngle());
                        rightControlView.setBtnX((int) event.getX());
                        rightControlView.setBtnY((int) event.getY());
//                        if (rightControlView.isRevoke()){
//                            rightControlView.changeBtnLocation();
//                        }
                        rightControlView.invalidate();
                        controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(rightControlView.getAngle(),0,true));
                        break;
                    case MotionEvent.ACTION_UP:
                        System.out.println(event.getX() + " " + event.getY() + "rTAG rangle"+rightControlView.getAngle());
                        controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("stop"));
//                        rightControlView.setBtnX(0);
//                        rightControlView.setBtnY(0);
                        rightControlView.invalidate();
                        break;
                }
                break;
        }
        return false;
    }

    DataSet dataSet;
    Point point;
    /**
     * TODO 获取用户设置的点
     * @return
     */
    private Point getPoint() {
        point = new Point();

        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("pose"));

        point.setName("test");

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
            Config config = Config.parseConfig(content);
            Utils.MakeToast(EquipmentControlActivity.this,config.getSpeed()+"");
        }else if("package".equals(command)){

        }else if("link_status".equals(command)){
            LinkStatus linkStatus = LinkStatus.parseLinkStatus(content);
            Utils.MakeToast(EquipmentControlActivity.this,linkStatus.getStatus()+"");
        }else if("add".equals(command)){

        }else if("dataset".equals(command)){
            dataSet = DataSet.parseDataSet(content);
        }else if("pose".equals(command)){
            Pose pose = Pose.parsePose(content);
            point.setPose(pose);
            //设置界面上的点位置。

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
        leftControlView.setOnTouchListener(this);
        rightControlView.setOnTouchListener(this);
    }


    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String angleStr = intent.getStringExtra("angle");
            angle.setText(angleStr);
        }
    }

}
