package com.megarobo.control.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;
import com.megarobo.control.R;

public class EquipmentStatusActivity extends BaseActivity {


    @ViewInject(R.id.restoreBtn)
    private Button restoreBtn;

    private Handler handler;
    private SocketClientManager queryClient;
    private SocketClientManager controlClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment_status);
        ViewUtils.inject(this);

        initHandler();

        controlClient = new SocketClientManager(MegaApplication.ip,
                handler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();



        // TODO 获取设备信息，然后进度条转完之后刷新页面数据

        //异步请求设备信息，获取完毕后刷新界面
//        getEquipmentInfo();

        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("package"));
                //判断是否连接中

//                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("package"));
                controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("meta"));


            }
        });
    }

    @SuppressLint("HandlerLeak")
    private void initHandler(){
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ConstantUtil.SOCKET_CONNECTED:
                        String data = (String) msg.obj;
                        Utils.MakeToast(EquipmentStatusActivity.this,data);
//                        controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("package"));
                        controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("meta"));
//                        controlClient.joinRoom();
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        Bundle bundle = msg.getData();
                        String content = bundle.getString("content");
                        Utils.MakeToast(EquipmentStatusActivity.this,content);
//                        Utils.MakeToast(ConnectActivity.this,content);
                        break;
                }

            }
        };
    }



    private void getEquipmentInfo() {
        queryClient = new SocketClientManager(ConstantUtil.HOST,handler,ConstantUtil.QUERY_PORT);
        queryClient.connectToServer();
        //发送获取设备信息指令
    }

}
