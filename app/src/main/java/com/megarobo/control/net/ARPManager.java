package com.megarobo.control.net;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.SimpleAdapter;

import com.megarobo.control.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * 先获取本机器ip，然后根据ip发起arp请求（UDPThread）
 * 将所有局域网内部的连接机器读取到文件里面
 */
public class ARPManager {

    private static ARPManager instance;

    private ARPManager(){

    }

    public static ARPManager getInstance(){
        if(instance == null ){
            instance = new ARPManager();
        }
        return instance;
    }

    public Map<String, String> readArpMap() {
        Map<String, String> map = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(
                    new FileReader("/proc/net/arp"));
            String line = "";
            String ip = "";
            String flag = "";
            String mac = "";

            while ((line = br.readLine()) != null) {
                Log.e("line",""+line);
                try {
                    line = line.trim();
                    if (line.length() < 63) continue;
                    if (line.toUpperCase(Locale.US).contains("IP")) continue;
                    ip = line.substring(0, 17).trim();
                    flag = line.substring(29, 32).trim();
                    mac = line.substring(41, 63).trim();
                    if (mac.contains("00:00:00:00:00:00")) continue;
                    Log.e("scanner", "readArp: mac= "+mac+" ; ip= "+ip+" ;flag= "+flag);
                    String arp = "ip: "+ip+" | "+"mac: "+mac+" | "+"flag: "+flag;

//                    map.put("arp",arp);
                    map.put(ip,arp);
//                    arpList.add(map);
                } catch (Exception e) {
                    continue;
                }
            }
            br.close();
        } catch(Exception e) {
        }
        return map;

    }

    private String myWifiName ;
    private String myIp;
    private String myMac;

    public void getNetworkInfo(Context context) {
        try {
            WifiManager wm = null;
            try {
                wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            } catch (Exception e) {
                wm = null;
            }
            if (wm != null && wm.isWifiEnabled()) {
                WifiInfo wifi = wm.getConnectionInfo();
                if (wifi.getRssi() != -200) {
                    myIp = getWifiIPAddress(wifi.getIpAddress());
                }
                myWifiName = wifi.getSSID(); //获取被连接网络的名称
                myMac =  wifi.getBSSID(); //获取被连接网络的mac地址
                String str = "WIFI: "+myWifiName+"\n"+"WiFiIP: "+myIp+"\n"+"MAC: "+myMac;
//                connectWifiInfo.setText(str);
                discover(myIp);
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }


    /**
     * 发送ARP请求
     * @param ip
     */
    private void discover(String ip) {
        String newip = "";
        if (!ip.equals("")) {
            String ipseg = ip.substring(0, ip.lastIndexOf(".")+1);
            for (int i=2; i<255; i++) {
                newip = ipseg+String.valueOf(i);
                if (newip.equals(ip)) continue;
                Thread ut = new UDPThread(newip);
                ut.start();
            }
        }
    }

    private String getWifiIPAddress(int ipaddr) {
        String ip = "";
        if (ipaddr == 0) return ip;
        byte[] addressBytes = {(byte)(0xff & ipaddr), (byte)(0xff & (ipaddr >> 8)),
                (byte)(0xff & (ipaddr >> 16)), (byte)(0xff & (ipaddr >> 24))};
        try {
            ip = InetAddress.getByAddress(addressBytes).toString();
            if (ip.length() > 1) {
                ip = ip.substring(1, ip.length());
            } else {
                ip = "";
            }
        } catch (UnknownHostException e) {
            ip = "";
        } catch (Exception e) {
            ip = "";
        }
        return ip;
    }

}
