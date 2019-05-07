package com.megarobo.control.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Logger;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

public class SocketClientHandler extends SimpleChannelHandler {
	
	private Handler mHandler;
	private String host;

	public SocketClientHandler(Handler mHandler,String host) {
		this.host = host;
		this.mHandler = mHandler;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e){
		Logger.e("exception",e.getCause().getMessage());
		if(e.getCause().getMessage().contains("recvfrom failed")){
			Message message = new Message();
			message.what = ConstantUtil.SOCKET_DISCONNECTED;
			Bundle bundle = new Bundle();
			bundle.putString("ip",host);
			message.setData(bundle);
			mHandler.sendMessage(message);
		}
		Channel channel = e.getChannel();
		if(channel!=null) {
			channel.close();
		}
	}

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelDisconnected(ctx, e);
        Logger.e("channelDisconnected",e.getState().toString());
        Message message = new Message();
        message.what = ConstantUtil.SOCKET_DISCONNECTED;
        Bundle bundle = new Bundle();
        bundle.putString("ip",host);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    /**
	 * 收到服务器发来的查询消息
	 * @param ctx
	 * @param e
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e){
		Object obj = e.getMessage();
		if(obj != null){
			String content = (String) obj;
			Logger.e("MessageReceived.....",content);
			Message message = new Message();
			message.what = ConstantUtil.MESSAGE_RECEIVED;
			Bundle bundle = new Bundle();
			bundle.putString("ip",host);
			bundle.putString("content",content);

			JSONObject jsonObject = JSON.parseObject(content);
			if(jsonObject != null){
				bundle.putString("command",jsonObject.getString("command"));
			}
			message.setData(bundle);
			mHandler.sendMessage(message);
		}
	}

	/**
	 * 和查询socket建立连接
	 * @param ctx
	 * @param e
	 * @throws Exception
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e){
		Logger.e("channelConnected",e.getState().toString());
		Message message = new Message();
		message.what = ConstantUtil.SOCKET_CONNECTED;
		Bundle bundle = new Bundle();
		bundle.putString("ip",host);
		message.setData(bundle);
		mHandler.sendMessage(message);
	}
}
