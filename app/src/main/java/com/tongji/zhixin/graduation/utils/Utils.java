package com.tongji.zhixin.graduation.utils;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhixin on 2018/2/2.
 */

public class Utils {

    /**
     * 设置控件在屏幕上的显示位置
     *
     * @param view 需要改变位置的控件
     * @param x    设置控件显示位置的X坐标
     * @param y    设置控件显示位置的Y坐标
     */
    public static void setLayout(View view, int x, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(view
                .getLayoutParams());
        margin.setMargins(x, y, x + margin.width, y + margin.height);
        //注意所用布局方式
        //设置控件绝对位置XY，左上角绝对位置，右下角绝对位置
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }

    public static void imageRotation(ImageView imageView, float currentAngle, float nextAngle) {
        Animation anim = new RotateAnimation(currentAngle, nextAngle, Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setFillAfter(true);  //设置保持最后状态
        anim.setDuration(800);   //单位ms
        anim.setInterpolator(new AccelerateInterpolator());  //设置插入器
        /*
        （1）LinearInterpolator：动画从开始到结束，变化率是线性变化。
        （2）AccelerateInterpolator：动画从开始到结束，变化率是一个加速的过程。
        （3）DecelerateInterpolator：动画从开始到结束，变化率是一个减速的过程。
        （4）CycleInterpolator：动画从开始到结束，变化率是循环给定次数的正弦曲线。
        （5）AccelerateDecelerateInterpolator：动画从开始到结束，变化率是先加速后减速的过程。
         */
        imageView.startAnimation(anim);


        //顺时针旋转90度，默认锚点是imageView中央，不带动画
//        imageView.setRotation(90);
        //设置旋转90度，带动画
//        imageView.animate().rotation(90);
    }

    /**
     * 红点闪烁效果
     *
     * @param imageView 闪烁的红点图片
     */
    public static void blinking(ImageView imageView) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(400);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        imageView.setAnimation(alphaAnimation);
        alphaAnimation.start();
    }

    /**
     * 实现缩放，不过缩放的是view而不是view的内容
     *
     * @param v     被缩放的view
     * @param scale 缩放倍数
     */
    public static void scaleTo(View v, float scale) {   //对整个view的缩放，而不仅仅是对view内容的缩放
        if (Build.VERSION.SDK_INT >= 11) {
            v.setScaleX(scale);
            v.setScaleY(scale);
        } else {
            float oldScale = 1;
            if (v.getTag(Integer.MIN_VALUE) != null) {
                oldScale = (Float) v.getTag(Integer.MIN_VALUE);
            }
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.width = (int) ((params.width / oldScale) * scale);
            params.height = (int) ((params.height / oldScale) * scale);
            v.setTag(Integer.MIN_VALUE, scale);
        }
    }

    public static float caculateOrientation(float currentOrientation) {

        float lastOrientation = 0;
        /*
        0~30度范围内保持直线行驶
         */
        if ((currentOrientation >= 0 && currentOrientation <= 45) || currentOrientation <= 360 &&
                currentOrientation >= 315){
            lastOrientation = 0;
            return 0;
        }
        /*
        30~60度范围内表示转角45度
         */
//        if (currentOrientation > 25 && currentOrientation < 60)
//            return 45;
        /*
        75~105度范围内表示转角90度
         */
        if (currentOrientation >=45 && currentOrientation <= 135){
            lastOrientation = 90;
            return 90;
        }

        /*
        165~195度范围内表示转角180度
         */
        if (currentOrientation >= 135 && currentOrientation <= 225)
        {
            lastOrientation = 180;
            return 180;
        }

        /*
        255~285度范围内表示转角270度
         */
        if (currentOrientation >= 225 && currentOrientation <= 315)
        {
            lastOrientation = 270;
            return 270;
        }

        /*
        120~150度范围内表示转角135度
         */
//        if (Math.abs(currentOrientation - lastOrientation) > 120 && Math.abs(currentOrientation -
//                lastOrientation) < 150)
//            if (currentOrientation - lastOrientation > 0)
//                return lastOrientation + 135f;
//            else {
//                return lastOrientation - 135f;
//            }
        /*
        210~240度范围内表示转角225度
         */
//        if (Math.abs(currentOrientation - lastOrientation) > 210 && Math.abs(currentOrientation -
//                lastOrientation) < 240)
//            if (currentOrientation - lastOrientation > 0)
//                return lastOrientation + 225f;
//            else {
//                return lastOrientation - 225f;
//            }
        /*
        300~330度范围内表示转角315度
         */
//        if (currentOrientation > 300 && currentOrientation < 330)
//            return 315;
        /*
        其他角度保持不变
         */
        return currentOrientation;
    }

