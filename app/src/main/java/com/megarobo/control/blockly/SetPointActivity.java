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
import android.widget.ListView;
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
import com.megarobo.control.adapter.PointListAdapter;
import com.megarobo.control.bean.Config;
import com.megarobo.control.bean.DataSet;
import com.megarobo.control.bean.DeviceStatus;
import com.megarobo.control.bean.LinkStatus;
import com.megarobo.control.bean.Parameter;
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
public class SetPointActivity extends BaseActivity implements View.OnClickListener,View.OnLongClickListener,View.OnTouchListener{

    private Handler handler;
    private SocketClientManager controlClient;

    private Context mContext;

    @ViewInject(R.id.up_btn)
    private TextView upBtn;

    @ViewInject(R.id.down_btn)
    private TextView downBtn;

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

    @ViewInject(R.id.speed)
    private TextView speed;

    @ViewInject(R.id.step)
    private TextView stepEdit;

    @ViewInject(R.id.route)
    private TextView route;

    @ViewInject(R.id.position)
    private TextView position;

    @ViewInject(R.id.end_point)
    private TextView endPosition;

    @ViewInject(R.id.confirm)
    private TextView confirm;

    @ViewInject(R.id.cancel)
    private TextView cancel;

    @ViewInject(R.id.choose)
    private TextView choose;

    @ViewInject(R.id.mark_view_stub)
    private ViewStub markViewStub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.e("setPointActivity","oncreate.............");
        setContentView(R.layout.activity_set_point);
        ViewUtils.inject(this);
        mContext = this;

        setClickListener();

        initHandler();
        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();

        route.setText(routeStr[routeWhich]);
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
            case R.id.up_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,1,false));
                break;
            case R.id.down_btn:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,-1,false));
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
            //前后左右
            case R.id.right_btn_front:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(90,0,false));
                break;
            case R.id.right_btn_back:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(270,0,false));
                break;
            case R.id.right_btn_left:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(180,0,false));
                break;
            case R.id.right_btn_right:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,0,false));
                break;

            case R.id.right_northeast:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(45,0,false));
                break;
            case R.id.right_northwest:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(135,0,false));
                break;
            case R.id.right_southeast:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(315,0,false));
                break;
            case R.id.right_southwest:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(225,0,false));
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
                intent1.putExtra("x", Utils.format(pose.getX()) + "");
                intent1.putExtra("y", Utils.format(pose.getY()) + "");
                intent1.putExtra("z", Utils.format(pose.getZ()) + "");
                intent1.putExtra("speed",speedPercent[speedWhich]+"");
                intent1.putExtra("route",routeStr[routeWhich]+"");
                setResult(RESULT_OK,intent1);
                finish();
                break;
            case R.id.cancel:
                finish();
                break;
            case R.id.choose:
                doChooseAction();
                break;
            case R.id.speed:
                showSelectDialog(speed);
                break;
            case R.id.step:
                showSelectDialog(stepEdit);
                break;
            case R.id.route:
                showRouteDialog(route);
                break;



        }
    }

    private String generateCode() {
        String code = "move({'x':"
                + Utils.format(pose.getX())+",'y':"
                + Utils.format(pose.getY())+",'z':"
                + Utils.format(pose.getZ())+"})";
        return code;
    }


    private RelativeLayout mMarkView;

    private ImageView markClose;
    private ListView pointList;

    private PointListAdapter adapter;

    /**
     * 处理记录该点逻辑
     */
    private void doChooseAction() {
        showMarkView(R.layout.choose_layout);
        markClose = findViewById(R.id.mark_close);
        pointList = findViewById(R.id.point_list);

        adapter = new PointListAdapter(SetPointActivity.this,DataSet.parseList(dataSet.getPointMap()));
        pointList.setAdapter(adapter);


        markClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goneBottomView();
            }
        });


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
        if(mMarkView != null && mMarkView.getVisibility() == View.VISIBLE){
            mMarkView.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.push_bottom_out));
            mMarkView.setVisibility(View.GONE);
            return true;
        }

        return false;
    }


    Config config;
    private void initSpeed() {
        if(config != null) {
            Logger.e("config","speed:"+config.getSpeed()+"step:"+config.getStep()+"jointStep:"+config.getJointStep());
            speed.setText(config.getSpeed() + "");
//            speed.setSelection(speed.getText().length());
            speedWhich = getSpeedwhich(config.getSpeed());
            stepEdit.setText(config.getStep() + "");
//            stepEdit.setSelection(stepEdit.getText().length());
        }
    }

    private int getSpeedwhich(double speedPercent){
        if(1 == speedPercent){
            return 0;
        }else if(20 == speedPercent){
            return 1;
        }else if(50 == speedPercent){
            return 2;
        }else if(100 == speedPercent){
            return 3;
        }
        return 2;
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
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(1,true,3));
                break;
            case R.id.counterclockwise:
                controlClient.sendMsgToServer(CommandHelper.getInstance().jointCommand(-1,true,3));
                break;

            //前后左右
            case R.id.right_btn_front:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(90,0,true));
                break;
            case R.id.right_btn_back:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(270,0,true));
                break;
            case R.id.right_btn_left:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(180,0,true));
                break;
            case R.id.right_btn_right:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(0,0,true));
                break;

            //四个角度
            case R.id.right_northeast:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(45,0,true));
                break;
            case R.id.right_northwest:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(135,0,true));
                break;
            case R.id.right_southeast:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(315,0,true));
                break;
            case R.id.right_southwest:
                controlClient.sendMsgToServer(CommandHelper.getInstance().stepCommand(225,0,true));
                break;

