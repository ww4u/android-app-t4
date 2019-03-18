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

public class EquipmentActivity extends BaseActivity {


    @ViewInject(R.id.switchBtn)
    private Button switchBtn;

    @ViewInject(R.id.remoteControlBtn)
    private Button remoteControlBtn;

    @ViewInject(R.id.info)
    private ImageView info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        ViewUtils.inject(this);


        switchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EquipmentActivity.this, ConnectActivity.class);
                startActivity(intent);
            }
        });

        remoteControlBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EquipmentActivity.this, EquipmentControlActivity.class);
                startActivity(intent);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EquipmentActivity.this, EquipmentStatusActivity.class);
                startActivity(intent);
            }
        });

    }



}
