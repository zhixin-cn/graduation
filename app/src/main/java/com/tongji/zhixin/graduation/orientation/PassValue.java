package com.tongji.zhixin.graduation.orientation;

/**
 * Created by zhixin on 2018/3/15.
 */

public class PassValue {
    private double orientation;  //0~360表示
    private float startOrientation;

    public double getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
            this.orientation = orientation;
    }

    public void setOrientation(double orientation) {
        this.orientation = orientation;
    }

    public float getStartOrientation() {
        return startOrientation;
    }

    public void setStartOrientation(float startOrientation) {
        this.startOrientation = startOrientation;
        this.orientation = startOrientation;
    }
}
