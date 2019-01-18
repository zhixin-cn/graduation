package com.tongji.zhixin.graduation.orientation;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.tongji.zhixin.graduation.FileOperation;
import com.tongji.zhixin.graduation.ValueCenter;
import com.tongji.zhixin.graduation.activity.Graduation_Project;
import com.tongji.zhixin.graduation.utils.Utils;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by zhixin on 2018/3/13.
 */

public class OrientationListener implements SensorEventListener {
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private ValueCenter valueCenter;
    private float[] rotationMatrixFrom_ROTATION_VECTOR = new float[9];
    private boolean start = false;
    private boolean wait = true;
    private float lastOrientation;
    private long startTime;
    private float startOrientation;
    private float[] accMagValues = new float[3];
    private float[] gravity = new float[3];
    private double[] gravityUnit = new double[3];
    private float angelGyro = 0;
    private float[] type_orientation_values = new float[3];
    private Timer fuseTimer = new Timer();
    private FileOperation fileOperation = new FileOperation();

    /*
    陀螺仪参数
     */
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final double[] deltaRotationVector = new double[4];
    private double timestamp;
    private static final float EPSILON = 0.000000001f;
    private float[] rotationMatrixRemap = new float[9];
    private static final int FILTER_LENGTH = 50;
    private float[] remapValueMeanFilter = new float[FILTER_LENGTH];
    public static final float FILTER_COEFFICIENT = 1.0f;
    private float comeGyro;

    private float[] quaternionFromVector = new float[4];
    private long lastTime;
    private float lastGyro;
    private long duration;
    private Kalman kalman = new Kalman();
    private float kalmanOrientation;
    private float magOrientationWithGyro;
    private float[] deltaRotationMatrix = new float[9];
    private float[] rotationMatrixFromQuaternion = new float[9];
    private float[] gyro3DAngle = new float[3];
    private float[] R = new float[9];

    private float[] magFilterX = new float[30];
    private float[] magFilterY = new float[30];
    private float[] magFilterZ = new float[30];
    private float[] magLevel = new float[3];
    private float[] angleFilter = new float[50];
    private float[] angleFilterPing = new float[100];
    private float anglePocketFromGyro;
    private float anglePing;
    private float[] accTran = new float[3];
    private float[] linearAcc = new float[3];
    private float orientationPocketFromMag;
    private double angle3;
    private double angleFromGravity;
    private float first = 0;
    private float second = 0;
    private float firstSubSecond = 0;
    private float preFirstSubSecond = 0;
    private float[] window = new float[200];
    private long lastTurnWindow;
    private float lastWindowAngle;
    private float magOrientation;
    private float lastMagOrientation;

    private boolean startWindow = false;
    private boolean endWindow = false;
    private float minWindow;
    private float maxWindow;
    private float ppWindow;
    private int numUp = 0;
    private int numDown = 0;
    private float preWindowAngle;
    private float[] behind = new float[window.length / 2];
    private float[] before = new float[window.length / 2];

    /*
    参数ping
     */
    private float[] windowPing = new float[150];
    private boolean startWindowPing = false;
    private boolean endWindowPing = false;
    private float minWindowPing;
    private float maxWindowPing;
    private float ppWindowPing;
    private int numUpPing = 0;
    private int numDownPing = 0;
    private float preWindowAnglePing;
    private float[] behindPing = new float[windowPing.length / 2];
    private float[] beforePing = new float[windowPing.length / 2];


    /*
    values[0]  ：azimuth 方向角，但用（磁场+加速度）得到的数据范围是（-180～180）,
    也就是说，0表示正北，90表示正东，180/-180表示正南，-90表示正西。
    而直接通过方向感应器数据范围是（0～359）360/0表示正北，90表示正东，180表示正南，270表示正西。
    values[1]  pitch 倾斜角  即由静止状态开始，前后翻转
    values[2]  roll 旋转角 即由静止状态开始，左右翻转
    */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accelerometerValues = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticFieldValues = event.values.clone();

                System.arraycopy(magFilterX, 0, magFilterX, 1, 29);
                System.arraycopy(magFilterY, 0, magFilterY, 1, 29);
                System.arraycopy(magFilterZ, 0, magFilterZ, 1, 29);

