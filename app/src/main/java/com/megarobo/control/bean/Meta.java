package com.megarobo.control.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

/**
 * 搜索设备---设备元数据
 */
public class Meta {

    //型号
    private String model;

    //序列号
    private String sn;

    //别名
    private String alias;

    //是否有爪子
    private boolean hasHand;

    //是否被占用
    private boolean link;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public boolean isHasHand() {
        return hasHand;
    }

    public void setHasHand(boolean hasHand) {
        this.hasHand = hasHand;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isLink() {
        return link;
    }

    public void setLink(boolean link) {
        this.link = link;
    }

    /**
     {
     "command": "meta",
     "alias": "test",
     "model": "AABB",
     "has_hand": "true",
     "sn": "111111"
     }
     * @param jsonStr
     * @return
     */
    public static Meta parseMeta(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        Meta meta = new Meta();
        meta.setAlias(result.getString("alias"));
        meta.setModel(result.getString("model"));
        meta.setHasHand(result.getBooleanValue("has_hand"));
        meta.setLink(result.getBooleanValue("link"));
        meta.setSn(result.getString("sn"));

        return meta;
    }
}
