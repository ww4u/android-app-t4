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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.blockly.android.AbstractBlocklyActivity;
import com.google.blockly.android.BlocklyActivityHelper;
import com.google.blockly.android.codegen.CodeGenerationRequest;
import com.google.blockly.android.codegen.LanguageDefinition;
import com.google.blockly.android.control.BlocklyController;
import com.google.blockly.model.DefaultBlocks;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;

import java.util.Arrays;
import java.util.List;


/**
 * Demo activity that programmatically adds a view to split the screen between the Blockly workspace
 * and an arbitrary other view or fragment and generates Python code.
 */
public class PythonActivity extends AbstractBlocklyActivity {
    private static final String TAG = "PythonActivity";

    private static final String SAVE_FILENAME = "python_workspace.xml";
    private static final String AUTOSAVE_FILENAME = "python_workspace_temp.xml";
    // Add custom blocks to this list.
    private static final List<String> BLOCK_DEFINITIONS = DefaultBlocks.getAllBlockDefinitions();
    private static final List<String> PYTHON_GENERATORS = Arrays.asList();

    private static final LanguageDefinition PYTHON_LANGUAGE_DEF
            = LanguageDefinition.PYTHON_LANGUAGE_DEFINITION;

    private TextView mGeneratedTextView;
    private Handler mHandler;

    private String mNoCodeText;

    CodeGenerationRequest.CodeGeneratorCallback mCodeGeneratorCallback =
            new CodeGenerationRequest.CodeGeneratorCallback() {
                @Override
                public void onFinishCodeGeneration(final String generatedCode) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            MegaApplication.getInstance().controlClient.sendMsgToServer(
                                    CommandHelper.getInstance().scriptCommand(generatedCode));
                            mGeneratedTextView.setText(generatedCode);
                            DemoUtil.updateTextMinWidth(mGeneratedTextView, PythonActivity.this);
                        }
                    });
                }
            };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return TurtleActivity.onDemoItemSelected(item, this) || super.onOptionsItemSelected(item);
    }


    class MyBroadCastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra("action");
            Utils.MakeToast(PythonActivity.this,"ssssbbbb"+action);
            //根据广播中传过来的block类型，跳转到相应的页面
            if("point".equals(action)){
                //跳转到设置x,y,z页面
                startActivityForResult(new Intent(PythonActivity.this, SetPointActivity.class),
                        ConstantUtil.REQUEST_CODE_POINT);
            }else if("hand".equals(action)){
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
        switch (requestCode){
            case ConstantUtil.REQUEST_CODE_POINT:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    String x = bundle.getString("x");
                    String y = bundle.getString("y");
                    String z = bundle.getString("z");
                    getController().setPoint(x,y,z);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();


        manager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlocklyController.DISC_BROADCAST);
        receiver = new MyBroadCastReceiver();
        manager.registerReceiver(receiver,filter);

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
}
