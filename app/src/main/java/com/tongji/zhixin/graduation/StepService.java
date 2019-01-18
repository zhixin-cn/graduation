package com.tongji.zhixin.graduation;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.tongji.zhixin.graduation.UI.UpdateUiCallBack;
import com.tongji.zhixin.graduation.orientation.OrientationListener;
import com.tongji.zhixin.graduation.stepcount.StepListener;

public class StepService extends Service {

    private static String TAG = "StepService";
    private SensorManager sensorManager;
    private OrientationListener orientationListener;
    private StepListener stepListener;
    private UpdateUiCallBack updateUiCallBack;
    private StepBinder stepBinder = new StepBinder();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private float orientationFromOSI;
    private boolean isDebug = false;

    private ValueCenter valueCenter = new ValueCenter() {
        private float preOrientation;
        @Override
        public void stepChange(int step) {
            editor.putInt("step", step);
            editor.commit();
            updateUiCallBack.UpdateNumberOfSteps();
            Log.e("stepChange", "stepChange执行");
        }

        @Override
        public void orientationChange(float orientation) {
//            editor.putFloat("orientation", orientationFromOSI);

            editor.putFloat("orientation", orientation);

            editor.putFloat("preOrientation", preOrientation);
            editor.commit();
            updateUiCallBack.UpdateOrientation();

            preOrientation = orientation;

//            preOrientation = orientationFromOSI;
        }

        @Override
        public void pathChange() {
            updateUiCallBack.UpdatePicture();
        }
    };

    public StepService() {
    }

    public void registerCallBack(UpdateUiCallBack updateUiCallBack){
        this.updateUiCallBack = updateUiCallBack;
    }

    public class StepBinder extends Binder{
        public StepService getStepService(){
            return StepService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.stepBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "StepService onDestroy方法执行");
        sensorManager.unregisterListener(orientationListener);
        sensorManager.unregisterListener(stepListener);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "StepService onStartCommand方法执行");
                Sensor sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                Sensor sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                Sensor sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                Sensor sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                Sensor sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                Sensor sensorOrientation = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
                Sensor sensorLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
                Sensor sensorHuaWeiStepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                Sensor sensorHuaWeiStepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

                /*
                注册stepListener
                 */
                sensorManager.registerListener(stepListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(stepListener, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(stepListener, sensorRotationVector, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(stepListener, sensorGyro, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(stepListener, sensorHuaWeiStepCounter, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(stepListener, sensorHuaWeiStepDetector, SensorManager.SENSOR_DELAY_GAME);

                /*
                注册orientationListener
                 */
                sensorManager.registerListener(orientationListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(orientationListener, sensorMagnetic, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(orientationListener, sensorRotationVector, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(orientationListener, sensorGyro, SensorManager.SENSOR_DELAY_UI);
                sensorManager.registerListener(orientationListener, sensorGravity, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(orientationListener, sensorProximity, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(orientationListener, sensorOrientation, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(orientationListener, sensorLinearAcceleration, SensorManager.SENSOR_DELAY_GAME);

                /*
                注册gyroscopeListener，适合转大弯
                 */
//                sensorManager.registerListener(gyroscopeListener, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(gyroscopeListener, sensorMagnetic, SensorManager.SENSOR_DELAY_GAME);
//                sensorManager.registerListener(gyroscopeListener, sensorGyro, SensorManager.SENSOR_DELAY_GAME);
                orientationListener.run();

//                orientationSensor.init(1.0, 1.0, 1.0);
//                orientationSensor.on(0);
            }
        }).start();

        //语句需写在return前面，这里需注意一下，容易忽略
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "StepService onCreate方法执行");
        init();
    }
    private void init(){
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationListener = new OrientationListener();
        stepListener = new StepListener();
        stepListener.setDebug(isDebug);
        sharedPreferences = getSharedPreferences("data", Activity.MODE_PRIVATE);
        this.editor = this.sharedPreferences.edit();
        stepListener.initValueCenter(valueCenter);
        orientationListener.initValueCenter(valueCenter);
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
        stepListener.setDebug(debug);
    }

    public boolean isDebug() {
        return isDebug;
    }
    public void setSensitivity(float sensitivity) {
        stepListener.setSensitivity(sensitivity);
    }
}
