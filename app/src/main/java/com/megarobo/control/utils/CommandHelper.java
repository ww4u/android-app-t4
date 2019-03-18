package com.megarobo.control.utils;


import com.megarobo.control.bean.Pose;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 封装所有socket命令的类
 *
 * 将命令以json的形式封装并返回给socketClient
 *
 * 命令主要分为控制，查询，配置
 */
public class CommandHelper {

    private static CommandHelper instance;

    private CommandHelper(){

    }

    public static CommandHelper getInstance(){
        if(instance == null ){
            instance = new CommandHelper();
        }
        return instance;
    }

    /**
     * 1.控制机器
     */
    public JSONObject stepCommand(int angle,double z,boolean continous){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","step");
            jsonObject.put("angle",angle);
            jsonObject.put("z",z);
            jsonObject.put("continous",continous);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 2.控制机器爪子
     * @return
     */
    public JSONObject jointCommand(boolean continous,int joint){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","joint_step");
            jsonObject.put("continous",continous);
            jsonObject.put("joint",joint);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 3.功能
     * @return
     *
     * home;
     * emergency_stop;
     * stop
     * package;
     */
    public JSONObject actionCommand(String type){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","action");
            jsonObject.put("item",type);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 4.指示灯
     * @return
     */
    public JSONObject indicatorCommand(boolean status){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","indicator");
            jsonObject.put("status",status);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 5.记录该点
     * @return
     */
    public JSONObject addPointCommand(String name,Pose pose){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","add");
            jsonObject.put("name",name);

            JSONObject poseObj = new JSONObject();
            poseObj.put("x",pose.getX()+"");
            poseObj.put("y",pose.getY()+"");
            poseObj.put("z",pose.getZ()+"");
            poseObj.put("w",pose.getW()+"");
            poseObj.put("h",pose.getH()+"");

            jsonObject.put("pose",poseObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 6.查询命令
     * @return
     * type:
     * link_status;
     * device_status;
     * exception;
     * pose;
     * parameter;
     * dataset;
     * meta;
     * config;
     */
    public JSONObject queryCommand(String type){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","query");
            jsonObject.put("item",type);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    /**
     * 7.config
     * @param
     * @return
     */
    public JSONObject configCommand(double step,double jointStep,double speed){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","config");
            jsonObject.put("step",step);
            jsonObject.put("joint_step",jointStep);
            jsonObject.put("speed",speed);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
