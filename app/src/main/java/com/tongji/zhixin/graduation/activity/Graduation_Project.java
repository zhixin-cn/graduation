package com.tongji.zhixin.graduation.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tongji.zhixin.graduation.R;
import com.tongji.zhixin.graduation.StepService;
import com.tongji.zhixin.graduation.UI.GetPointDialog;
import com.tongji.zhixin.graduation.UI.PathView;
import com.tongji.zhixin.graduation.UI.SensivityDialog;
import com.tongji.zhixin.graduation.UI.StartPointDialog;
import com.tongji.zhixin.graduation.UI.StepLengthDialog;
import com.tongji.zhixin.graduation.UI.UpdateUiCallBack;
import com.tongji.zhixin.graduation.orientation.PassValue;
import com.tongji.zhixin.graduation.utils.Utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Graduation_Project extends AppCompatActivity {
    private TextView textViewStep;
    private TextView leftRoomTextView;
    private TextView rightRoomTextView;
    private PathView picture;
    private PassValue passValue;
    private Button startButton;
    private SeekBar mSeekbar;
    private ButtonListener buttonListener;
    private RadioGroup choice;
    private String choiceState;
    private StepService stepService;
    private static Graduation_Project mainActivity;
    private static final int REQUEST_CODE = 1;

    public static final int UPDATE_PICTURE = 1;
    public static final int NUM_OF_STEP = 2;
    public static final int ANGLE = 3;
    private SharedPreferences sharedPreferences;
    private float orientation;
    private String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.VIBRATE};
    private boolean serviceState = false;
    private GetPointDialog getPointDialog;
    private StartPointDialog startPointDialog;
    private Button getPointButton;
    private StepLengthDialog stepLengthDialog;
    private SensivityDialog sensivityDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);    //设置全屏，隐藏状态栏
        hideStatusNavigationBar(false);
        setContentView(R.layout.activity_main);
        init();
        checkPermission();
        startOrientation();
    }

    public void setRoom(String[] room){
        leftRoomTextView.setText(room[0]);
        rightRoomTextView.setText(room[1]);
    }

    private void startOrientation(){
        int x = picture.getStartPointX();
        int y = picture.getStartPointY();
        if (x > 195 && x < 235)
            passValue.setStartOrientation(0);
        else if (x > 900 && x < 935)
            passValue.setStartOrientation(180);
        else if (y > 170 && y < 205)
            passValue.setStartOrientation(90);
        else if (y > 1158 && y < 1210)
            passValue.setStartOrientation(270);
        else if(y > 560 && y < 594)
            passValue.setStartOrientation(90);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                int uiOptions =
                        //布局位于状态栏下方
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        //隐藏导航栏
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                if (Build.VERSION.SDK_INT >= 19) {
                    uiOptions |= 0x00001000;
                } else {
                    uiOptions |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
                }
                getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            }
        });


//        menu.add(1, Menu.FIRST, 1, "设置起点");   //四个参数，groupid, itemid, orderid, title/
        // /Menu.FIRST对应itemid为1
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.first:
                startPointDialog.show();
//                Log.d("setStartPoint", "start");
                break;
            case R.id.second:
                picture.setSetPoint_state(!picture.isSetPoint_state());
                break;
            case R.id.third:
                passValue.setStartOrientation((passValue.getStartOrientation() + 180) % 360);
                break;
            case R.id.fourth:
                if (serviceState) {
                    stepService.setDebug(!stepService.isDebug());
                } else {
                    Toast.makeText(Graduation_Project.this, "StepService未启动，请点击开始按钮", Toast
                            .LENGTH_SHORT).show();
                }
                break;
            case R.id.fifth:
                stepLengthDialog.show();
                break;
            case R.id.sixth:
                sensivityDialog.show();
                break;
            default:
                return false;
        }
        hideStatusNavigationBar(false);
        return true;
