package com.megarobo.control.mqtt;


import com.megarobo.control.bean.Point;

import org.json.JSONException;
import org.json.JSONObject;


public class MQTTCommandHelper {

    private static MQTTCommandHelper instance;

    private MQTTCommandHelper(){

    }

    public static MQTTCommandHelper getInstance(){
        if(instance == null ){
            instance = new MQTTCommandHelper();
        }
        return instance;
    }

    public String mqttCommand(String command){
        StringBuffer sb = new StringBuffer();
        sb.append("->");
        sb.append(command);
        return sb.toString();
    }


    /**
     * demo
     *
     * home	回零
     *
     * fold 折叠
     *
     * estop 立即停止
     *
     * x+	前
     *
     * x-	后
     *
     * y+	左
     *
     * y-	右
     *
     * z+	上
     *
     * z-	下
     *
     * h+	放开
     *
     * h-	抓
     * @return
     */

    public String demo(){
        return mqttCommand("demo");
    }

    public String home(){
        return mqttCommand("home");
    }

    public String fold(){
        return mqttCommand("fold");
    }

    public String stop(){
        return mqttCommand("estop");
    }

    public String front(){
        return mqttCommand("x+");
    }

    public String frontContinue(){
        return mqttCommand("x++");
    }

    public String back(){
        return mqttCommand("x-");
    }

    public String backContinue(){
        return mqttCommand("x--");
    }

    public String left(){
        return mqttCommand("y+");
    }

    public String leftContinue(){
        return mqttCommand("y++");
    }

    public String right(){
        return mqttCommand("y-");
    }

    public String rightContinue(){
        return mqttCommand("y--");
    }

    public String up(){
        return mqttCommand("z+");
    }

    public String upContinue(){
        return mqttCommand("z++");
    }

    public String down(){
        return mqttCommand("z-");
    }

    public String downContinue(){
        return mqttCommand("z--");
    }

    public String open(){
        return mqttCommand("h+");
    }

    public String openContinue(){
        return mqttCommand("h++");
    }

    public String close(){
        return mqttCommand("h-");
    }

    public String closeContinue(){
        return mqttCommand("h--");
    }



}
