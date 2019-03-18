package com.megarobo.control.bean;


import com.alibaba.fastjson.JSON;
import com.megarobo.control.utils.Utils;

import java.util.List;

/**
 * 遥控机器人---点集合
 */
public class DataSet {

    private List<Point> pointArrayList;

    public List<Point> getPointArrayList() {
        return pointArrayList;
    }

    public void setPointArrayList(List<Point> pointArrayList) {
        this.pointArrayList = pointArrayList;
    }

    public static DataSet parseDataSet(String jsonStr){
        if(!Utils.isNotEmptyString(jsonStr)){
            return null;
        }

        List<Point> pointList = JSON.parseArray(jsonStr,Point.class);
        if(pointList == null || pointList.isEmpty()){
            return null;
        }

        DataSet dataSet = new DataSet();
        dataSet.setPointArrayList(pointList);

        return dataSet;
    }
}