    public static float caculateOrientationPocket(float angle){
        if(angle <= 0){
            if(Math.abs(angle) < 50){
                return 0;
            }
            if(Math.abs(angle + 90) < 50){
                return -90 + 360;
            }
            if(Math.abs(angle + 180) < 50){
                return 180;
            }
        }else {
            if(angle <= 45 || angle >= 315){
                return 0;
            }
            if(Math.abs(angle - 90) <= 45){
                return 90;
            }
            if(Math.abs(angle - 180) <= 45){
                return 180;
            }
            if(Math.abs(angle - 270) <= 45){
                return 270;
            }
        }
        return (angle + 360) % 360;
    }

    static float lastAngle = 0;

    public static float getAngleForOSI(float angle) {
        if (lastAngle == 0) {
            lastAngle = angle;
        } else {
            if (angle > 340 && angle < 25) {
                return 0;
            }
            if (angle > 80 && angle < 100) {
                return 90;
            }
            if (angle > 160 && angle < 200) {
                return 180;
            }
            if (angle > 250 && angle < 290) {
                return 270;
            }
            return angle;
        }
        return angle;
    }


    public static void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);     //第二个参数为延时多长时间后执行，第三个参数为执行周期
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
    }

    public static float getMaxFromFloatArray(float[] s){
        float result = s[0];
        for(float element : s){
            if(element > result){
                result = element;
            }
        }
        return result;
    }

    public  static float getMinFromFloatArray(float[] s){
        float result = s[0];
        for(float element : s){
            if(element < result){
                result = element;
            }
        }
        return result;
    }

    public static float getPeak2Peak(float[] s){
        float temp = getMaxFromFloatArray(s) - getMinFromFloatArray(s);
//        return 360 - temp > temp ? temp : 360 - temp;
        return temp;
    }

    public static float getAveFromFloatArray(float[] s){
        float result = 0;
        for(float element : s){
            result += element;
        }
        return result / s.length;
    }

    public static boolean isUp(float[] s){
        int n = 0;
        for(int i = 1; i < s.length; i++){
            if(s[i] - s[i - 1] > 0){
                n++;
            }
        }
        if(n >= s.length * 2 / 3){
            return true;
        }
        return false;
    }

    public static boolean isDown(float[] s){
        int n = 0;
        for(int i = 1; i < s.length; i++){
            if(s[i] - s[i - 1] < 0){
                n++;
            }
        }
        if(n >= s.length * 2 / 3){
            return true;
        }
        return false;
    }

    public static float angleM0(float s){
        while (s < 0){
            s = (s + 360) % 360;
        }
        return s % 360;
    }

    public static String[] room(double X, double Y){
        int x = (int) X;
        int y = (int) Y;
        /*
        result[0]表示左边,result[1]表示右边
         */
        String[] result = new String[2];
        result[0] = "";
        result[1] = "";
        if(x <= 238 && y <= 1160){
            if(y >= 1065){
                result[0] = "731";
                result[1] = "733";
            }else if(y >= 1045){
                result[0] = "729";
                result[1] = "733";
            }else if(y >= 910){
                result[0] = "729";
                result[1] = "";
            }else if(y >= 792){
                result[0] = "727";
                result[1] = "";
            }else if(y >= 680){
                result[0] = "卫生间";
                result[1] = "";
            }else if(y >= 662){
                result[0] = "卫生间";
                result[1] = "742";
            }else if(y >= 592){
                result[0] = "725";
                result[1] = "742";
            }else if(y >= 558){
                result[0] = "725";
                result[1] = "";
            }else if(y >= 547){
                result[0] = "725";
                result[1] = "728";
            }else if(y >= 436){
                result[0] = "723";
                result[1] = "728";
            }else if(y >= 430) {
                result[0] = "721";
                result[1] = "728";
            }else if(y >= 315){
                result[0] = "721";
                result[1] = "";
            }else if(y >= 200){
                result[0] = "719";
                result[1] = "";
            }else if(y >= 145){
                result[0] = "717";
                result[1] = "";
            }else if(y >= 78){
                result[0] = "717";
                result[1] = "726";
            }else {
                result[0] = "715";
                result[1] = "726";
            }
        }else if(y > 1160 && x <= 238){
          if(x < 135){
              result[0] = "748";
              result[1] = "731";
          }else if(x <= 195){
              result[0] = "750";
              result[1] = "731";
          }else {
              result[0] = "750";
              result[1] = "";
          }
        } else if(x > 850 && y <= 1060){
            if(y >= 908){
                result[0] = "702";
                result[1] = "701";
            }else if(y >= 795){
                result[0] = "704";
                result[1] = "";
            }else if(y >= 670){
                result[0] = "706";
                result[1] = "703";
            }else if(y >= 595){
                result[0] = "708";
                result[1] = "703";
            }else if(y >= 560){
                result[0] = "708";
                result[1] = "";
            }else if(y >= 435){
                result[0] = "710";
                result[1] = "705";
            }else if(y >= 325){
                result[0] = "712";
                result[1] = "707";
            }else if(y >= 317){
                result[0] = "714";
                result[1] = "707";
            }else if(y >= 205){
                result[0] = "714";
                result[1] = "卫生间";
            }else if(y >= 198){
                result[0] = "714";
                result[1] = "";
            }else if(y >= 88){
                result[0] = "716";
                result[1] = "";
            }else{
                result[0] = "718";
                result[1] = "709";
            }
        }else if(x >= 238 && x <= 900){
            if(y >= 140 && y <= 250){
                if(x <= 430){
                    result[0] = "726";
                    result[1] = "";
                }else if(x <= 455){
                    result[0] = "713";
                    result[1] = "";
                }else if(x <= 547){
                    result[0] = "713";
                    result[1] = "720";
                }else if(x <= 580){
                    result[0] = "711";
                    result[1] = "720";
                }else if(x <= 785){
                    result[0] = "711";
                    result[1] = "";
                }else if(x <= 900){
                    result[0] = "";
                    result[1] = "卫生间";
                }
            }else if(y >= 500 && y <= 660){
                if(x <= 418){
                    result[0] = "728";
                    result[1] = "742";
                }else if(x <= 455){
                    result[0] = "";
                    result[1] = "740";
                }else if(x <= 482){
                    result[0] = "724";
                    result[1] = "740";
                }else if(x <= 540){
                    result[0] = "724";
                    result[1] = "738";
                }else if(x <= 592){
                    result[0] = "724";
                    result[1] = "736";
                }else if(x <= 602){
                    result[0] = "";
                    result[1] = "736";
                }else if(x <= 665){
                    result[0] = "";
                    result[1] = "734";
                }else if(x <= 728){
                    result[0] = "";
                    result[1] = "732";
                }else if(x <= 788){
                    result[0] = "";
                    result[1] = "730";
                }else if(x <= 900){
                    result[0] = "705";
                    result[1] = "703";
                }
            }else if(y >= 1100 && y<= 1250 && x <= 733){
                 if(x <= 287){
                     result[0] = "750";
                     result[1] = "733";
                 }else if(x <= 415){
                     result[0] = "752";
                     result[1] = "733";
                 }else if(x <= 423){
                     result[0] = "752";
                     result[1] = "735";
                 }else if(x <= 543){
                     result[0] = "754";
                     result[1] = "735";
                 }else if(x <= 553){
                     result[0] = "754";
                     result[1] = "737";
                 }else if(x <= 672){
                     result[0] = "756";
                     result[1] = "737";
                 }else{
                     result[0] = "758";
                     result[1] = "739";
                 }
            }else if(y >= 1000 && y <= 1150 && x >= 733){
                if(x >= 786){
                    result[0] = "760";
                    result[1] = "701";
                }else {
                    result[0] = "758";
                    result[1] = "701";
                }
            }
        }

        /*
        判断中间竖直小道
         */
        if(x >= 370 && x <= 520 && y >= 205 && y <= 558){
            if(y <= 315){
                result[0] = "";
                result[1] = "720";
            }else if(y <= 430){
                result[0] = "";
                result[1] = "722";
            }else if(y <= 435){
                result[0] = "728";
                result[1] = "722";
            }else if(y <= 558){
                result[0] = "728";
                result[1] = "724";
            }
        }

        /*
        判断电梯
         */
        if(x >= 344 && x <= 425 && y >= 878 && y <= 912){
            result[0] = "楼梯";
            result[1] = "电梯";
        }
        if(x >= 788 && x <= 870 && y >= 840 && y <= 870){
            result[0] = "";
            result[1] = "电梯";
        }

        return result;
    }

}
