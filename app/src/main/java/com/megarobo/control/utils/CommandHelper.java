package com.megarobo.control.utils;


import com.megarobo.control.bean.Point;
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
     * 1.控制机器，上和下点击时，z会有数值，分别时1和-1，z等于0时，说明在xy方向有运动
     *
     * step的圆形区域有连续运动操作
     */
    public JSONObject stepCommand(double angle,double z,boolean continous){
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
     *
     * 界面上的开和闭代表爪子，点击开则value = 1，点击闭则value = -1，按住则continous=true，同时joint=4
     *
     * 左边的圆形代表腕运动,joint = 3, continous=false, value=角度
     *
     * 上是90度，前180度，后0度，下270度
     *
     * 1代表正向运动，顺时针
     *
     * -1代表反向运动，逆时针
     *
     * @return
     */
    public JSONObject jointCommand(int value,boolean continous,int joint){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","joint_step");
            jsonObject.put("value",value);
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
     * 发送连接和退出连接命令
     * @return
     */
    public JSONObject linkCommand(boolean status){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","link_status");
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
    public JSONObject addPointCommand(Point point){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","add");
            jsonObject.put("name",point.getName());

            JSONObject poseObj = new JSONObject();
            poseObj.put("x",point.getPose().getX());
            poseObj.put("y",point.getPose().getY());
            poseObj.put("z",point.getPose().getZ());
            poseObj.put("w",point.getPose().getW());
            poseObj.put("h",point.getPose().getH());

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

    /**
     * 8.script
     * @param
     * @return
     */
    public JSONObject scriptCommand(String pythonScript){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("command","execute");
            jsonObject.put("script",pythonScript);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }
}
