package com.megarobo.control.activity;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.megarobo.control.utils.Utils;
import com.megarobo.control.R;

public class EquipmentStatusActivity extends BaseActivity {


    @ViewInject(R.id.restoreBtn)
    private TextView restoreBtn;

    @ViewInject(R.id.refresh)
    private TextView refreshBtn;

    @ViewInject(R.id.back)
    private ImageView backImg;

    private Handler handler;
    private SocketClientManager controlClient;
    private Context mContext;

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


        //发起恢复出厂姿态请求
        restoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.customDialog(mContext, "此操作不可逆", new Utils.DialogListenner() {
                    @Override
                    public void confirm() {

                    }
                },"确定将机器恢复出厂姿势吗");
                if(Utils.isNotEmptyString(MegaApplication.ip)){
                    controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("package"));
                }
            }
        });

        //刷新按钮，重新发起获取机器参数接口请求
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utils.isNotEmptyString(MegaApplication.ip)) {
                    controlClient.sendMsgToServer(CommandHelper.getInstance().queryCommand("parameter"));
                }
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

                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        Bundle bundle = msg.getData();
                        String content = bundle.getString("content");
                        String command = bundle.getString("command");
                        if("parameter".equals(command)){
                            Utils.MakeToast(EquipmentStatusActivity.this,content);
                            Parameter parameter = Parameter.parseParameter(content);
                        }else if("package".equals(command)){
                            Utils.MakeToast(EquipmentStatusActivity.this,"恢复出厂设置成功");
                        }

                        break;
                }

            }
        };
    }


}
