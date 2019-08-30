/*
 *  Copyright 2017 Google Inc. All Rights Reserved.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.megarobo.control.blockly;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.BlocklyActivityHelper;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.codegen.LanguageDefinition;
import com.google.blockly.android.control.BlocklyController;
import com.google.blockly.android.ui.BlockView;
import com.google.blockly.android.ui.TrashCanView;
import com.google.blockly.model.Block;
import com.google.blockly.model.BlocklySerializerException;
import com.google.blockly.model.DefaultBlocks;
import com.google.blockly.utils.BlockLoadingException;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.activity.EquipmentStatusActivity;
import com.megarobo.control.bean.DeviceStatus;
import com.megarobo.control.blockly.bean.BlocklyFile;
import com.megarobo.control.blockly.bean.Code;
import com.megarobo.control.blockly.bean.ForCode;
import com.megarobo.control.blockly.bean.MoveCode;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.NetBroadcastReceiver;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.NetUtil;
import com.megarobo.control.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Demo activity that programmatically adds a view to split the screen between the Blockly workspace
 * and an arbitrary other view or fragment and generates Python code.
 */
public class PythonActivity extends AbstractBlocklyActivity implements NetBroadcastReceiver.NetChangeListener,
        View.OnClickListener ,RadioGroup.OnCheckedChangeListener {

    @ViewInject(R.id.stop)
    private TextView stopBtn;

    @ViewInject(R.id.back)
    private ImageView backImg;

    @ViewInject(R.id.link_status)
    private TextView linkStatus;

    @ViewInject(R.id.net_status)
    private TextView netStatus;

    @ViewInject(R.id.save)
    private TextView save;

    @ViewInject(R.id.upload)
    private TextView upload;

    @ViewInject(R.id.save_and_play)
    private TextView saveAndPlay;

    @ViewInject(R.id.repeat)
    private TextView blockBtnRepeat;

    @ViewInject(R.id.move)
    private TextView blockBtnMove;

    @ViewInject(R.id.hand)
    private TextView blockBtnHand;

    @ViewInject(R.id.wrist)
    private TextView blockBtnWrist;

    @ViewInject(R.id.delay)
    private TextView blockBtnDelay;

    @ViewInject(R.id.wait_io)
    private TextView blockBtnWaitIO;

    @ViewInject(R.id.output_io)
    private TextView blockBtnOutputIO;

    @ViewInject(R.id.block_button_layout)
    private LinearLayout blockBtnLayout;

    @ViewInject(R.id.play_layout)
    private LinearLayout playLayout;

    @ViewInject(R.id.above_cover)
    private View aboveCover;

    @ViewInject(R.id.blockly_trash_icon)
    private TrashCanView trashCanView;

    @ViewInject(R.id.play_type)
    private RadioGroup playTypeGroup;

    @ViewInject(R.id.play_all)
    private RadioButton playAll;

    @ViewInject(R.id.play_step)
    private RadioButton playStep;

    @ViewInject(R.id.play)
    private TextView play;

    @ViewInject(R.id.next)
    private TextView next;

    @ViewInject(R.id.speed_input)
    private TextView speedInput;

    @ViewInject(R.id.download)
    private TextView download;

    private static final String TAG = "PythonActivity";

    public static final int INTENT_TYPE_SIMPLE = 0;
    public static final int INTENT_TYPE_CUSTOM = 1;
    public static final int INTENT_TYPE_MYPROGRAM = 2;

    public static final String SAVE_FILENAME = "python_workspace.xml";
    public static final String AUTOSAVE_FILENAME = "python_workspace_temp.xml";
    // Add custom blocks to this list.
    private static final List<String> BLOCK_DEFINITIONS = DefaultBlocks.getAllBlockDefinitions();
    private static final List<String> PYTHON_GENERATORS = Arrays.asList();

    private static final LanguageDefinition PYTHON_LANGUAGE_DEF
            = LanguageDefinition.PYTHON_LANGUAGE_DEFINITION;

    private TextView mGeneratedTextView;
    private Handler mHandler;

    private String mNoCodeText;

    private SocketClientManager controlClient;

    private CodeManager codeManager;

    private BlocklyFile blocklyFile;

    private boolean isPlayingAll = false;

    private boolean isUpload = false;

    CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

//                            controlClient.sendMsgToServer(
//                                    CommandHelper.getInstance().scriptCommand(generatedCode));
//                            mGeneratedTextView.setText(generatedCode);
//                            DemoUtil.updateTextMinWidth(mGeneratedTextView, PythonActivity.this);

                            //开始单步执行
                            codeManager = new CodeManager(generatedCode);
                            codeManager.init();
                            if(check() && isUpload){
                                doUpload();
                            }
                            //同时显示代码区域
//                            if(!mGeneratedTextView.isShown()){
//                                mGeneratedTextView.setVisibility(View.VISIBLE);
//                            }

                        }
                    });
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return TurtleActivity.onDemoItemSelected(item, this) || super.onOptionsItemSelected(item);
    }

    View view;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stop:
                controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("emergency_stop"));
                break;
            case R.id.back:
                onBackPressed();
                break;
            case R.id.link_status:
                Intent intent = new Intent(PythonActivity.this, EquipmentStatusActivity.class);
                startActivity(intent);
                break;
            case R.id.upload:
                //先保存xml，然后再读取xml内容进行上传
                if (getController().getWorkspace().hasBlocks()) {
                    isUpload = true;
                    onRunCode();
                } else {
                    Log.i(TAG, "No blocks in workspace. Skipping run request.");
                }
