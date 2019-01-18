package com.tongji.zhixin.graduation.utils;

import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhixin on 2018/3/28.
 */

public class MyToast {
    public static void showMyToast(final Toast toast, final int cnt) {
        final Timer timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        },0,3000);     //第二个参数为延时多长时间后执行，第三个参数为执行周期
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
    }
}
