package com.megarobo.control.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

public class LinkStatus {

    //idle,busy
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static LinkStatus parseLinkStatus(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        LinkStatus linkStatus = new LinkStatus();
        linkStatus.setStatus(result.getString("status"));

        return linkStatus;
    }
}
