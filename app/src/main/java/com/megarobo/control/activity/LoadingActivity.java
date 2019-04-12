package com.megarobo.control.activity;

import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;

import com.lidroid.xutils.ViewUtils;
import com.megarobo.control.R;


public class LoadingActivity extends BaseActivity {


    private Handler handler = new Handler();


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        ViewUtils.inject(this);

        //加载首页
        playLoading();

    }

    /**
     * 加载loading画面
     */
    private void playLoading() {
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                Intent intent = new Intent(LoadingActivity.this, ConnectActivity.class);
                startActivity(intent);
                LoadingActivity.this.finish();
            }
        }, 2000);
    }


}
