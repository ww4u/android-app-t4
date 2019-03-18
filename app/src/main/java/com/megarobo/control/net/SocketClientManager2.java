package com.megarobo.control.net;

import android.support.annotation.NonNull;

import com.megarobo.control.utils.Logger;
import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.helper.SocketClientDelegate;
import com.vilyever.socketclient.helper.SocketResponsePacket;

import java.io.IOException;

public class SocketClientManager2 {

    private static SocketClientManager2 instance;

    private SocketClient socketClient;
    private SocketClientDelegate delegate;

    private SocketClientManager2(){

    }

    public static SocketClientManager2 getInstance(){
        if(instance == null ){
            instance = new SocketClientManager2();
        }
        return instance;
    }

    public void startSocketClient(String info,String ip) throws IOException {
        //String[] array = info.split("_");
        socketClient = new SocketClient();
        //socketClient.getAddress().setRemoteIP(array[0]);//设置IP,这里设置的是本地IP
        //socketClient.getAddress().setRemotePort(String.valueOf(Integer.parseInt(array[1])));//设置端口
        socketClient.getAddress().setRemoteIP(ip);//设置IP,这里设置的是本地IP
        socketClient.getAddress().setRemotePort(ConstantUtil.CONTROL_PORT+"");//设置端口
        socketClient.getAddress().setConnectionTimeout(15 * 1000);//设置超时时间


        //socketClient.setCharsetName(CharsetUtil.UTF_8);//设置编码格式，默认为UTF-8
        socketClient.setCharsetName("GBK");//设置编码格式，默认为UTF-8
        socketClient.connect(); // 连接，异步进行

        //常用回调配置
        // 对应removeSocketClientDelegate
        socketClient.registerSocketClientDelegate(delegate = new SocketClientDelegate() {
            /**
             * 连接上远程端时的回调
             */
            @Override
            public void onConnected(SocketClient client) {
                Logger.d("melog", "gamesocket连接成功");
                //launcher.callExternalInterface("gameSocketConnectSuccess", "success");
            }

            /**
             * 与远程端断开连接时的回调
             */
            @Override
            public void onDisconnected(SocketClient client) {
                Logger.d("melog", "gamesocket连接断开");
                // 可在此实现自动重连
                socketClient.connect();
                //launcher.callExternalInterface("socketClose", "close");
            }

            /**
             * 接收到数据包时的回调
             */
            @Override
            public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {

                String message = responsePacket.getMessage(); // 获取按默认设置的编码转化的String，可能为null
                Logger.i("接收服务端消息：",message);

                //launcher.callExternalInterface("socketDataHandler", message);
            }
        });
    }

    //发送消息
    public void socketSendMessage(String info) {
        String status = String.valueOf(socketClient.getState());
        if (socketClient != null && status == "Connected") {
            socketClient.sendData(info.getBytes()); // 发送byte[]消息
        }
    }

    //前台请求gamesocket连接状态
    public void requestGameSocketConnectFlg() {
        String status = String.valueOf(socketClient.getState());
        //launcher.callExternalInterface("gameSocketFlg", status);
    }

    //前台主动断开gamesocket
    public void closeGameSocket() {
        if (socketClient != null) {
            socketClient.removeSocketClientDelegate(delegate);
            socketClient.disconnect();
        }

    }
}