                float[] mag = new float[3];
                if (magFilterX[29] == 0 || magFilterY[29] == 0 || magFilterZ[29] == 0) {
                } else {
                    float sumX = 0;
                    float sumY = 0;
                    float sumZ = 0;

                    for (int i = 0; i < 30; i++) {
                        sumX += magFilterX[i];
                        sumY += magFilterY[i];
                        sumZ += magFilterZ[i];
                    }
                    sumX = sumX / 30;
                    sumY = sumY / 30;
                    sumZ = sumZ / 30;
                    mag[0] = sumX;
                    mag[1] = sumY;
                    mag[2] = sumZ;
                    SensorManager.getRotationMatrix(R, null, gravity, mag);
                    float[] Rout = new float[9];
                    SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager
                            .AXIS_Z, Rout);
                    float[] temp = new float[3];
                    SensorManager.getOrientation(Rout, temp);
                    magOrientation = (float) Math.toDegrees(temp[0]);
//                    Log.d("Rout", String.valueOf(magOrientation));
                }
                magFilterX[0] = magneticFieldValues[0];
                magFilterY[0] = magneticFieldValues[1];
                magFilterZ[0] = magneticFieldValues[2];
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotationMatrixFrom_ROTATION_VECTOR,
                        event.values);     //这个利用陀螺仪的旋转角度得到旋转矩阵

                SensorManager.remapCoordinateSystem(rotationMatrixFrom_ROTATION_VECTOR,
                        SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z, rotationMatrixRemap);

                SensorManager.getQuaternionFromVector(quaternionFromVector, event.values);   //获取四元数
                break;
            case Sensor.TYPE_GRAVITY:
                gravity = event.values.clone();
                double a = Math.sqrt(gravity[0] * gravity[0] + gravity[1] * gravity[1] +
                        gravity[2] * gravity[2]);
                gravityUnit[0] = gravity[0] / a;
                gravityUnit[1] = gravity[1] / a;
                gravityUnit[2] = gravity[2] / a;
                break;
            case Sensor.TYPE_GYROSCOPE:
                long time = System.currentTimeMillis();
                if (timestamp != 0) {
                    double dT = (time - timestamp) / 1000;

                    // Axis of the rotation sample, not normalized yet.
                    float axisX = event.values[0];
                    float axisY = event.values[1];
                    float axisZ = event.values[2];

                    gyro3DAngle[0] += Math.toDegrees(axisX) * dT;
                    gyro3DAngle[1] += Math.toDegrees(axisY) * dT;
                    gyro3DAngle[2] += Math.toDegrees(axisZ) * dT;

                    angleFromGravity = gyro3DAngle[0] * gravityUnit[0] + gyro3DAngle[1] *
                            gravityUnit[1] + gyro3DAngle[2] * gravityUnit[2];
                    angleFromGravity = (angleFromGravity + 360) % 360;
//                    Log.e("ff", String.valueOf(angleFromGravity));

                    // Calculate the angular speed of the sample
                    float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY +
                            axisZ * axisZ);
