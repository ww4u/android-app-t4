package com.megarobo.control.mqtt;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.R;
import com.megarobo.control.activity.BaseActivity;
import com.megarobo.control.utils.Utils;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class VoiceActivity extends BaseActivity {

    @ViewInject(R.id.connectBtn)
    private TextView connectBtn;

    @ViewInject(R.id.voiceControlBtn)
    private TextView voiceControlBtn;

    @ViewInject(R.id.info)
    private TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);
        ViewUtils.inject(this);
        EventBus.getDefault().register(this);

        //点击录音图标开始，发送转换成文字，并将文字变成指令
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MQTTService.publish(MQTTCommandHelper.getInstance().home());
            }
        });


        voiceControlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMqttMessage(MQTTMessage mqttMessage){
        Log.i(MQTTService.TAG,"get message:"+mqttMessage.getMessage());
        Toast.makeText(this,mqttMessage.getMessage(),Toast.LENGTH_SHORT).show();

        //获取消息，根据消息进行区分，是服务端还是客户端发的，然后进行相应处理
        String message = mqttMessage.getMessage();
        if(Utils.isNotEmptyString(message)){
            if(message.indexOf("s") == 2){//服务器指令
                String result = message.substring(4,message.length());
                Toast.makeText(this,result,Toast.LENGTH_SHORT).show();
                if("start".equals(result)){
                    //显示运动中，回复收到

                }else if("end".equals(result)){
                    //运动结束，回复完毕

                }

            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}
