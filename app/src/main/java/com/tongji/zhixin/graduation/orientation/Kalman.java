package com.tongji.zhixin.graduation.orientation;

/**
 * Created by zhixin on 2018/4/10.
 */

public class Kalman {
    private float A = 1.0f;
    private float H = 1.0f;
    private float B;
    private float Zt;
    private float St;
    private float St_1 = 0;
    private float R = 0.005f;   //预测误差协方差，陀螺仪固有属性
    private float Q = 0.03f;    //测量误差协方差
    private float Ut;
    private float Pt;   //后验估计协方差
    private float Pt_1 = 0.01f;
    private float Kt;

    public float kalman(float orientationMag, float gyroVelocity_Z, float dt){
        //参数赋值
        Ut = (float) Math.toDegrees(gyroVelocity_Z);
        Zt = orientationMag;
        B = dt;

        //预测方程
        St = A * St_1 + B * Ut;      //由前一时刻状态得到当前状态
        if(St < 0){
            St = St + 360;
        }
        St = St % 360;
        Pt = A * Pt_1 * (1 / A) + R;    //前一时刻后验估计协方差推算出当前时刻后验估计协方差

        //更新方程
        Kt = Pt * (1 / H) * (1 / (Q + H * Pt * (1 / H)));      //由推算出来的协方差更新卡尔曼增益
        St_1 = St + Kt * (Zt - H * St);     //由推算出来的当前状态，结合卡尔曼增益以及测量值更新当前状态
        if(St_1 < 0){
            St_1 = St_1 + 360;
        }
        St_1 = St_1 % 360;
        Pt_1 = Pt - Kt * H * Pt;   //

        return St_1;
    }

}