//                    Log.e("ss2", String.valueOf(omegaMagnitude));

                    // Normalize the rotation vector if it's big enough to get the axis
                    // (that is, EPSILON should represent your maximum allowable margin of error)
                    if (omegaMagnitude > EPSILON) {
                        angelGyro += Math.toDegrees(axisZ) * dT;   //陀螺仪积分角度，-180度到+180度

                        System.arraycopy(windowPing, 0, windowPing, 1, windowPing.length - 1);
                        windowPing[0] = anglePing;

                        System.arraycopy(angleFilter, 0, angleFilter, 1, angleFilter.length - 1);
                        System.arraycopy(angleFilterPing, 0, angleFilterPing, 1, angleFilterPing
                                .length - 1);
                        /*
                        平端模式下滤波
                         */
                        if (angleFilterPing[angleFilterPing.length - 1] == 0) {
                        } else {
                            float sum = 0;
                            for (float element : angleFilterPing) {
                                sum += element;
                            }
                            anglePing = sum / angleFilterPing.length;

//                            anglePing = Utils.angleM0(anglePing);
//                            comeGyro = (anglePing + 360) % 360;
//                            comeGyro = 360 - anglePing;
                            Log.d("1411", "angleGyro:" + String.valueOf(angelGyro) + "anglePing:" + String.valueOf(anglePing));
                        }
                        angleFilterPing[0] = angelGyro;

                        if (anglePing > 0) {
                            comeGyro = 360 - anglePing;
                        } else {
                            comeGyro = -anglePing;
                        }


                        while (comeGyro < 0 || comeGyro > 360) {
                            comeGyro = (comeGyro + 360) % 360;
                        }

                        /*
                        口袋模式下滤波
                         */
                        if (angleFilter[angleFilter.length - 1] == 0) {
                        } else {
                            float sum = 0;
                            for (float element : angleFilter) {
                                sum += element;
                            }
//                            anglePocketFromGyro = Utils.angleM0(sum / angleFilter.length);
                            anglePocketFromGyro = sum / angleFilter.length;
//                            fileOperation.writeTxtToFile(String.valueOf(anglePocketFromGyro),
//                                    fileOperation.getFilePath(), "name.txt");
                            System.arraycopy(window, 0, window, 1, window.length - 1);
                            window[0] = anglePocketFromGyro;

                            /*
                            *****************************************************************
                             */
//                            fileOperation.writeTxtToFile(String.valueOf(anglePocketFromGyro),
// fileOperation.getFilePath(), "A.txt");
                        }
                        angleFilter[0] = gyro3DAngle[1];


//                        angleFilter[0] = (float) angleFromGravity;

                        float s = ((orientationPocketFromMag - startOrientation) + 360) % 360;
//                        anglePocketFromGyro = (anglePocketFromGyro + s) / 2;

//                        Log.e("ppp", String.valueOf(anglePocketFromGyro));
                    }
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                    double thetaOverTwo = (omegaMagnitude * dT / 2.0f);
                    double sinThetaOverTwo = Math.sin(thetaOverTwo);
                    double cosThetaOverTwo = Math.cos(thetaOverTwo);
                    deltaRotationVector[0] = sinThetaOverTwo * axisX;
                    deltaRotationVector[1] = sinThetaOverTwo * axisY;
                    deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                    deltaRotationVector[3] = cosThetaOverTwo;

                    /*
                获取两个旋转矩阵，做一下对比，是一样的
                 */
//                    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix,
//                            deltaRotationVector);
                    double[] angle;
                    angle = getAngleFromQuaternion(deltaRotationVector);

                    angle3 += angle[2];
                }
                timestamp = time;
                break;
            case Sensor.TYPE_PROXIMITY:
//                Log.e("proximity", String.valueOf(event.values[2])); //其他两个值为0
                break;
            case Sensor.TYPE_ORIENTATION:
                type_orientation_values = event.values.clone();   //360度
                if (!start) {
                    type_orientation_values[0] = type_orientation_values[0] - startOrientation;
                    if (type_orientation_values[0] < 0) {
                        type_orientation_values[0] += 360f;
                    }
                    type_orientation_values[0] %= 360;
                }
                magOrientationWithGyro = type_orientation_values[0];
                if (Math.abs(magOrientationWithGyro - comeGyro) > 20) {
                    magOrientationWithGyro = comeGyro;
                }
                Log.e("ttt", String.valueOf(type_orientation_values[0]));
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                linearAcc = event.values.clone();

                accTran[0] = R[0] * linearAcc[0] + R[1] * linearAcc[1] + R[2] * linearAcc[2];
                accTran[1] = R[3] * linearAcc[0] + R[4] * linearAcc[1] + R[5] * linearAcc[2];
                accTran[2] = R[6] * linearAcc[0] + R[7] * linearAcc[1] + R[8] * linearAcc[2];

