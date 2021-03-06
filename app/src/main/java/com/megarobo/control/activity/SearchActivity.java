package com.megarobo.control.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.megarobo.control.MegaApplication;
import com.megarobo.control.R;
import com.megarobo.control.adapter.SearchListAdapter;
import com.megarobo.control.bean.AreaDeviceBean;
import com.megarobo.control.bean.Meta;
import com.megarobo.control.bean.Robot;

import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.AllUitls;
import com.megarobo.control.utils.CommandHelper;

import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.ThreadPoolWrap;
import com.megarobo.control.utils.Utils;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchActivity extends BaseActivity {

    @ViewInject(R.id.textViewTop)
    private TextView textViewTop;

    @ViewInject(R.id.search_again_btn)
    private TextView searchAgainBtn;

    private Handler handler;

    private Set<String> ipSet = new HashSet<String>();
    private Set<String> realIpSet = new HashSet<String>();


    @ViewInject(R.id.equipment_list)
    private ListView robotListView;

    @ViewInject(R.id.no_equipment_layout)
    private LinearLayout noEquipmentLayout;

    @ViewInject(R.id.listview_layout)
    private RelativeLayout listviewLayout;

    @ViewInject(R.id.rotate_progress)
    private ImageView progressImg;

    private List<Robot> robotList;
    private SearchListAdapter adapter;

    private SocketClientManager clientManager;

    private Map<String,SocketClientManager> socketManagerMap;

    boolean isSearchFinished = false;

    private Animation animation;


    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ViewUtils.inject(this);
        initHandler();

        robotList = new ArrayList<Robot>();
        animation=AnimationUtils.loadAnimation(this, R.drawable.rotate_progress);


        adapter = new SearchListAdapter(
                this,robotList
        );
        robotListView.setAdapter(adapter);

        Utils.MakeToast(SearchActivity.this,"正在搜索机器人......");
        setMask(true);
        ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
            @Override
            public void run() {
                getList();
            }
        });

        socketManagerMap = new HashMap<String,SocketClientManager>();

        searchAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSearchFinished){
                    return;
                }
                if(robotList!=null && realIpSet != null){
                    robotList.clear();
                    realIpSet.clear();
                }
                Utils.MakeToast(SearchActivity.this,"开始搜索......");
                //1.获取连接的设备ip列表
                ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        getList();
                    }
                });
                isSearchFinished = false;
            }
        });

    }

    private void showNoEquipment(boolean isShown){
        if(isShown) {
            textViewTop.setText("未发现可用机器人");
            listviewLayout.setVisibility(View.GONE);
            noEquipmentLayout.setVisibility(View.VISIBLE);
        }else{
            textViewTop.setText("搜索机器人");
            listviewLayout.setVisibility(View.VISIBLE);
            noEquipmentLayout.setVisibility(View.GONE);
        }
    }

    private void getList() {
        clientManager = new SocketClientManager(ConstantUtil.HOST,
                handler,ConstantUtil.CONTROL_PORT);
        //1.首先判断最近列表是否有，有则直接尝试连接
        if(MegaApplication.latestRoboSet.size() > 0){
            Logger.e("latestRoboSet",MegaApplication.latestRoboSet.size()+"");
            for (String ip : MegaApplication.latestRoboSet){
                Logger.e("latestRoboSet",ip+"");
                if(socketManagerMap != null && socketManagerMap.get(ip)!=null) {
                    Logger.e("latestRoboSet111",ip+"");
                    socketManagerMap.get(ip).connectToServer();
                }else{
                    new SocketClientManager(ip,
                            handler, ConstantUtil.CONTROL_PORT).connectToServer();
                }
            }
        }

        AllUitls.initAreaIp(SearchActivity.this);
        List<AreaDeviceBean> beans = new ArrayList<>();
        int sum = 0;
        while (beans.size() == 0 && sum < 8) {
            beans.addAll(AllUitls.getAllCacheMac(MegaApplication.myIp));
            SystemClock.sleep(beans.size()>0?0:1000);
            sum++;
        }

        int size = beans.size();
        for(int i=0;i<size;i++){
            if(MegaApplication.latestRoboSet.contains(beans.get(i).getIp())){
                continue;
            }
            Logger.e("beans.ip",beans.get(i).getIp());
            clientManager.setHost(beans.get(i).getIp());
            clientManager.connectToServer();
            SystemClock.sleep(100);

        }
        isSearchFinished = true;
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
                        if(!ipSet.contains(ip)){//第一次回调，主要是将ip添加到set中
                            ipSet.add(ip);
                            SocketClientManager realClientManager = new SocketClientManager(ip,
                                    handler,ConstantUtil.CONTROL_PORT);
                            socketManagerMap.put(ip,realClientManager);
                            realClientManager.connectToServer();
                        }else{//第二次回调,从map里面找到对应的socketManager发起消息
                            if(socketManagerMap.get(ip) != null) {
                                Logger.e("late",ip+"");
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
                        setMask(false);
                        //真实的机器人IP
                        if(!realIpSet.contains(robot.getIp())){
                            realIpSet.add(robot.getIp());
                            robotList.add(robot);
                            adapter.notifyDataSetChanged();
                            showNoEquipment(false);
                        }
                        MegaApplication.robotList = robotList;
                        MegaApplication.latestRoboSet.add(robot.getIp());
                        break;
                    case ConstantUtil.IP_SEARCH_FINISHED:
                        if(robotList != null && robotList.size() == 0){
                            Logger.e("robotList",""+robotList.size());

                            setMask(false);
                            showNoEquipment(true);
                        }
                        MegaApplication.robotList = robotList;
                        break;
//                    default:
//                        Logger.e("robotList",""+robotList.size()+"msg.what"+msg.what);
//                        showNoEquipment(true);
//                        break;
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