//                if() {
//                    doUpload();
//                }else{
//                    Utils.MakeToast(PythonActivity.this,"输入有误!");
//                }

                break;
            case R.id.save:
//                if(!mGeneratedTextView.isShown()) {
//                    mGeneratedTextView.setVisibility(View.VISIBLE);
//                }else{
//                    mGeneratedTextView.setVisibility(View.GONE);
//                }
                onSaveWorkspace();
                Utils.MakeToast(PythonActivity.this,"保存成功！");
                break;
            case R.id.save_and_play:
                doUploadAndPlay();
                break;

            case R.id.repeat:
                getController().showBlockByIndex(0);
                break;
            case R.id.move:
                getController().showBlockByIndex(1);
                break;
            case R.id.hand:
                getController().showBlockByIndex(2);
                break;
            case R.id.wrist:
                getController().showBlockByIndex(3);
                break;
            case R.id.delay:
                getController().showBlockByIndex(4);
                break;
            case R.id.wait_io:
                getController().showBlockByIndex(5);
                break;
            case R.id.output_io:
                getController().showBlockByIndex(6);
                break;
            case R.id.play://全部播放
                doPlayAll();
                break;
            case R.id.next://下一步
                doNextPlay();
                break;
            case R.id.download:
                controlClient.sendMsgToServer(CommandHelper.getInstance().readFileCommand("workspace.xml"));
                break;
        }
    }

    /**
     * 上传逻辑
     */
    private void doUpload() {
        if(view != null){
            view.setSelected(false);
        }
        onSaveWorkspace();
        try {
            InputStream xmlStream = PythonActivity.this.openFileInput(getWorkspaceSavePath());
            String xml = Utils.inputStream2String(xmlStream);
            controlClient.sendMsgToServer(CommandHelper.getInstance().writeFileCommand("workspace.xml",xml));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查所有代码
     */
    private boolean check(){
        if(codeManager == null){
            return false;
        }

        Code code = codeManager.check();
        if(code != null){
            setBlockSelected(code);
            Utils.MakeToast(PythonActivity.this,"IO输入有误，请确保格式为形如Y1的端口，信号值为0和1的组合");
            return false;
        }

        return true;
    }

    /**
     * 播放全部代码，需要对代码中的blockId进行处理
     */
    private void doPlayAll() {
        if(!check()){
            onBackPressed();
            return;
        }
        if(view != null){
            view.setSelected(false);
        }
        if(isPlayingAll){

            controlClient.sendMsgToServer(CommandHelper.getInstance().actionCommand("stop"));
            play.setText("播放");
            isPlayingAll = false;

        }else{
            String allCode = codeManager.playAll();
            Logger.e("allCode",allCode);
            if(codeManager != null && controlClient!=null){
                controlClient.sendMsgToServer(
                        CommandHelper.getInstance().scriptCommand(allCode));
            }
            isPlayingAll = true;
            play.setText("停止");
        }

    }

    private void initSpeed() {
        speedInput.setText(speedPercent[speedWhich]);
        speedInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog(speedInput);
            }
        });
        speedInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                controlClient.sendMsgToServer(
                        CommandHelper.getInstance().velCommand(getPlaySpeed()));
            }
        });
    }

    private int getPlaySpeed() {
        int speed = Integer.parseInt(speedInput.getText().toString());
        if(speed<=0 || speed > 100){
            speed = 100;
            Utils.MakeToast(PythonActivity.this,"速度输入有误，以正常速度播放");
        }

        return speed;
    }

    private String speedPercent[] = new String[] {"20", "50", "100"};
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

    /**
     * 执行下一步
     */
    private void doNextPlay() {
        if(!check()){
            onBackPressed();
            return;
        }
        if(codeManager != null && controlClient !=null){
            if(view != null) {
                view.setSelected(false);
            }
            Code code = codeManager.step();
            if(code == null){
                Utils.MakeToast(PythonActivity.this,"已经执行完毕");
                return;
            }
            controlClient.sendMsgToServer(
                    CommandHelper.getInstance().scriptCommand(code.getContent().trim()));
//            Utils.MakeToast(PythonActivity.this,code.getContent());
            setBlockSelected(code);
        }
    }

    /**
     * 根据code选中block
     * @param code
     */
    private void setBlockSelected(Code code) {
        Block block = getController()
                .getBlockFactory().getBlockViewById(code.getBlockId());
        Logger.e("....blockId",code.getBlockId());
        BlockView blockView1 = getController()
                .getBlockViewFactory().getView(block);
        view = (View) blockView1;
        view.setSelected(true);
    }

    boolean isPlay = false;
    private void doUploadAndPlay() {
        isPlay = true;
        //1.7个按钮消失，垃圾箱消失，保存上传消失，紧急停止出现
        blockBtnLayout.setVisibility(View.GONE);
        save.setVisibility(View.GONE);
        upload.setVisibility(View.GONE);
        download.setVisibility(View.GONE);
        saveAndPlay.setVisibility(View.GONE);
        stopBtn.setVisibility(View.VISIBLE);
        playLayout.setVisibility(View.VISIBLE);
        aboveCover.setVisibility(View.VISIBLE);
        trashCanView.setVisibility(View.GONE);

        //初始化播放速度
        initSpeed();

        if (getController().getWorkspace().hasBlocks()) {
            onRunCode();
        } else {
            Log.i(TAG, "No blocks in workspace. Skipping run request.");
        }
    }

    private void setClickListener(){
        stopBtn.setOnClickListener(this);
        backImg.setOnClickListener(this);

        linkStatus.setOnClickListener(this);

        save.setOnClickListener(this);
        upload.setOnClickListener(this);
        saveAndPlay.setOnClickListener(this);

        blockBtnRepeat.setOnClickListener(this);
        blockBtnMove.setOnClickListener(this);
        blockBtnHand.setOnClickListener(this);
        blockBtnWrist.setOnClickListener(this);
        blockBtnDelay.setOnClickListener(this);
        blockBtnWaitIO.setOnClickListener(this);
        blockBtnOutputIO.setOnClickListener(this);

        playTypeGroup.setOnCheckedChangeListener(this);
        play.setOnClickListener(this);
        next.setOnClickListener(this);
        download.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.play_all:
                play.setVisibility(View.VISIBLE);
                next.setVisibility(View.GONE);
                break;
            case R.id.play_step:
                next.setVisibility(View.VISIBLE);
                play.setVisibility(View.GONE);
                break;
        }

    }


    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            //根据广播中传过来的block类型，跳转到相应的页面
            if("point".equals(action)){
                //跳转到设置x,y,z页面
                startActivityForResult(new Intent(PythonActivity.this, SetPointActivity.class),
                        ConstantUtil.REQUEST_CODE_POINT);
            }else if("handClose".equals(action)){
                //跳转到设置手爪角度页面
                startActivityForResult(new Intent(PythonActivity.this, SetHandActivity.class),
                        ConstantUtil.REQUEST_CODE_HAND);
            }else if("wrist".equals(action)){
                //跳转到设置手腕角度页面
                startActivityForResult(new Intent(PythonActivity.this, SetWristActivity.class),
                        ConstantUtil.REQUEST_CODE_WRIST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == ConstantUtil.RESULT_CODE_NOTIFY){
            finish();
        }
        switch (requestCode){
            case ConstantUtil.REQUEST_CODE_POINT:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    String x = bundle.getString("x");
                    String y = bundle.getString("y");
                    String z = bundle.getString("z");
                    String speed = bundle.getString("speed");
                    String route = bundle.getString("route");
                    getController().setPoint(x,y,z,speed,route);
                }else{
                    Utils.MakeToast(PythonActivity.this,"未设置坐标");
                }
                break;
            case ConstantUtil.REQUEST_CODE_HAND:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    String angle = bundle.getString("angle");
                    getController().setHand(angle);
                }else{
                    Utils.MakeToast(PythonActivity.this,"未设置手爪角度");
                }
                break;
            case ConstantUtil.REQUEST_CODE_WRIST:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    String angle = bundle.getString("angle");
                    getController().setWrist(angle);

                }else{
                    Utils.MakeToast(PythonActivity.this,"未设置手腕角度");
                }
                break;
        }
    }

    private MyBroadCastReceiver receiver;
    private LocalBroadcastManager manager;

    private NetBroadcastReceiver netBroadcastReceiver;

    /** 页面是从哪个编程页面跳转过来的 0简单搬运编程，1代表自定义界面，2代表我的编程**/
    private int type = 0;
    private String simple = "";
    private String workspaceStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        initWorkspaceByIntentType();

        //隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initHandler();
        setClickListener();

        manager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlocklyController.DISC_BROADCAST);
        receiver = new MyBroadCastReceiver();
        manager.registerReceiver(receiver,filter);

        controlClient = new SocketClientManager(MegaApplication.ip,
                mHandler,ConstantUtil.CONTROL_PORT);
        controlClient.connectToServer();
        MegaApplication.getInstance().controlClient = controlClient;

        //实例化IntentFilter对象
        IntentFilter netFilter = new IntentFilter();
        netFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        netBroadcastReceiver = new NetBroadcastReceiver(this);
        //注册广播接收
        registerReceiver(netBroadcastReceiver, netFilter);

    }

    /**
     * 根据跳转source加载不同的xml文件，同时生成workspaceStr用来判断是否修改
     */
    private void initWorkspaceByIntentType() {
        type = getIntent().getIntExtra("type",0);
        if(type == INTENT_TYPE_CUSTOM){//展示自定义界面
            download.setVisibility(View.VISIBLE);
            onClearWorkspace();
        }else if(type == INTENT_TYPE_MYPROGRAM){ //我的编程
            onLoadWorkspace();
            try {
                InputStream inputStream = PythonActivity.this.openFileInput(getWorkspaceSavePath());
                workspaceStr = Utils.inputStream2String(inputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{//简单搬运编程
            download.setVisibility(View.GONE);
            AssetManager assetManager = getAssets();
            try {
                getController().loadWorkspaceContents(assetManager.open("simple_workspace.xml"));

                simple = Utils.inputStream2String(assetManager.open("simple_workspace.xml"));

            } catch (BlockLoadingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    public void initHandler(){
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ConstantUtil.SOCKET_CONNECTED:
                        Logger.e("pythonActivity","socket_connected");
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        processMsg(msg);
                        break;
                    case ConstantUtil.SOCKET_DISCONNECTED:

                        break;
                }

            }
        };
    }

    private void processMsg(Message msg){
        Bundle bundle = msg.getData();
        String command = bundle.getString("command");
        String content = bundle.getString("content");
        if("notify".equals(command)){
            controlClient.sendMsgToServer(CommandHelper.getInstance().linkCommand(false));
            onBackPressed();
        }else if("file".equals(command)){
            doProcessFileMsg(content);
        }else if("device_status".equals(command)){
            DeviceStatus deviceStatus = DeviceStatus.parseDeviceStatus(content);
            setStatus(deviceStatus);
        }
    }

    /**
     * 处理文件上传下载相关消息
     * @param content
     */
    private void doProcessFileMsg(String content) {
        blocklyFile = BlocklyFile.parseFile(content);
        if(blocklyFile == null){
            return;
        }
        if("read".equals(blocklyFile.getAction())){//下载
            try {
                getController().loadWorkspaceContents(blocklyFile.getContents());
            } catch (BlockLoadingException e) {
                e.printStackTrace();
            }
        }else{//上传
            if(0 == blocklyFile.getRet()) {
                Utils.MakeToast(PythonActivity.this, "上传成功");
            }
        }
    }

    private void setStatus(DeviceStatus deviceStatus) {
        if(deviceStatus.getStatus().equals(DeviceStatus.RUNNING)){
            linkStatus.setText("运动中");
            linkStatus.setTextColor(getResources().getColor(R.color.blue_status));
            playStep.setClickable(false);
            playAll.setClickable(false);
        }else if(deviceStatus.getStatus().equals(DeviceStatus.STOPED)){
            linkStatus.setText("停止");
            linkStatus.setTextColor(getResources().getColor(R.color.blue_status));
            playStep.setClickable(true);
            playAll.setClickable(true);
            if(isPlayingAll){
                play.setText("播放");
                isPlayingAll = false;
            }
        }else if(deviceStatus.getStatus().equals(DeviceStatus.EXCEPTION_STOPED)){
            linkStatus.setText("异常");
            linkStatus.setTextColor(getResources().getColor(R.color.orange));
        }

    }

    @Override
    public void onChange(int networkState) {
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
    protected View onCreateContentView(int parentId) {
        View root = getLayoutInflater().inflate(R.layout.split_content, null);
        mGeneratedTextView = (TextView) root.findViewById(R.id.generated_code);
        DemoUtil.updateTextMinWidth(mGeneratedTextView, this);

        mNoCodeText = mGeneratedTextView.getText().toString(); // Capture initial value.

        return root;
    }

    @Override
    protected int getActionBarMenuResId() {
        return R.menu.split_actionbar;
    }

    @NonNull
    @Override
    protected List<String> getBlockDefinitionsJsonPaths() {
        return BLOCK_DEFINITIONS;
    }

    @NonNull
    @Override
    protected LanguageDefinition getBlockGeneratorLanguage() {
        return PYTHON_LANGUAGE_DEF;
    }

    @NonNull
    @Override
    protected String getToolboxContentsXmlPath() {
        return DefaultBlocks.TOOLBOX_PATH;
    }

    @NonNull
    @Override
    protected List<String> getGeneratorsJsPaths() {
        return PYTHON_GENERATORS;
    }

    @NonNull
    @Override
    protected CodeGenerationRequest.CodeGeneratorCallback getCodeGenerationCallback() {
        // Uses the same callback for every generation call.
        return mCodeGeneratorCallback;
    }

    @Override
    public void onClearWorkspace() {
        super.onClearWorkspace();
        mGeneratedTextView.setText(mNoCodeText);
        DemoUtil.updateTextMinWidth(mGeneratedTextView, this);
    }

    /**
     * Optional override of the save path, since this demo Activity has multiple Blockly
     * configurations.
     * @return Workspace save path used by this Activity.
     */
    @Override
    @NonNull
    protected String getWorkspaceSavePath() {
        return SAVE_FILENAME;
    }

    /**
     * Optional override of the auto-save path, since this demo Activity has multiple Blockly
     * configurations.
     * @return Workspace auto-save path used by this Activity.
     */
    @Override
    @NonNull
    protected String getWorkspaceAutosavePath() {
        return AUTOSAVE_FILENAME;
    }

    @Override
    protected BlocklyActivityHelper onCreateActivityHelper() {
        return super.onCreateActivityHelper();
    }

    private void showViewByPlay(boolean isPlay){
        if(isPlay){
            blockBtnLayout.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            upload.setVisibility(View.GONE);
            download.setVisibility(View.GONE);
            saveAndPlay.setVisibility(View.GONE);
            stopBtn.setVisibility(View.VISIBLE);
            playLayout.setVisibility(View.VISIBLE);
            aboveCover.setVisibility(View.VISIBLE);
            trashCanView.setVisibility(View.GONE);
        }else{
            download.setVisibility(View.VISIBLE);
            blockBtnLayout.setVisibility(View.VISIBLE);
            save.setVisibility(View.VISIBLE);
            upload.setVisibility(View.VISIBLE);
            saveAndPlay.setVisibility(View.VISIBLE);
            stopBtn.setVisibility(View.GONE);
            playLayout.setVisibility(View.GONE);
            aboveCover.setVisibility(View.GONE);
            trashCanView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onBackPressed() {
        if(isPlay){
            showViewByPlay(false);
            isPlay = false;
            return;
        }

        ByteArrayOutputStream  outputStream   =   new   ByteArrayOutputStream();
        try {
            getController().getWorkspace().serializeToXml(outputStream);
        } catch (BlocklySerializerException e) {
            e.printStackTrace();
        }
        //有修改
        String result = outputStream.toString().trim();
        Logger.e("result",result);
        if(type == INTENT_TYPE_SIMPLE && !simple.trim().equals(result)){//比较simple.xml内容是否发生改)
            doSave();
            return;
        }

        if(type == INTENT_TYPE_CUSTOM && getController().getWorkspace().hasBlocks()){
            doSave();
            return;
        }

        if(type == INTENT_TYPE_MYPROGRAM && !workspaceStr.trim().equals(result)){//比较已经保存的内容是否被修改
            doSave();
            return;
        }

        super.onBackPressed();
    }

    /**
     * 提示是否保存
     */
    private void doSave() {
        Utils.customDialog(PythonActivity.this, "是否保存修改", new Utils.DialogListenner() {
            @Override
            public void confirm() {
                onSaveWorkspace();
                finish();
            }

            @Override
            public void cancel() {
                finish();
            }
        },"温馨提示");
    }

    @Override
    protected void onDestroy() {
        if (receiver != null && manager != null){
            manager.unregisterReceiver(receiver);
        }
        if(controlClient != null && controlClient.isConnected()){
            controlClient.exit();
        }
        if (netBroadcastReceiver != null){
            unregisterReceiver(netBroadcastReceiver);
        }
        super.onDestroy();
    }
}