//        return super.onOptionsItemSelected(item);
    }

    private void checkPermission() {
        boolean result = true;
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager
                    .PERMISSION_GRANTED) {
                result = false;
                Log.e("check", String.valueOf(i));
            }
        }
        if (!result) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        //写SD卡权限申请失败！
                        Toast toast = Toast.makeText(this, "SD卡写入权限申请失败！", Toast.LENGTH_LONG);
                        Utils.showMyToast(toast, 600);
                    }
                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        //读SD卡权限申请失败！
                        Toast toast = Toast.makeText(this, "SD卡读取权限申请失败！", Toast.LENGTH_LONG);
                        Utils.showMyToast(toast, 600);
                    }
                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    } else {
                        //振动权限申请失败！
                        Toast toast = Toast.makeText(this, "振动权限申请失败！", Toast.LENGTH_LONG);
                        Utils.showMyToast(toast, 600);
                    }
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public Graduation_Project() {
        mainActivity = Graduation_Project.this;
    }

    public static Graduation_Project getMainActivity() {
        return mainActivity;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        bindStepService();
    }

    @Override
    protected void onPause() {
//        unbindStepService();
        super.onPause();
    }

    private void init() {

        Intent intent = getIntent();
        choiceState = intent.getStringExtra("mode");
        Log.d("mode", choiceState);

        leftRoomTextView = findViewById(R.id.leftRoom);
        rightRoomTextView = findViewById(R.id.rightRoom);
        choice = findViewById(R.id.choice);
        mSeekbar = findViewById(R.id.scale);
        textViewStep = findViewById(R.id.step);
        picture = findViewById(R.id.picture);
        startButton = findViewById(R.id.startButton);
        buttonListener = new ButtonListener();
        startButton.setOnClickListener(buttonListener);
        passValue = new PassValue();
        sharedPreferences = getSharedPreferences("data", Activity.MODE_PRIVATE);
        mSeekbar.setOnSeekBarChangeListener(new SeekBarLisrener());
        startButton.setOnTouchListener(new ButtonOnTouchListener());

        getPointDialog = new GetPointDialog(Graduation_Project.this, R.style
                .Theme_AppCompat_Dialog, getPointDialogClickListener);
        startPointDialog = new StartPointDialog(Graduation_Project.this, R.style
                .Theme_AppCompat_Dialog, setStartPointDialogClickListener);
        stepLengthDialog = new StepLengthDialog(Graduation_Project.this, R.style
                .Theme_AppCompat_Dialog, stepLengthListener);
        sensivityDialog = new SensivityDialog(Graduation_Project.getMainActivity(), R.style
                .Theme_AppCompat_Dialog, sensitivityListener);
        getPointButton = findViewById(R.id.setPoint);
        getPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPointDialog.show();
            }
        });
    }

    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PICTURE:
                    picture.draw(passValue);
                    break;
                case NUM_OF_STEP:
                    textViewStep.setText(String.valueOf(sharedPreferences.getInt("step", 0)));
                    break;
                case ANGLE:
                    float preOrientation = sharedPreferences.getFloat("preOrientation", 0);
                    orientation = sharedPreferences.getFloat("orientation", preOrientation);
                    passValue.setOrientation((passValue.getStartOrientation() + orientation) % 360);
                    break;
                default:
                    break;
            }
            return true;
            //这里的返回值是控制消息是否继续传递给handler中的handleMessage方法执行
            //如果返回了true,那么handler中的handleMessge方法是不会被执行的,注意是handler中的handleMessage方法
        }
    });

    private UpdateUiCallBack updateUiCallBack = new UpdateUiCallBack() {
        @Override
        public void UpdateNumberOfSteps() {
            Log.e("stepMessage", "步数消息发送");
            Message message = handler.obtainMessage();
            message.what = NUM_OF_STEP;
            handler.sendMessage(message);
        }

        @Override
        public void UpdatePicture() {
            Message message = handler.obtainMessage();
            message.what = UPDATE_PICTURE;
            handler.sendMessage(message);
        }

        @Override
        public void UpdateOrientation() {
            Message message = handler.obtainMessage();
            message.what = ANGLE;
            handler.sendMessage(message);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService.StepBinder binder = (StepService.StepBinder) service;
            stepService = binder.getStepService();
            stepService.registerCallBack(updateUiCallBack);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            //do nothing
        }
    };

    private void bindStepService() {
        bindService(new Intent(this, StepService.class), this.serviceConnection, Context
                .BIND_AUTO_CREATE);
    }

    private void unbindStepService() {
        unbindService(this.serviceConnection);
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.startButton:
                    Button button = findViewById(v.getId());
                    if (serviceState) {
                        serviceState = false;
                        button.setText("开始");
                        Log.e("service", "stop");
//                        Resources resources = Graduation_Project.getMainActivity().getResources();
//                        startButton.setBackground(resources.getDrawable(R.mipmap.play));
                        Intent endIntent = new Intent(Graduation_Project.this, StepService.class);
                        stopService(endIntent);
                        unbindStepService();   //需要通过stopSevice以及unbindStepService这两个方法结束service
//                        Resources resources = Graduation_Project.getMainActivity().getResources();
//                        startButton.setBackground(resources.getDrawable(R.mipmap.play));
                    } else {
                        serviceState = true;
                        Log.e("service", "run");
                        button.setText("停止");
//                        Resources resources = Graduation_Project.getMainActivity().getResources();
//                        startButton.setBackground(resources.getDrawable(R.mipmap.stop));
//                        Log.e("service", "stop");
                        Toast toast = Toast.makeText(Graduation_Project.this, "稍等...", Toast
                                .LENGTH_LONG);
                        Utils.showMyToast(toast, 500);
                        final Intent startIntent = new Intent(Graduation_Project.this,
                                StepService.class);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startService(startIntent);
                                bindStepService();
                            }
                        }, 2000);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class ButtonOnTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.startButton:
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (isServiceRunning(Graduation_Project.this, "com.tongji.zhixin" +
                                ".graduation.StepService")) {

                        } else {

                        }
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private class SeekBarLisrener implements SeekBar.OnSeekBarChangeListener {
        /*
        拖动条进度改变的时候调用
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.scale:
                    float scale = (float) progress / (float) seekBar.getMax();
                    scale = scale * 2.0f;
                    if (scale < 0.1) {
                        scale = 0.1f;
                    }
//                    Log.d("aaqq", "progress:" + String.valueOf(progress));
//                    Log.d("aaqq", "Max:" + String.valueOf(seekBar.getMax()));
//                    Log.d("aaqq", "scale:" + String.valueOf(scale));
                    picture.setScale(scale);
                    break;
                default:
                    break;
            }
        }

        /*
        拖动条开始拖动的时候调用
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        /*
        拖动条停止拖动的时候调用
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context.getSystemService(Context
                .ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager
                .RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    private View.OnClickListener getPointDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ok:
                    if ("".equals(getPointDialog.x.getText().toString().trim()) || "".equals
                            (getPointDialog.y.getText().toString().trim())) {
                        Toast toast = Toast.makeText(Graduation_Project.getMainActivity(),
                                "未设置坐标", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    } else {
                        int x = Integer.parseInt(getPointDialog.x.getText().toString().trim());
                        int y = Integer.parseInt(getPointDialog.y.getText().toString().trim());
                        picture.setPoint(x, y);
                        Log.d("dialog", "ok点击");
                        getPointDialog.cancel();
                    }
                    Log.d("dialog", String.valueOf(picture.getPoint()[0]) + "," + String.valueOf
                            (picture.getPoint()[1]));
                    hideStatusNavigationBar(false);
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener setStartPointDialogClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.setStartPoint:
                    if ("".equals(startPointDialog.xEditText.getText().toString().trim()) | ""
                            .equals(startPointDialog.yEditText.getText().toString().trim())) {
                        Toast toast = Toast.makeText(Graduation_Project.getMainActivity(),
                                "未设置起点", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    } else {
                        int x = Integer.parseInt(startPointDialog.xEditText.getText().toString()
                                .trim());
                        int y = Integer.parseInt(startPointDialog.yEditText.getText().toString()
                                .trim());
                        if (x > 195 && x < 235)
                            passValue.setStartOrientation(0);
                        else if (x > 900 && x < 935)
                            passValue.setStartOrientation(180);
                        else if (y > 170 && y < 205)
                            passValue.setStartOrientation(90);
                        else if (y > 1158 && y < 1210)
                            passValue.setStartOrientation(270);
                        else if(y > 560 && y < 594)
                            passValue.setStartOrientation(90);
                        picture.setStartPoint(x, y);
                        startPointDialog.cancel();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private View.OnClickListener stepLengthListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.steplengthbutton:
                    String temp = stepLengthDialog.stepLengthEditText.getText().toString().trim();
                    if ("".equals(temp)) {
                        Toast.makeText(Graduation_Project.this, "未输入步长", Toast.LENGTH_SHORT).show();
                    } else
                        picture.setStepLength(Integer.parseInt(temp));
                    stepLengthDialog.cancel();
                    break;
                default:
                    break;
            }
        }
    };

    private View.OnClickListener sensitivityListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sensivityButton:
                    String temp = sensivityDialog.sensivityEditText.getText().toString().trim();
                    if ("".equals(temp))
                        Toast.makeText(Graduation_Project.this, "灵敏度不能为空值，默认值0.8", Toast
                                .LENGTH_SHORT).show();
                    else {
                        if (!serviceState)
                            Toast.makeText(Graduation_Project.this, "启动stepService后设置灵敏度", Toast
                                    .LENGTH_SHORT).show();
                        else
                            stepService.setSensitivity(Float.parseFloat(temp));
                    }
                    sensivityDialog.cancel();
                    break;
                default:
                    break;
            }
            hideStatusNavigationBar(false);
        }
    };

    private void hideStatusNavigationBar(boolean show) {
        if (show) {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//            uiFlags |= 0x00001000;
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        } else {
            int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            uiFlags |= 0x00001000;
            getWindow().getDecorView().setSystemUiVisibility(uiFlags);
        }
    }

    public String getMode() {
        return choiceState;
    }

    /*
另外一种实现handler的方法
 */
    //    public static Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UPDATE_PICTURE:
//                    picture.draw();
//                    break;
//                case NUM_OF_STEP:
//                    textViewStep.setText(String.valueOf(msg.arg1));
//                    break;
//                case ANGLE:
//                    angle = (Angle) msg.obj;
//                    textView.setText(String.valueOf(angle.angle));
//                    Utils.imageRotation(compass, angle.currentAngle, angle.nextAngle);
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
}