//                Log.e("acc", String.valueOf(accTran[0]) + " " + String.valueOf(accTran[1]));
                break;
            default:
                break;
        }
    }


    public void run() {
        if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                .getMainActivity().getResources().getString(com.tongji.zhixin.graduation.R.string
                        .ping))) {
//            fuseTimer.scheduleAtFixedRate(new CaculateOrientationTask(), 800, 30);  //30毫秒执行一次

            Vibrator vibrator = (Vibrator) Graduation_Project.getMainActivity().getSystemService
                    (VIBRATOR_SERVICE);
            long[] patter = {100, 300};  //静止100毫秒，振动500毫秒
            vibrator.vibrate(patter, -1);  //-1表示不循环
            fuseTimer.scheduleAtFixedRate(new pingTask(), 100, 100);
        }
        if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                .getMainActivity().getResources().getString(com.tongji.zhixin.graduation.R.string
                        .pocket))) {
            Vibrator vibrator = (Vibrator) Graduation_Project.getMainActivity().getSystemService
                    (VIBRATOR_SERVICE);
            long[] patter = {100, 300};  //静止100毫秒，振动500毫秒
            vibrator.vibrate(patter, -1);  //-1表示不循环
            fuseTimer.scheduleAtFixedRate(new OrientationTask(), 100, 100);
        }
    }

    class OrientationTask extends TimerTask {
        @Override
        public void run() {
//            /*
//             *********************************************************滑动窗口检测完整过程
//             */
            System.arraycopy(window, window.length / 2, behind, 0, behind.length);
            System.arraycopy(window, 0, before, 0, before.length);
            float tempBefore = Utils.getPeak2Peak(before);    //左边一半峰峰值
            float tempBehind = Utils.getPeak2Peak(behind);    //右边一半峰峰值
            if (!startWindow && tempBefore - tempBehind > 20 && !endWindow) {
                //进入变化阶段
                startWindow = true;
            } else if (!endWindow && tempBehind - tempBefore > 20 && startWindow) {
                //进入结束阶段
                endWindow = true;
                startWindow = false;
            }
            if (startWindow && !endWindow) {
                minWindow = Utils.getMinFromFloatArray(window);
                maxWindow = Utils.getMaxFromFloatArray(window);
                if (Utils.isUp(window)) {
                    numUp++;
                }
                if (Utils.isDown(window)) {
                    numDown++;
                }
            }
            if (endWindow) {
                startWindow = false;
                endWindow = false;
                ppWindow = maxWindow - minWindow;
                if (Math.abs(ppWindow - 90) < 20) {
                    ppWindow = 90;
                } else if (Math.abs(ppWindow - 180) < 25) {
                    ppWindow = 180;
                }
                if (numUp > numDown) {
                    preWindowAngle = Utils.angleM0(preWindowAngle - ppWindow);
                }
                if (numUp < numDown) {
                    preWindowAngle = Utils.angleM0(preWindowAngle + ppWindow);
                }
                valueCenter.orientationChange(preWindowAngle);
                Log.d("2113", String.valueOf(preWindowAngle));
                numUp = 0;
                numDown = 0;
            }
//            fileOperation.writeTxtToFile(String.valueOf(anglePocketFromGyro) + "," + String
//                    .valueOf(preWindowAngle), fileOperation.getFilePath(), "po.txt");
        }
    }

    class pingTask extends TimerTask{
        @Override
        public void run() {
//             /*
//             *********************************************************滑动窗口检测完整过程
//             */
            System.arraycopy(windowPing, windowPing.length / 2, behindPing, 0, behindPing.length);
            System.arraycopy(windowPing, 0, beforePing, 0, beforePing.length);
            float tempBefore = Utils.getPeak2Peak(beforePing);    //左边一半峰峰值
            float tempBehind = Utils.getPeak2Peak(behindPing);    //右边一半峰峰值
            if (!startWindowPing && tempBefore - tempBehind > 20 && !endWindowPing) {
                //进入变化阶段
                startWindowPing = true;
            } else if (!endWindowPing && tempBehind - tempBefore > 20 && startWindowPing) {
                //进入结束阶段
                endWindowPing = true;
                startWindowPing = false;
            }
            if (startWindowPing && !endWindowPing) {
                minWindowPing = Utils.getMinFromFloatArray(windowPing);
                maxWindowPing = Utils.getMaxFromFloatArray(windowPing);
                if (Utils.isUp(windowPing)) {
                    numUpPing++;
                }
                if (Utils.isDown(windowPing)) {
                    numDownPing++;
                }
            }
            if (endWindowPing) {
                startWindowPing = false;
                endWindowPing = false;
                ppWindowPing = maxWindowPing - minWindowPing;
                if (Math.abs(ppWindowPing - 90) < 40) {
                    ppWindowPing = 90;
                } else if (Math.abs(ppWindowPing - 180) < 25) {
                    ppWindowPing = 180;
                }
                if (numUpPing > numDownPing) {
                    preWindowAnglePing = Utils.angleM0(preWindowAnglePing + ppWindowPing);
                }
                if (numUpPing < numDownPing) {
                    preWindowAnglePing = Utils.angleM0(preWindowAnglePing - ppWindowPing);
                }
                valueCenter.orientationChange(preWindowAnglePing);
                Log.d("2113", String.valueOf(preWindowAnglePing));
                numUpPing = 0;
                numDownPing = 0;
            }
        }
    }

    class CaculateOrientationTask extends TimerTask {
        @Override
        public void run() {
            //调用getRotationMatrix获得旋转矩阵R
            float[] orientationFromMag = new float[3];
            float[] outRotationMatrix = new float[9];
            SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager
                    .AXIS_MINUS_Z, outRotationMatrix);
            SensorManager.getOrientation(outRotationMatrix, orientationFromMag);
            Log.e("acc", String.valueOf(Math.toDegrees(orientationFromMag[0])));

            System.arraycopy(remapValueMeanFilter, 0, remapValueMeanFilter, 1, FILTER_LENGTH - 1);
            if (remapValueMeanFilter[FILTER_LENGTH - 1] == 0) {
            } else {
                float oriRe;
                float sum = 0;
                for (float element : remapValueMeanFilter) {
                    sum += element;
                }
                oriRe = sum / FILTER_LENGTH;
                oriRe = (oriRe + 360) % 360;

                Log.e("remap", String.valueOf(oriRe));

                orientationPocketFromMag = oriRe;
                if (startOrientation == 0) {
                    startOrientation = oriRe;
                }
            }
            remapValueMeanFilter[0] = (float) Math.toDegrees(orientationFromMag[0]);

            accMagValues[0] = (float) Math.toDegrees(accMagValues[0]); //得到角度值，角度变化范围是0~180,0~-180
            Log.e("ooo", String.valueOf(accMagValues[0]));
            if (accMagValues[0] < 0) {
                accMagValues[0] = accMagValues[0] + 360;   //将values[0]范围变为0~360
            }
            if (wait) {
                if (!start) {
                    startTime = System.currentTimeMillis();
                    start = true;
                }
                //初始1秒等待
//                if (System.currentTimeMillis() - startTime > 1000) {
//                    start = true;
                wait = false;
//                } else {
//                    return;
//                }
            }

            if (start) {
//                startOrientation = type_orientation_values[0];   //360度，平端模式
                //口袋模式

                if (startOrientation != 0) {
                    start = false;
                    /*
                    提醒准备妥当
                    */
                    Vibrator vibrator = (Vibrator) Graduation_Project.getMainActivity()
                            .getSystemService(VIBRATOR_SERVICE);
                    long[] patter = {100, 300};  //静止100毫秒，振动500毫秒
                    vibrator.vibrate(patter, -1);  //-1表示不循环

                    /*
                    toast需在主线程中使用，虽然android studio并未报错
                    */
                    Graduation_Project.getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(Graduation_Project.getMainActivity(),
                                    "初始化完成", Toast
                                            .LENGTH_LONG);
                            Utils.showMyToast(toast, 500);
                        }
                    });
                    return;
                }

                lastOrientation = 0;
