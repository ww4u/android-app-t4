package com.megarobo.control.activity;


import android.annotation.SuppressLint;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.os.SystemClock;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.adapter.ConnectListAdapter;
import com.megarobo.control.bean.Meta;
import com.megarobo.control.bean.Robot;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.utils.CommandHelper;
import com.megarobo.control.R;
import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.ThreadPoolWrap;
import com.megarobo.control.utils.Utils;

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

    @ViewInject(R.id.rotate_progress)
    private ImageView progressImg;

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

    private Animation animation;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ViewUtils.inject(this);

        robotList = new ArrayList<Robot>();
        animation=AnimationUtils.loadAnimation(this, R.drawable.rotate_progress);


        initHandler();
        socketManagerMap = new HashMap<String,SocketClientManager>();
        //获取局域网内所有机器人
        setMask(true);
        ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
            @Override
            public void run() {
                getList();

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
                if(robot == null || robot.getMeta().isLink()){
                    return;
                }
                MegaApplication.ip = robot.getIp();
                MegaApplication.name = Utils.replaceX(robotList.get(position).getMeta().getSn());
//                SocketClientManager socketClientManager = socketManagerMap.get(robot.getIp());
//                if(socketClientManager!=null) {
//                    socketClientManager.sendMsgToServer(CommandHelper.getInstance().indicatorCommand(true));
//                }
                Intent intent = new Intent(ConnectActivity.this, EquipmentActivity.class);
                startActivity(intent);
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ConnectActivity.this, SearchActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(MegaApplication.robotList != null){
            if(adapter != null && robotList != null){
                robotList.clear();
                robotList.addAll(MegaApplication.robotList);
                adapter.notifyDataSetChanged();
                setMask(false);
            }
        }

    }

    private void getList() {
        Logger.e("MegaApplication.beans",MegaApplication.beans.size()+"......");
        if(MegaApplication.beans != null && MegaApplication.beans.size() != 0){
            clientManager = new SocketClientManager(ConstantUtil.HOST,
                    handler,ConstantUtil.CONTROL_PORT);
            for(int i=0;i<MegaApplication.beans.size();i++){
                clientManager.setHost(MegaApplication.beans.get(i).getIp());
                clientManager.connectToServer();
                SystemClock.sleep(100);
            }
        }
        SystemClock.sleep(10000);
        handler.sendEmptyMessage(ConstantUtil.IP_SEARCH_FINISHED);
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
                        Logger.e("ip",""+ip);
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
                            setMask(false);
                            realIpSet.add(robot.getIp());
                            robotList.add(robot);
                            adapter.notifyDataSetChanged();
                        }
                        MegaApplication.latestRoboSet.add(robot.getIp());
                        break;
                    case MSG_EXIT_ROOM:
                        isExit = false;
                        break;
                    case ConstantUtil.IP_SEARCH_FINISHED:
                        if(robotList != null && robotList.size() == 0){
                            setMask(false);
                        }
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
        if(clientManager!=null){
            clientManager.exit();
            clientManager = null;
        }
        if(robotList != null){
            robotList.clear();
            robotList = null;
        }
        if(MegaApplication.robotList != null){
            MegaApplication.robotList.clear();
            MegaApplication.robotList = null;
        }
    }

    protected void setMask(boolean b) {
        if (b) {
            progressImg.startAnimation(animation);
            progressImg.setVisibility(View.VISIBLE);
        } else {
            progressImg.clearAnimation();
            progressImg.setVisibility(View.GONE);
        }
    }
}
