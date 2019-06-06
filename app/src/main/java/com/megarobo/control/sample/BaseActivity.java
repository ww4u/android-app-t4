package com.megarobo.control.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.R;

import java.util.ArrayList;


/**
 * Created by fujiayi on 2017/9/13.
 * <p>
 * 此类 底层UI实现 无SDK相关逻辑
 */

public class BaseActivity extends AppCompatActivity implements MainHandlerConstant {
    protected Button mSpeak;
    protected Button mPause;
    protected Button mResume;
    protected Button mStop;
    protected Button mSynthesize;
    protected Button mBatchSpeak;
    protected Button mLoadModel;
    protected Button[] buttons;
    protected TextView mHelp;
    protected EditText mInput;
    protected TextView mShowText;
    protected TextView mShowTips;
    protected Handler mainHandler;
    protected ImageView backImg;

    private static final String TAG = "MainActivity";

    protected static String DESC = "欢迎使用小镁机器人语音控制功能，点击“开始录音”按钮即可说话。。\n"
            + "小镁小镁，"
            + "向前走，向后走，左转，右转，向上，向下，回零位，恢复出厂设置，抓取，放开，停止.\n\n"
            + "我的流式样本加样完毕了吗？\n\n"
            + "把PCR混液分到96孔板中，每孔20uL\n\n";

    /*
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synth);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handle(msg);
            }

        };
        initialView(); // 初始化UI
        initPermission(); // android 6.0以上动态权限申请

        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity.super.onBackPressed();
            }
        });
    }


    private void initialView() {
        mSpeak = (Button) this.findViewById(R.id.speak);
        mPause = (Button) this.findViewById(R.id.pause);
        mResume = (Button) this.findViewById(R.id.resume);
        mStop = (Button) this.findViewById(R.id.stop);
        mSynthesize = (Button) this.findViewById(R.id.synthesize);
        mBatchSpeak = (Button) this.findViewById(R.id.batchSpeak);
        mLoadModel = (Button) this.findViewById(R.id.loadModel);
        buttons = new Button[]{
                mSpeak, mPause, mResume, mStop, mSynthesize, mBatchSpeak, mLoadModel
        };
        mHelp = (TextView) this.findViewById(R.id.help);
        mInput = (EditText) this.findViewById(R.id.input);
        mShowText = (TextView) this.findViewById(R.id.showText);
        mShowText.setMovementMethod(new ScrollingMovementMethod());

        mShowTips = (TextView) this.findViewById(R.id.showTips);
        mShowTips.setText(DESC);
        backImg = (ImageView) this.findViewById(R.id.back);
    }

    protected void handle(Message msg) {
        int what = msg.what;
        switch (what) {
            case PRINT:
                print(msg);
                break;
            case UI_CHANGE_INPUT_TEXT_SELECTION:
                if (msg.arg1 <= mInput.getText().length()) {
                    mInput.setSelection(0, msg.arg1);
                }
                break;
            case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                SpannableString colorfulText = new SpannableString(mInput.getText().toString());
                if (msg.arg1 <= colorfulText.toString().length()) {
                    colorfulText.setSpan(new ForegroundColorSpan(Color.WHITE), 0, msg.arg1, Spannable
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                    mInput.setText(colorfulText);
                }
                break;
            default:
                break;
        }
    }

    protected void toPrint(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        mainHandler.sendMessage(msg);
    }

    private void print(Message msg) {
        String message = (String) msg.obj;
        if (message != null) {
            scrollLog(message);
        }
    }

    private void scrollLog(String message) {
        Spannable colorMessage = new SpannableString(message + "\n");
        colorMessage.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.yellow)), 0, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mShowText.append(colorMessage);
        Layout layout = mShowText.getLayout();
        if (layout != null) {
            int scrollAmount = layout.getLineTop(mShowText.getLineCount()) - mShowText.getHeight();
            if (scrollAmount > 0) {
                mShowText.scrollTo(0, scrollAmount + mShowText.getCompoundPaddingBottom());
            } else {
                mShowText.scrollTo(0, 0);
            }
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO // 加这一行
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }
}