//            case R.id.route:
//                showRouteDialog(route);
//                break;
//            case R.id.speed:
//                showSelectDialog(speed);
//                break;
//            case R.id.step:
//                showSelectDialog(stepEdit);
//                break;

        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.up_btn:
            case R.id.down_btn:


            case R.id.right_btn_back:
            case R.id.right_btn_front:
            case R.id.right_btn_left:
            case R.id.right_btn_right:

            case R.id.right_northeast:
            case R.id.right_northwest:
            case R.id.right_southeast:
            case R.id.right_southwest:

            if(event.getAction() == MotionEvent.ACTION_UP ||
                    event.getAction() == MotionEvent.ACTION_CANCEL){
                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("stop"));
            }
            break;
        }
        return false;
    }

    DataSet dataSet = new DataSet();
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
                            //点击先弹出页面展示已经保存的点位置
                            controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("dataset"));
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
        if(mMarkView!=null && mMarkView.isShown()){
            goneBottomView();
            return;
        }
        super.onBackPressed();
    }

    private boolean isInit = true;
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
                String positionStr = Utils.format(pose.getX())+","+
                        Utils.format(pose.getY())+","+
                        Utils.format(pose.getZ());
                if(isInit){
                    position.setText(positionStr);
                    isInit = false;
                }
                endPosition.setText(positionStr);
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

    public void setEndPosition(Pose pose){
        String positionStr = Utils.format(pose.getX())+","+
                Utils.format(pose.getY())+","+
                Utils.format(pose.getZ());
        endPosition.setText(positionStr);
        this.pose = pose;
    }


    private String speedPercent[] = new String[] { "1","20", "50", "100"};
    private int speedWhich = 2;

    /**
     * 显示选择
     */
    public void showSelectDialog(final TextView editText) {
        editText.requestFocus();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择：");
        // 选择下标
        builder.setSingleChoiceItems(speedPercent, speedWhich,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        speedWhich = which;
                        editText.setText(speedPercent[which]);
//                        editText.setSelection(editText.getText().length());
                        dialog.cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    private String routeStr[] = new String[] { "自由","直线"};
    private int routeWhich = 0;

    /**
     * 显示选择
     */
    public void showRouteDialog(final TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("请选择：");
        // 选择下标
        builder.setSingleChoiceItems(routeStr, routeWhich,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        routeWhich = which;
                        textView.setText(routeStr[which]);
                        dialog.cancel();
                    }
                });
        Dialog dialog = builder.create();
        dialog.show();
    }


    /**
     * 为页面上所有的按钮添加点击事件
     */
    private void setClickListener(){
        upBtn.setOnClickListener(this);
        downBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        upBtn.setOnLongClickListener(this);
        downBtn.setOnLongClickListener(this);
        backImg.setOnClickListener(this);

        upBtn.setOnTouchListener(this);
        downBtn.setOnTouchListener(this);

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

        speed.setOnClickListener(this);
        stepEdit.setOnClickListener(this);
        route.setOnClickListener(this);

//        endPosition.setKeyListener(null);

        confirm.setOnClickListener(this);
        cancel.setOnClickListener(this);
        choose.setOnClickListener(this);
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
