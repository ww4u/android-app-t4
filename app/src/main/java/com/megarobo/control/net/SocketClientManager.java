package com.megarobo.control.net;

import android.os.Handler;

import com.megarobo.control.utils.Logger;
import com.megarobo.control.utils.ThreadPoolWrap;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * socket管理类 调用该类 必需先调用init方法
 *
 */
public class SocketClientManager {

	private SocketClient mQueryClient;
	private Handler mHandler;
	private int port;
	private String host;
	
	private Timer mTimer;
	private TimerTask mTimerTask;
	private final int DELAY = 1000 * 2;
	private final int PERIOD = 1000 * 2;
	
	public SocketClientManager(String host,Handler mHandler,int port){
		this.host = host;
		this.mHandler = mHandler;
		this.port = port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void joinRoom(){
		try {
			int msgType = 1;

			JSONObject jsonObject = new JSONObject();
			jsonObject.put("msghead", "");
			
			JSONObject objBody = new JSONObject();
				objBody.put("msgid", msgType);
				objBody.put("os", 1);
			jsonObject.put("msgbody", objBody);
			
			sendMsgToServer(jsonObject);
			startkeepAlive();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void exit(){
		stopkeepAlive();//关闭心跳包
		if(mQueryClient != null){
			mQueryClient.disconnect();
		}
	}

	public boolean isConnected(){
		if(mQueryClient == null){
			return false;
		}
		return mQueryClient.isConnected();
	}
	
	public void sendEmptyMessage() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("msghead", "");
		
		JSONObject objBody = new JSONObject();
			objBody.put("os", 1);
		jsonObject.put("msgbody", objBody);
		
		sendMsgToServer(jsonObject);
	}
	
	public void sendMsgToServer(JSONObject jsonObject){
		try {
    	    if(mQueryClient!=null && !mQueryClient.sendmsg(jsonObject.toString()+"#")){
    	    	connectToServer();
    	    	Logger.v("SendMessageToServer","Error connect,channel is closed");
    	    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 连接服务器

	 */
	public void connectToServer(){
		Logger.e("connectToServer","connectToServer.......");
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					mQueryClient = new SocketClient();
					mQueryClient.start(host, port,mHandler);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
//		ThreadPoolWrap.getThreadPool().executeTask(new Runnable() {
//			@Override
//			public void run() {
//				mQueryClient = new SocketClient();
//				mQueryClient.start(host, port,mHandler);
//			}
//		});
	}
	/**
	 * 开启心跳包
	 */
	public void startkeepAlive(){
		Logger.e("startKeepAlive","start...send ...alive");
		stopkeepAlive();
		
		mTimerTask = new TimerTask() {
			
			@Override
			public void run() {
				
				try {
					sendEmptyMessage();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
		};
		mTimer = new Timer();
		mTimer.schedule(mTimerTask, DELAY, PERIOD);
	}
	/**
	 * 关闭心跳包
	 */
	public void stopkeepAlive(){
		if(mTimer != null){
			mTimer.cancel();
		}
		if(mTimerTask != null){
			mTimerTask.cancel();
		}
		mTimer = null;
		mTimerTask = null;
	}
	
}