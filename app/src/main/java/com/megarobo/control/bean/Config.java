package com.megarobo.control.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

/**
 * 遥控机器人--设置
 */
public class Config {


    //移动步距
    private double step;

    //手开抓合步距
    private double jointStep;

    //移动速度
    private double speed;

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getJointStep() {
        return jointStep;
    }

    public void setJointStep(double jointStep) {
        this.jointStep = jointStep;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public static Config parseConfig(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        Config config = new Config();
        config.setStep(result.getDoubleValue("step"));
        config.setJointStep(result.getDoubleValue("joint_step"));
        config.setSpeed(result.getDoubleValue("speed"));

        return config;
    }
}
