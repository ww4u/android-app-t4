package com.megarobo.control.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

/**
 * 位置
 */
public class Pose {

    private double x;

    private double y;

    private double z;

    //腕角度
    private double w;

    //爪子角度
    private double h;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public static Pose parsePose(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        Pose pose = new Pose();


        pose.setW(result.getDoubleValue("w"));
        pose.setH(result.getDoubleValue("h"));
        pose.setX(result.getDoubleValue("x"));
        pose.setY(result.getDoubleValue("y"));
        pose.setZ(result.getDoubleValue("z"));



        return pose;
    }
}
