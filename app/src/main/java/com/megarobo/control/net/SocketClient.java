package com.megarobo.control.net;

import android.os.Handler;

import com.megarobo.control.utils.Logger;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class SocketClient {
	
	ClientBootstrap bootstrap;
	ChannelFuture channelFuture = null;
	Channel ch;

	public SocketClient(){
		
	}

	/**
	 * https://bylijinnan.iteye.com/blog/1985813 粘包
	 * @param host
	 * @param port
	 * @param mHandler
	 */
	public void start(final String host, int port, final Handler mHandler){
		bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline channelPipeline = Channels.pipeline();

				channelPipeline.addLast("encode", new StringEncoder());
				channelPipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192,Delimiters.lineDelimiter()));
				channelPipeline.addLast("decode", new StringDecoder());
				channelPipeline.addLast("handler", new SocketClientHandler(mHandler,host));
				return channelPipeline;
			}
		});
		bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(65535));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("reuseAddress", true);
		if(host != null) {
			channelFuture = bootstrap.connect(new InetSocketAddress(host, port));
			Logger.e("host",""+host);
		}
		if(channelFuture!=null)
			ch = channelFuture.getChannel();

	}
	
	public boolean sendmsg(String msg){
		if(ch!=null && ch.isConnected()){
			Logger.e("sendmsg",msg);
			ch.write(msg);
			return true;
		}
		return false;
	}

	public boolean isConnected(){
		if(ch == null){
			return false;
		}
		return ch.isConnected();
	}
	
	public void disconnect() {
		try {

			if(ch!=null){
				ch.disconnect();
				ch.close();
				ch = null;
			}
			if(channelFuture != null && channelFuture.awaitUninterruptibly() != null) {
				Channel channel = channelFuture.awaitUninterruptibly().getChannel();
				if (channel != null) {
					channel.close().awaitUninterruptibly();
				}
			}
			if (bootstrap != null) {
				bootstrap.releaseExternalResources();
			}
			if(channelFuture != null)
				channelFuture.getChannel().close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
