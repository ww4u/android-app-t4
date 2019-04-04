package com.megarobo.control.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.R;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;

public class EquipmentActivity extends BaseActivity implements View.OnClickListener{


    @ViewInject(R.id.switchBtn)
    private TextView switchBtn;

    @ViewInject(R.id.remoteControlBtn)
    private TextView remoteControlBtn;

    @ViewInject(R.id.back)
    private ImageView backImg;

//    @ViewInject(R.id.info)
//    private ImageView info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        ViewUtils.inject(this);


        backImg.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        remoteControlBtn.setOnClickListener(this);

//        info.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(EquipmentActivity.this, EquipmentStatusActivity.class);
//                startActivity(intent);
//            }
//        });

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
                Intent intent1 = new Intent(EquipmentActivity.this, EquipmentControlActivity.class);
                startActivity(intent1);
                break;

        }
    }
}
