package com.megarobo.control.bean;

/**
 * 遥控机器人---前后左右上下
 */
public class Step {

    private double x;

    private double y;

    private double z;

    //连续运动
    private boolean continous;

    //速度百分比
    private double speed;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

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
