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
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.adapter.ConnectListAdapter;
import com.megarobo.control.bean.Meta;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.event.IPSearchEvent;
import com.megarobo.control.event.ReadARPMapEvent;
import com.megarobo.control.net.ARPManager;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.R;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.ThreadPoolWrap;
import com.megarobo.control.utils.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    @ViewInject(R.id.searchBtn)
    private TextView searchBtn;

    @ViewInject(R.id.robotList)
    private ListView robotListView;

    private List<Robot> robotList;
    private ConnectListAdapter adapter;


    private SocketClientManager clientManager;

    private Handler handler;

    private Map<String,SocketClientManager> socketManagerMap;

    private Set<String> ipSet = new HashSet<String>();
    private Set<String> realIpSet = new HashSet<String>();

    private boolean isExit;
    private static final int MSG_EXIT_ROOM = 5001;
    private static final int MSG_EXIT_ROOM_DELAY = 2000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ViewUtils.inject(this);

        EventBus.getDefault().register(this);

        robotList = new ArrayList<Robot>();

        initHandler();
        socketManagerMap = new HashMap<String,SocketClientManager>();
        //获取局域网内所有机器人
        ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
            @Override
            public void run() {
                ARPManager.getInstance().getNetworkInfo(ConnectActivity.this);

            }
        });


        adapter = new ConnectListAdapter(
                this,robotList
        );
        robotListView.setAdapter(adapter);
        robotListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Robot robot = (Robot) parent.getAdapter().getItem(position);
                if(robot == null){
                    return;
                }
                MegaApplication.ip = robot.getIp();
                MegaApplication.name = Utils.replaceX(robotList.get(position).getMeta().getSn());
                SocketClientManager socketClientManager = socketManagerMap.get(robot.getIp());
                if(socketClientManager!=null) {
                    socketClientManager.sendMsgToServer(CommandHelper.getInstance().indicatorCommand(true));
                }

                Intent intent = new Intent(ConnectActivity.this, EquipmentActivity.class);
                startActivity(intent);
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(ConnectActivity.this, SearchActivity.class);
//                startActivity(intent);
                Utils.MakeToast(ConnectActivity.this,"正在搜索机器人......");
                ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        ARPManager.getInstance().getNetworkInfo(ConnectActivity.this);

                    }
                });

            }
        });
    }


    public Map<String, String> map;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(IPSearchEvent event){
        ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
            @Override
            public void run() {
                map = ARPManager.getInstance().readArpMap();
            }
        });

        Utils.MakeToast(ConnectActivity.this,"正在搜索机器人......");
        clientManager = new SocketClientManager(ConstantUtil.HOST,
                handler,ConstantUtil.CONTROL_PORT);

        if(map != null) {
            for (String ip : map.keySet()) {
                clientManager.setHost(ip);
                clientManager.connectToServer();

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event1(ReadARPMapEvent event){
//        Utils.MakeToast(ConnectActivity.this,"!!!!");
        clientManager = new SocketClientManager(ConstantUtil.HOST,
                handler,ConstantUtil.CONTROL_PORT);
        for(String ip : map.keySet()){
            clientManager.setHost(ip);
            clientManager.connectToServer();
        }
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
                            if(socketManagerMap.get(ip) != null){
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
                        //真实的机器人IP
                        if(!realIpSet.contains(robot.getIp())){
                            realIpSet.add(robot.getIp());
                            robotList.add(robot);
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case MSG_EXIT_ROOM:
                        isExit = false;
                        break;
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        if(!isExit){
            isExit = true;
            Utils.MakeToast(ConnectActivity.this,"再按一次退出程序");
            handler.sendEmptyMessageDelayed(MSG_EXIT_ROOM, MSG_EXIT_ROOM_DELAY);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
        if(clientManager!=null){
            clientManager.exit();
        }
    }
}
