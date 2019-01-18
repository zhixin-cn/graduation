package com.tongji.zhixin.graduation.stepcount;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.tongji.zhixin.graduation.FileOperation;
import com.tongji.zhixin.graduation.ValueCenter;
import com.tongji.zhixin.graduation.activity.Graduation_Project;

/**
 * Created by zhixin on 2018/3/13.
 */

public class StepListener implements SensorEventListener {
    float[] oriValues = new float[3];   //存放三轴数据
    float gravityNew = 0;
    private StepCount stepCount = new StepCount();
    private ValueCenter valueCenter;
    private float[] linearAcceleration = new float[3];
    private float[] R = new float[9];
    private float[] realValues = new float[3];
    private float[] tempArrays = new float[10];
    private float valueAfterThreshold;
    private float valueAfterThresholdLast;
    private float sum = 0;
    private float avg;

    private float[] gyroOriValue = new float[3];
    private static int FILTER_LENGTH = 3;
    private float[] gyroXAngularSpeed = new float[FILTER_LENGTH];
    private float preXSpeed;
    private float currentSpeed;
    private boolean change = false;
    private float lastHW = 0;
    private FileOperation fileOperation = new FileOperation();
    private float sensitivity = 0.8f;

    private long testTimeStamp;
    private int testCounter = 0;
    private boolean isDebug = false;

    public void initValueCenter(ValueCenter valueCenter) {
        this.valueCenter = valueCenter;
        stepCount.initValueCenter(valueCenter);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                oriValues = event.values.clone();

                gravityNew = (float) Math.sqrt(oriValues[0] * oriValues[0]
                        + oriValues[1] * oriValues[1] + oriValues[2] * oriValues[2]);
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:   //记得注册该传感器
                linearAcceleration = event.values.clone();
                if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                        .getMainActivity().getResources().getString(com.tongji.zhixin.graduation
                                .R.string.ping))) {
                    stepByAcc();
                }
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(R, event.values);
                break;
            case Sensor.TYPE_GYROSCOPE:
//                if(testCounter == 0){
//                    testTimeStamp = System.currentTimeMillis();
//                }
//                if(System.currentTimeMillis() - testTimeStamp < 1000){
//                    testCounter++;
//                    Log.d("testFre", String.valueOf(testCounter));
//                }
                gyroOriValue = event.values.clone();
                System.arraycopy(gyroXAngularSpeed, 0, gyroXAngularSpeed, 1, FILTER_LENGTH - 1);
                gyroXAngularSpeed[0] = gyroOriValue[0];
                float sum = 0;
                for (float element : gyroXAngularSpeed) {
                    sum += element;
                }
                currentSpeed = sum / FILTER_LENGTH;
                float temp = currentSpeed;
//                fileOperation.writeTxtToFile(String.valueOf(temp), fileOperation.getFilePath(),
// "u.txt");
//                if (currentSpeed >= 1) {
//                    currentSpeed = 5;
//                } else if(currentSpeed <= -0.8){
//                    currentSpeed = -5;
//                }else {
//                    currentSpeed = preXSpeed;
//                }
//                if (currentSpeed != preXSpeed) {
//                    //判断发生了一步，存在2倍
//                    stepCount.countStep();
//                }

                if (currentSpeed <= -1) {
                    currentSpeed = 0;
                } else if (currentSpeed >= 1) {
                    currentSpeed = 1;
                } else {
                    currentSpeed = preXSpeed;
                }
//                float ttt = 0f;
//                if(currentSpeed > preXSpeed){
//                    ttt = 1.0f;
//                }else {
//                    ttt = 0f;
//                }
//                fileOperation.writeTxtToFile(String.valueOf(temp) + "," + String.valueOf
//                        (currentSpeed) + "," + String.valueOf(ttt), fileOperation.getFilePath(), "uuu.txt");
                if (currentSpeed > preXSpeed) {
                    if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                            .getMainActivity().getResources().getString(com.tongji.zhixin
                                    .graduation.R.string.pocket))) {
                        if (!change) {
                            stepCount.countStep();
                        }
                    }
                }
                preXSpeed = currentSpeed;
                break;
            case Sensor.TYPE_STEP_COUNTER:
                if (lastHW == 0) {
                    lastHW = event.values[0];
                } else {
                    if (event.values[0] != lastHW) {
                        if (!change) {
//                            change = true;
                        }
//                        if (Graduation_Project.getMainActivity().getMode().equals
//                                (Graduation_Project.getMainActivity().getResources().getString
//                                        (com.tongji.zhixin.graduation.R.string.pocket))) {
//                            stepCount.countStep();
//                            lastHW = event.values[0];
//                        }
                    }
                }
                Log.e("HW", String.valueOf(event.values[0]));
                break;
            default:
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //
    }

    private void stepByAcc() {

        realValues[0] = R[0] * linearAcceleration[0] + R[1] * linearAcceleration[1] + R[2] *
                linearAcceleration[2];
        realValues[1] = R[3] * linearAcceleration[0] + R[4] * linearAcceleration[1] + R[5] *
                linearAcceleration[2];
        realValues[2] = R[6] * linearAcceleration[0] + R[7] * linearAcceleration[1] + R[8] *
                linearAcceleration[2];

//        Log.e("aa", String.valueOf(realValues[2]));
        System.arraycopy(tempArrays, 0, tempArrays, 1, 9);
        if (tempArrays[9] == 0) {
            tempArrays[0] = realValues[2];
        } else {
            tempArrays[0] = realValues[2];
            for (float element : tempArrays) {
                sum += element;
            }
            avg = sum / 10;
            sum = 0;
            if (avg >= sensitivity) {
                valueAfterThreshold = 3;
                //通过上升沿判断步子发生
                if (valueAfterThreshold > valueAfterThresholdLast) {
                    stepCount.countStep();
                }
                valueAfterThresholdLast = valueAfterThreshold;
            } else if (avg <= -sensitivity) {
                valueAfterThreshold = -3;
                valueAfterThresholdLast = valueAfterThreshold;
            } else {
                valueAfterThreshold = valueAfterThresholdLast;
            }
        }
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
        stepCount.setDebug(debug);
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }
}
