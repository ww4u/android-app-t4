package com.megarobo.control.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.R;
import com.megarobo.control.adapter.SearchListAdapter;
import com.megarobo.control.bean.Meta;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.net.ARPManager;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchActivity extends BaseActivity {


    @ViewInject(R.id.search_again_btn)
    private TextView searchAgainBtn;

    private Handler handler;

    private Set<String> ipSet = new HashSet<String>();

    @ViewInject(R.id.equipment_list)
    private ListView robotListView;

    private List<Robot> robotList;
    private SearchListAdapter adapter;

    private SocketClientManager clientManager;

    private Map<String,SocketClientManager> socketManagerMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ViewUtils.inject(this);

        initHandler();

        robotList = new ArrayList<Robot>();
        robotList.add(Utils.getTestRobot());//测试用，后面要删掉
        robotList.add(Utils.getTestRobot());//测试用，后面要删掉


        adapter = new SearchListAdapter(
                this,robotList
        );
        robotListView.setAdapter(adapter);

        socketManagerMap = new HashMap<String,SocketClientManager>();


        searchAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.获取连接的设备ip列表
                ARPManager.getInstance().getNetworkInfo(SearchActivity.this);
                Map<String, String> map = ARPManager.getInstance().readArpMap();
                clientManager = new SocketClientManager(ConstantUtil.HOST,
                        handler,ConstantUtil.CONTROL_PORT);
                for(String ip : map.keySet()){
                    clientManager.setHost(ip);
                    clientManager.connectToServer();
                }
            }
        });


    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case ConstantUtil.SOCKET_CONNECTED:
                        Bundle bundle = msg.getData();
                        String ip = bundle.getString("ip");
                        if(!ipSet.contains(ip)){//第一次回调，主要是将ip添加到set中
                            ipSet.add(ip);
                            Utils.MakeToast(SearchActivity.this,"重新搜索......"+ip);
                            SocketClientManager realClientManager = new SocketClientManager(ip,
                                    handler,ConstantUtil.CONTROL_PORT);
                            socketManagerMap.put(ip,realClientManager);
                            realClientManager.connectToServer();
                        }else{//第二次回调,从map里面找到对应的socketManager发起消息
                            if(socketManagerMap.get(ip) != null) {
                                socketManagerMap.get(ip).sendMsgToServer(
                                        CommandHelper.getInstance().queryCommand("meta"));
                            }
                        }
                        break;
                    case ConstantUtil.MESSAGE_RECEIVED:
                        Bundle bundle1 = msg.getData();
                        String content = bundle1.getString("content");

                        Robot robot = new Robot();
                        robot.setIp(bundle1.getString("ip"));
                        robot.setMeta(Meta.parseMeta(content));
                        robotList.add(robot);
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        };
    }


}
