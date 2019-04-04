package com.megarobo.control.bean;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.megarobo.control.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 遥控机器人---点集合
 */
public class DataSet {

    private Map<String,Point> pointMap;

    public Map<String, Point> getPointMap() {
        return pointMap;
    }

    public void setPointMap(Map<String, Point> pointMap) {
        this.pointMap = pointMap;
    }

    public static DataSet parseDataSet(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        JSONObject result = JSON.parseObject(jsonStr);
        if(result == null || result.isEmpty()){
            return null;
        }

        JSONArray jsonArray = result.getJSONArray("points");

        Map<String,Point> pointMap = new HashMap<>();
        for(int i=0; i<jsonArray.size();i++){
            JSONObject pointJson = jsonArray.getJSONObject(i);
            String name  = pointJson.getString("name");
            Pose pose = Pose.parsePose(pointJson.getString("pose"));

            Point point = new Point();
            point.setName(name);
            point.setPose(pose);
            pointMap.put(name,point);
        }

        DataSet dataSet = new DataSet();
        dataSet.setPointMap(pointMap);
        return dataSet;
    }
}
