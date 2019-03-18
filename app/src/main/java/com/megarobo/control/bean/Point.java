package com.megarobo.control.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

/**
 * 点
 */
public class Point {

    private Pose pose;

    private String name;

    public Pose getPose() {
        return pose;
    }

    public void setPose(Pose pose) {
        this.pose = pose;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Point parsePoint(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        Point point = new Point();
        point.setName(result.getString("name"));
        point.setPose(Pose.parsePose(result.getString("pose")));

        return point;
    }
}
