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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.activity.BaseActivity;
import com.megarobo.control.activity.EquipmentActivity;
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

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.text.SimpleDateFormat;


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
public class MyProgramActivity extends BaseActivity implements View.OnClickListener{

    private Context mContext;

    @ViewInject(R.id.back)
    private ImageView backImg;

    @ViewInject(R.id.program_name)
    private TextView programName;

    @ViewInject(R.id.last_modify_time)
    private TextView lastModifyTime;

    @ViewInject(R.id.continue_program)
    private TextView continueProgram;

    @ViewInject(R.id.program_layout)
    private LinearLayout programLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_program);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ViewUtils.inject(this);
        mContext = this;

        if(getFile(PythonActivity.SAVE_FILENAME).exists()){
            programLayout.setVisibility(View.VISIBLE);
        }else{
            programLayout.setVisibility(View.INVISIBLE);
        }

        setClickListener();
        lastModifyTime.setText(getLastModifiedTime(PythonActivity.SAVE_FILENAME));
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * TODO
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.back:
                onBackPressed();
                break;

            case R.id.continue_program:
                Intent intent = new Intent(mContext, PythonActivity.class);
                intent.putExtra("type",PythonActivity.INTENT_TYPE_MYPROGRAM);
                startActivity(intent);
                break;
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    /**
     * 为页面上所有的按钮添加点击事件
     */
    private void setClickListener(){
        backImg.setOnClickListener(this);
        continueProgram.setOnClickListener(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private File getFile(String fileName){
        String path = "/data/data/"+getPackageName()+"/files/";

        File file = new File(path+fileName);

        return file;
    }


    private String getLastModifiedTime(String fileName){

        File file = getFile(fileName);
        //Date time=new Date(f.lastModified());//两种方法都可以
        if(file.exists()){//喜欢的话可以判断一下。。。
            System.out.println("File Exist.");
        }
        long time=file.lastModified();
        SimpleDateFormat formatter = new
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String result=formatter.format(time);
        return result;
    }
}
