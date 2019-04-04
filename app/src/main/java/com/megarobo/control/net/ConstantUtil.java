package com.megarobo.control.net;

public class ConstantUtil {

	public  static final String HOST = "192.168.1.219";

	public static final int CONTROL_PORT = 50000;

	public static final int QUERY_PORT = 50001;

	public static final int EMERGENCY_PORT = 50002;

	//控制socket连接
	public static final int SOCKET_CONNECTED = 88;

	//控制socket连接
	public static final int MESSAGE_RECEIVED = 89;

	//控制socket连接
	public static final int SOCKET_CONTROL_CONNECTED = 801;

	//查询socket连接
	public static final int SOCKET_QUERY_CONNECTED = 802;

	//紧急socket连接
	public static final int SOCKET_EMERGENCY_CONNECTED = 803;

	//控制消息
	public static final int MESSAGE_CONTROL = 8001;

	//查询消息
	public static final int MESSAGE_QUERY = 8002;

	//紧急停止消息
	public static final int MESSAGE_EMERGENCY = 8003;


}