//                if (accMagValues[0] - startOrientation < 0) {
//                    valueCenter.orientationChange(accelerometerValues[0] - startOrientation +
// 360);
//                } else {
//                    valueCenter.orientationChange(accMagValues[0] - startOrientation);
// 瞿博所说的初始化工作
//                }
                return;
            }
            sendOrientation();
        }
    }

    float lastAngle = 0;
    long lastTime1 = 0;
    long updateTime = 0;

    private void sendOrientation() {
        float angleFromMag = type_orientation_values[0];   //360度

//        float angle = FILTER_COEFFICIENT * comeGyro + (1.0f - FILTER_COEFFICIENT) * angleFromMag;

        float returnAngle = Utils.caculateOrientation(comeGyro);
        Log.d("1414", String.valueOf(comeGyro));
        Log.d("1413", String.valueOf(returnAngle));

        if (returnAngle == lastOrientation && Math.abs(returnAngle - comeGyro)< 30) {
            duration = System.currentTimeMillis() - lastTime;
            if (duration > 5000) {

                Log.d("quan", "angleGyro:" + String.valueOf(angelGyro) + "," + "lastGyro:" + String.valueOf(lastGyro));
                angelGyro = lastGyro;

                Log.e("duration", String.valueOf(duration));
                lastTime = System.currentTimeMillis();
            }
        } else {
            lastTime = System.currentTimeMillis();
            lastOrientation = returnAngle;
            if (returnAngle > 0 && returnAngle < 180) {
                lastGyro = -returnAngle;
            } else {
                if(Math.abs(returnAngle) < 45){
                    lastGyro = 0;
                }else {
                    lastGyro = 360 - returnAngle;
                }
            }
        }

//        Utils.caculateOrientation(kalmanOrientation)
//        可选参数
//        magOrientationWithGyro
//        comeGyro
        if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                .getMainActivity().getResources().getString(com.tongji.zhixin.graduation.R.string
                        .ping))) {
//            fileOperation.writeTxtToFile(String.valueOf(comeGyro), fileOperation.getFilePath(),
//                    "p.txt");
            valueCenter.orientationChange(Utils.caculateOrientation(comeGyro % 360));  //平端模式
            Log.d("1321", "comeGyro:" + String.valueOf(comeGyro) + "," + "Util:" + String.valueOf(Utils.caculateOrientation(comeGyro % 360)));
        }

        if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                .getMainActivity().getResources().getString(com.tongji.zhixin.graduation.R.string
                        .pocket))) {
            //result
            //anglePocketFromGyro
            //angleFromGravity


            float result = ((orientationPocketFromMag - startOrientation) + 360) % 360;
//        valueCenter.orientationChange(result);
//        currentAngle = Utils.caculateOrientationPocket(anglePocketFromGyro);


            anglePocketFromGyro = Utils.angleM0(anglePocketFromGyro);
            float temp = (360 - Math.abs(anglePocketFromGyro - lastAngle)) > Math.abs
                    (anglePocketFromGyro - lastAngle) ? Math.abs(anglePocketFromGyro - lastAngle) :
                    (360 - Math.abs(anglePocketFromGyro - lastAngle));
            if (lastTime1 == 0) {
                valueCenter.orientationChange(0);
                lastTime1 = System.currentTimeMillis();
            }
            if (temp > 60) {
                if (anglePocketFromGyro > lastAngle) {
                    lastAngle = Utils.angleM0(lastAngle + 90);
                    valueCenter.orientationChange(lastAngle);
                } else {
                    lastAngle = Utils.angleM0(lastAngle - 90);
                    valueCenter.orientationChange(Utils.angleM0(lastAngle));
                }
                lastTime1 = System.currentTimeMillis();
                updateTime = lastTime1;
            } else if (temp > 60 && firstSubSecond == 0) {
                gyro3DAngle[1] = lastAngle;
            }
//            口袋模式，陀螺仪
            if (Math.abs(anglePocketFromGyro - lastAngle) < 40 && System.currentTimeMillis() -
                    updateTime > 5000) {
                gyro3DAngle[1] = lastAngle;
                updateTime = System.currentTimeMillis();
            }
        }
    }

    private float[] getRotationMatrixFromQuaternion(float[] quaternion) {
        float[] R = new float[9];
        float x = quaternion[0];
        float y = quaternion[1];
        float z = quaternion[2];
        float w = quaternion[3];

        /*
        R = [
            1-2*y*y-2*z*z   2*x*y-2*z*w   2*x*z+2*y*w
            2*x*z+2*z*w    1-2*x*x-2*z*z  2*y*z-2*x*w
            2*x*z-2*y*w  2*y*z+2*x*w  1-2*x*x-2*z*y*y
        ]
         */
        R[0] = 1 - 2 * y * y - 2 * z * z;
        R[1] = 2 * x * y - 2 * z * w;
        R[2] = 2 * x * z + 2 * y * w;

        R[3] = 2 * x * z + 2 * z * w;
        R[4] = 1 - 2 * x * x - 2 * z * z;
        R[5] = 2 * y * z - 2 * x * w;

        R[6] = 2 * x * z - 2 * y * w;
        R[7] = 2 * y * z + 2 * x * w;
        R[8] = 1 - 2 * x * x - 2 * y * y;

        return R;
    }

    private double[] getAngleFromQuaternion(double[] qFromGyro) {
        double[] result = new double[3];
        double[] q = new double[4];
        q[0] = qFromGyro[3];
        q[1] = qFromGyro[0];
        q[2] = qFromGyro[1];
        q[3] = qFromGyro[2];
        result[0] = Math.atan2(2 * (q[0] * q[1] + q[2] * q[3]), 1 - 2 * q[1] * q[1] - 2 *
                q[2] * q[2]);
        result[1] = Math.asin(2 * (q[0] * q[2] - q[1] * q[3]));
        result[2] = Math.atan2(2 * (q[0] * q[3] + q[1] * q[2]), 1 - 2 * q[2] * q[2] - 2 *
                q[3] * q[3]);
        return result;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //nothing to do
    }

    public void initValueCenter(ValueCenter valueCenter) {
        this.valueCenter = valueCenter;
    }

}
