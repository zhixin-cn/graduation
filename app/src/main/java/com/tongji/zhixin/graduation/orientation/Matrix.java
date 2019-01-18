package com.tongji.zhixin.graduation.orientation;

/**
 * Created by zhixin on 2018/4/11.
 */

public class Matrix {
    public static float[] phone2world(float[] rotationMatrix, float[] b){
        float[] R = rotationMatrix;
        float[] result = new float[3];
        result[0] = b[0] * R[0] + b[1] * R[1] + b[2] * R[2];
        result[1] = b[0] * R[3] + b[1] * R[4] + b[2] * R[5];
        result[2] = b[0] * R[6] + b[1] * R[7] + b[2] * R[8];
        return result;
    }
}
