package com.megarobo.control.net;

import com.megarobo.control.event.IPSearchEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPThread extends Thread {
	private String target_ip = "";
	
	public static final byte[] NBREQ = { (byte) 0x82, (byte) 0x28, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x1,
		(byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x20, (byte) 0x43, (byte) 0x4B,
		(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
		(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
		(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x41,
		(byte) 0x41, (byte) 0x41, (byte) 0x41, (byte) 0x0, (byte) 0x0, (byte) 0x21, (byte) 0x0, (byte) 0x1 };
	
	public static final short NBUDPP = 137;

	public UDPThread(String target_ip) {
		this.target_ip = target_ip;
	}

	@Override
	public synchronized void run() {
		if (target_ip == null || target_ip.equals("")) return;
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		InetAddress address = null;
//		DatagramPacket packet = null; //单播
		DatagramPacket packet = new DatagramPacket(new byte[0], 0, 0);

		int position = 2;
		try {
			while (position < 255) {
				String newip = target_ip+String.valueOf(position);
				address = InetAddress.getByName(newip);
				if(address == null){
					continue;
				}
				packet = new DatagramPacket(NBREQ, NBREQ.length, address, NBUDPP);
//				socket = new DatagramSocket();
				socket.setSoTimeout(200);
				if(socket.getInetAddress() == null){
					position++;
					continue;
				}
				socket.send(packet);
				position ++;
				if(position == 125){
					socket.close();
					socket = new DatagramSocket();
				}
				socket.close();
			}

			socket.close();
			//发送消息告诉已经扫描完了
			EventBus.getDefault().postSticky(new IPSearchEvent("ip search completed!"));
		} catch (SocketException se) {
		} catch (UnknownHostException e) {
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}
}
