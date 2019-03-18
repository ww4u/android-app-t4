package com.megarobo.control.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

public class DeviceStatus {

    /**stoped,running,exception_stoped
     *
     * 停止，运行中，异常停止
     */
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static DeviceStatus parseDeviceStatus(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        DeviceStatus deviceStatus = new DeviceStatus();
        deviceStatus.setStatus(result.getString("status"));

        return deviceStatus;
    }
}
