package com.megarobo.control.activity;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import org.apache.http.conn.util.InetAddressUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchIPActivity extends BaseActivity {

    @ViewInject(R.id.textViewTop)
    private TextView textViewTop;

    @ViewInject(R.id.search_again_btn)
    private TextView searchAgainBtn;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ViewUtils.inject(this);
//        initHandler();

        robotList = new ArrayList<Robot>();

        adapter = new SearchListAdapter(
                this,robotList
        );
        robotListView.setAdapter(adapter);

//        getList();

        socketManagerMap = new HashMap<String,SocketClientManager>();


        searchAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //1.获取连接的设备ip列表
            getList();
            }
        });

        scan();


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
        ARPManager.getInstance().getNetworkInfoSearch(SearchIPActivity.this);
        Map<String, String> map = ARPManager.getInstance().readArpMap();
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
                            Utils.MakeToast(SearchIPActivity.this,"重新搜索......"+ip);
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

    private int SERVERPORT = 8888;

    private String locAddress;//存储本机ip，例：本地ip ：192.168.1.

    private Runtime run = Runtime.getRuntime();//获取当前运行环境，来执行ping，相当于windows的cmd

    private Process proc = null;

    private String ping = "ping -c 1 -w 0.5 " ;//其中 -c 1为发送的次数，-w 表示发送后等待响应的时间

    private int j;//存放ip最后一位地址 0-255

    private Context ctx;//上下文



    private Handler handler = new Handler(){

        public void dispatchMessage(Message msg) {
            switch (msg.what) {

                case 222:// 服务器消息
                    break;

                case 333:// 扫描完毕消息
                    Toast.makeText(ctx, "扫描到主机："+((String)msg.obj).substring(6), Toast.LENGTH_LONG).show();

                    break;
                case 444://扫描失败
                    Toast.makeText(ctx, (String)msg.obj, Toast.LENGTH_LONG).show();
                    break;
            }
        }

    };



    //向serversocket发送消息
    public String sendMsg(String ip,String msg) {

        String res = null;
        Socket socket = null;

        try {
            socket = new Socket(ip, SERVERPORT);
            //向服务器发送消息
            PrintWriter os = new PrintWriter(socket.getOutputStream());
            os.println(msg);
            os.flush();// 刷新输出流，使Server马上收到该字符串

            //从服务器获取返回消息
            DataInputStream input = new DataInputStream(socket.getInputStream());
            res = input.readUTF();
            System.out.println("server 返回信息：" + res);
            Message.obtain(handler, 222, res).sendToTarget();//发送服务器返回消息

        } catch (Exception unknownHost) {
            System.out.println("You are trying to connect to an unknown host!");
        } finally {
            // 4: Closing connection
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return res;
    }



    /**
     * 扫描局域网内ip，找到对应服务器
     */
    public void scan(){

        locAddress = getLocAddrIndex();//获取本地ip前缀

        if(locAddress.equals("")){
            Toast.makeText(ctx, "扫描失败，请检查wifi网络", Toast.LENGTH_LONG).show();
            return ;
        }

        for ( int i = 0; i < 256; i++) {//创建256个线程分别去ping

            j = i ;

            new Thread(new Runnable() {

                public void run() {

                    String p = ping + locAddress + j ;

                    String current_ip = locAddress+ j;

                    try {
                        proc = run.exec(p);

                        int result = proc.waitFor();
                        if (result == 0) {
                            System.out.println("连接成功" + current_ip);
                            // 向服务器发送验证信息
                            String msg = sendMsg(current_ip,"scan"+getLocAddress()+" ( "+android.os.Build.MODEL+" ) ");

                            //如果验证通过...
                            if (msg != null){
                                if (msg.contains("OK")){
                                    System.out.println("服务器IP：" + msg.substring(8,msg.length()));
                                    Message.obtain(handler, 333, msg.substring(2,msg.length())).sendToTarget();//返回扫描完毕消息
                                }
                            }
                        } else {

                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    } finally {
                        proc.destroy();
                    }
                }
            }).start();

        }

    }


    //获取本地ip地址
    public String getLocAddress(){

        String ipaddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                // 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> address = networks.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        System.out.println("本机IP:" + ipaddress);

        return ipaddress;

    }

    //获取IP前缀
    public String getLocAddrIndex(){

        String str = getLocAddress();

        if(!str.equals("")){
            return str.substring(0,str.lastIndexOf(".")+1);
        }

        return null;
    }

    //获取本机设备名称
    public String getLocDeviceName() {

        return android.os.Build.MODEL;

    }


}

