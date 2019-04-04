package com.megarobo.control.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

/**
 * 设备状态
 */
public class Parameter {

    //驱动电流
    private Double[] currents;

    //空闲电流
    private Double[] idleCurrents;

    //减速比
    private Double[] slowRatio;

    //细分数
    private Double[] microSteps;

    //机械爪光电开关
    private Boolean[] handIo;

    //测距开关
    private Boolean[] distanceSensors;

    //节能开关
    private Boolean[] tunning;

    //碰撞开关
    private boolean collide;

    public Parameter(){
        currents = new Double[5];
        idleCurrents = new Double[5];
        slowRatio = new Double[5];
        microSteps = new Double[5];
        handIo = new Boolean[2];
        distanceSensors = new Boolean[4];
        tunning = new Boolean[5];
    }

    public Double[] getCurrents() {
        return currents;
    }

    public void setCurrents(Double[] currents) {
        this.currents = currents;
    }

    public Double[] getIdleCurrents() {
        return idleCurrents;
    }

    public void setIdleCurrents(Double[] idleCurrents) {
        this.idleCurrents = idleCurrents;
    }

    public Double[] getSlowRatio() {
        return slowRatio;
    }

    public void setSlowRatio(Double[] slowRatio) {
        this.slowRatio = slowRatio;
    }

    public Double[] getMicroSteps() {
        return microSteps;
    }

    public void setMicroSteps(Double[] microSteps) {
        this.microSteps = microSteps;
    }

    public Boolean[] getHandIo() {
        return handIo;
    }

    public void setHandIo(Boolean[] handIo) {
        this.handIo = handIo;
    }

    public Boolean[] getDistanceSensors() {
        return distanceSensors;
    }

    public void setDistanceSensors(Boolean[] distanceSensors) {
        this.distanceSensors = distanceSensors;
    }

    public Boolean[] getTunning() {
        return tunning;
    }

    public void setTunning(Boolean[] tunning) {
        this.tunning = tunning;
    }

    public boolean isCollide() {
        return collide;
    }

    public void setCollide(boolean collide) {
        this.collide = collide;
    }

    /**
     {
     "currents":[1.0,2.0,3.0,4.0,5.0],
     "idle_currents":[1.0,2.0,3.0,4.0,5.0],
     "slow_ratio":[1.0,2.0,3.0,4.0,5.0],
     "micro_steps":[1.0,2.0,3.0,4.0,5.0],
     "hand_io":[true,false],
     "distance_sensors":[true,false,true,false],
     "collide":true,
     "tunning":[1.0,2.0,3.0,4.0,5.0]
     }
     * @param jsonStr
     * @return
     */
    public static Parameter parseParameter(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        Parameter parameter = new Parameter();

        JSONArray currents = result.getJSONArray("currents");

        for (int i=0;i<currents.size();i++){
            parameter.getCurrents()[i] = currents.getDouble(i);
        }

        JSONArray idleCurrents = result.getJSONArray("idle_currents");
        if(idleCurrents == null){
            return null;
        }

        for (int i=0;i<idleCurrents.size();i++){
            parameter.getIdleCurrents()[i] = idleCurrents.getDouble(i);
        }

        JSONArray slowRatio = result.getJSONArray("slow_ratio");

        for (int i=0;i<slowRatio.size();i++){
            parameter.getSlowRatio()[i] = slowRatio.getDouble(i);
        }

        JSONArray microSteps = result.getJSONArray("micro_steps");

        for (int i=0;i<microSteps.size();i++){
            parameter.getMicroSteps()[i] = microSteps.getDouble(i);
        }

        JSONArray handIo = result.getJSONArray("hand_io");

        for (int i=0;i<handIo.size();i++){
            parameter.getHandIo()[i] = handIo.getBoolean(i);
        }

        JSONArray distanceSensors = result.getJSONArray("distance_sensors");

        for (int i=0;i<distanceSensors.size();i++){
            parameter.getDistanceSensors()[i] = distanceSensors.getBoolean(i);
        }

        parameter.setCollide(result.getBooleanValue("collide"));


        JSONArray tunning = result.getJSONArray("tunning");

        for (int i=0;i<tunning.size();i++){
            parameter.getTunning()[i] = tunning.getBoolean(i);
        }

        return parameter;
    }

    /**
     *
     String jsonStr = "{\n" +
     "     \"currents\":[1.0,2.0,3.0,4.0,5.0],\n" +
     "     \"idle_currents\":[1.0,2.0,3.0,4.0,5.0],\n" +
     "     \"slow_ratio\":[1.0,2.0,3.0,4.0,5.0],\n" +
     "     \"micro_steps\":[1.0,2.0,3.0,4.0,5.0],\n" +
     "     \"hand_io\":[true,false],\n" +
     "     \"distance_sensors\":[true,false,true,false],\n" +
     "     \"collide\":true,\n" +
     "     \"tunning\":[1.0,2.0,3.0,4.0,5.0]\n" +
     "     }";
     Parameter parameter = Parameter.parseParameter(jsonStr);

     */


}
