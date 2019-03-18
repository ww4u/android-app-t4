package com.megarobo.control.bean;


/**
 * 遥控机器人--爪子开合或腕运动
 */
public class JointStep {


    //移动步距
    private Step step;

    //连续运动
    private boolean continous;

    //速度百分比
    private double speed;

    //3表示腕运动，4表示爪子
    private double joint;

    public boolean isContinous() {
        return continous;
    }

    public void setContinous(boolean continous) {
        this.continous = continous;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
}
