package com.megarobo.control.activity;


import android.annotation.SuppressLint;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.adapter.ConnectListAdapter;
import com.megarobo.control.bean.Meta;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.net.ARPManager;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * app主页面
 * 进来自动搜索局域网内机器人，并初始化到listview中
 *
 */
public class ConnectActivity extends BaseActivity {


    @ViewInject(R.id.connectBtn)
    private Button connectBtn;

    @ViewInject(R.id.searchBtn)
    private Button searchBtn;

    @ViewInject(R.id.robotList)
    private ListView robotListView;

    private List<Robot> robotList;
    private ConnectListAdapter adapter;


    private SocketClientManager clientManager;

    private Handler handler;

    private Map<String,SocketClientManager> socketManagerMap;

    private Set<String> ipSet = new HashSet<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ViewUtils.inject(this);

        initHandler();
        socketManagerMap = new HashMap<String,SocketClientManager>();
        //获取局域网内所有机器人
        ARPManager.getInstance().getNetworkInfo(ConnectActivity.this);
        Map<String, String> map = ARPManager.getInstance().readArpMap();
        clientManager = new SocketClientManager(ConstantUtil.HOST,
                handler,ConstantUtil.CONTROL_PORT);
        for(String ip : map.keySet()){
            clientManager.setHost(ip);
            clientManager.connectToServer();
        }

        robotList = new ArrayList<Robot>();
        adapter = new ConnectListAdapter(
                this,robotList
        );
        robotListView.setAdapter(adapter);
        robotListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Robot robot = (Robot) parent.getAdapter().getItem(position);
                MegaApplication.ip = robot.getIp();
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //根据列表里面的ip初始化clientManager
                //MegaApplication.ip = ConstantUtil.HOST;
//                clientManager = new SocketClientManager(MegaApplication.ip,
//                        handler,ConstantUtil.CONTROL_PORT);
//                clientManager.connectToServer();

                Intent intent = new Intent(ConnectActivity.this, EquipmentActivity.class);
                startActivity(intent);
            }
        });



        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ConnectActivity.this, SearchActivity.class);
                startActivity(intent);

//                SocketClientManager2.getInstance().socketSendMessage("sss");
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        test();
//                    }
//                }).start();

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
                            SocketClientManager realClientManager = new SocketClientManager(ip,
                                    handler,ConstantUtil.CONTROL_PORT);
                            socketManagerMap.put(ip,realClientManager);
                            realClientManager.connectToServer();
                        }else{//第二次回调,从map里面找到对应的socketManager发起消息
                            socketManagerMap.get(ip).sendMsgToServer(
                                    CommandHelper.getInstance().queryCommand("meta"));
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(clientManager!=null){
            clientManager.exit();
        }
    }
}
