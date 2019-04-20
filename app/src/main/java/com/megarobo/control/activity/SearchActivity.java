package com.megarobo.control.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
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
import com.megarobo.control.event.IPSearchEvent;
import com.megarobo.control.event.ReadARPMapEvent;
import com.megarobo.control.net.ARPManager;
import com.megarobo.control.net.ConstantUtil;
import com.megarobo.control.net.SocketClientManager;
import com.megarobo.control.utils.AllUitls;
import com.megarobo.control.utils.CommandHelper;
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

    private List<Robot> robotList;
    private SearchListAdapter adapter;

    private SocketClientManager clientManager;

    private Map<String,SocketClientManager> socketManagerMap;

    boolean isSearchFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ViewUtils.inject(this);
        initHandler();

        robotList = new ArrayList<Robot>();

        adapter = new SearchListAdapter(
                this,robotList
        );
        robotListView.setAdapter(adapter);

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
        AllUitls.initAreaIp(SearchActivity.this);
        List<AreaDeviceBean> beans = new ArrayList<>();
        int sum = 0;
        while (beans.size() == 0 && sum < 8) {
            beans.addAll(AllUitls.getAllCacheMac(MegaApplication.myIp));
            SystemClock.sleep(beans.size()>0?0:1000);
            sum++;
        }
        clientManager = new SocketClientManager(ConstantUtil.HOST,
                handler,ConstantUtil.CONTROL_PORT);
        for(int i=0;i<beans.size();i++){
            clientManager.setHost(beans.get(i).getIp());
            clientManager.connectToServer();
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
                            showNoEquipment(false);
                        }
                        break;
                    case ConstantUtil.IP_SEARCH_FINISHED:
                        if(robotList != null && robotList.size() == 0){
                            showNoEquipment(true);
                        }
                        break;
                    default:
                        showNoEquipment(true);
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
