package com.megarobo.control.blockly.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

/**
 * 遥控机器人--设置
 */
public class BlocklyFile {


    private String action;

    //名称
    private String name;

    //内容
    private String contents;

    //响应
    private int ret;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public static BlocklyFile parseFile(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        BlocklyFile file = new BlocklyFile();
        file.setAction(result.getString("action"));
        file.setName(result.getString("name"));
        file.setContents(result.getString("contents"));
        file.setRet(result.getIntValue("ret"));

        return file;
    }
}
