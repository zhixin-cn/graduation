package com.tongji.zhixin.graduation.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.tongji.zhixin.graduation.activity.Graduation_Project;
import com.tongji.zhixin.graduation.orientation.PassValue;
import com.tongji.zhixin.graduation.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhixin on 2018/3/9.
 */

public class PathView extends android.support.v7.widget.AppCompatImageView {
    private Route route;
    private List<PointF> mPointFList;
    private int currentX;
    private int currentY;
    private int finalX_test;
    private int finalY_test;
    private float scale = 1.0f;

    //波纹特效参数，暂时没有用到波纹效果。
    private int waveColor;
    private int waveCount = 2;
    private float radius = 30f;    // 最外圆半径，即最大半径
    private float innerRadius = 0;  // 最内圆的半径，即最小半径
    private float[] waveDegreeArr;
    private boolean isRunning = true;
    private boolean reset = false;
    private int stepLength = 11;
    private int startPointX = 670;
    private int startPointY = 1184;
    private boolean setPoint_state = false;
    private static final int UP = 1;
    private static final int LEFT = 2;
    private static final int DOWN = 3;
    private static final int RIGHT = 4;
    private int forwardDirection = 0;
    private int cornerSize = 20;
    private double lastTheta;
    private List<PointF> coner = new ArrayList<>();
    private long lastTurn;
    private boolean isTurn = false;
    private long timeThreshold = 0;

    private void init() {
        mPointFList = new ArrayList<>();
        mPointFList.add(new PointF(startPointX, startPointY));
//        mPointFList.add(new PointF(100, 330));
//        mPointFList.add(new PointF(200, 280));
        finalX_test = 300;
        finalY_test = 340;
        route = new Route(mPointFList);
//        route = new Route();

        //对波纹特效初始化
        waveDegreeArr = new float[waveCount];
        for (int i = 0; i < waveCount; i++) {
            waveDegreeArr[i] = innerRadius + (radius - innerRadius) / waveCount * i;
        }
        coner.add(new PointF(215, 190));  //717      0
        coner.add(new PointF(917, 190));  //716      1
        coner.add(new PointF(917, 577));  //708      2
        coner.add(new PointF(917, 1108)); //760      3
        coner.add(new PointF(766, 1108)); //739      4
        coner.add(new PointF(766, 1184)); //758      5
        coner.add(new PointF(215, 1184)); //731      6
        coner.add(new PointF(215, 577));  //725      7
        coner.add(new PointF(436, 575));  //740      8
        coner.add(new PointF(436, 187));  //720      9
    }

    public PathView(Context context) {
        super(context);
        init();
    }

    public PathView(Context context, AttributeSet attrs) {   //该构造方法要存在，否则报错
        super(context, attrs);
        init();
    }

    public void reset() {
        route.reset();
        reset = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(scale, scale, 400, 525);
        route.drawPath(canvas);
        invalidate();
    }

    public void draw(PassValue passValue) {
        double theta = passValue.getOrientation();
        Log.d("theta", "theta:" + String.valueOf(theta) + "," + "lastTheta:" + String.valueOf
                (lastTheta));
        if (lastTurn == 0) {
            lastTheta = theta;
            lastTurn = System.currentTimeMillis();
        }
        int xPre = route.getFinalX();
        int yPre = route.getFinalY();
        float tempStepLength = 0;
        if (!isTurn)
            tempStepLength = stepLength;
        else {
            isTurn = false;
            tempStepLength = stepLength * 1.5f;
        }
        double xNew = xPre + tempStepLength * Math.sin(Math.toRadians(theta));
        //未做步长估计，一步体现在屏幕上的距离为10个像素点
        double yNew = yPre - tempStepLength * Math.cos(Math.toRadians(theta));

        float temp1 = (float) Math.abs(theta - lastTheta);
        float temp2 = 360 - temp1;
        /*
        关闭地图限制，需要关闭检测转向以及修正坐标
         */
        /*
        检测转向
         */
        if ((temp1 < temp2 ? temp1 : temp2) > 50f && System.currentTimeMillis() - lastTurn > timeThreshold) {
            Log.d("1321", "zhuan" + "theta:" + String.valueOf(theta) + "," + "lastTheta:" + String.valueOf
                    (lastTheta));
            if((xNew >= 160 && xNew <= 500 && yNew >= 700 && yNew <= 1045) || (xNew >= 700 && xNew <= 935 && yNew >= 700 && yNew <= 1030)){
                //不转向拉回
                Log.d("bbbbbb", "buzhu");
            }else {
                int conerNum = getTheConer(xNew, yNew);
                Log.d("bbbbbb", String.valueOf(conerNum));
                xNew = coner.get(conerNum).x;
                yNew = coner.get(conerNum).y;
//                if(conerNum == 2 || conerNum == 6 || conerNum == 7){
//                    timeThreshold = 8000;
//                }else {
//                    timeThreshold = 2000;
//                }
                lastTurn = System.currentTimeMillis();
                isTurn = true;
            }
            lastTheta = theta;
        }
        /*
        判断行走方向
         */
        getCurrentDirection(xNew, yNew, route.getLastStepX(), route.getLastStepY());
        /*
        修正坐标，使之在地图内，location不需要注释
         */
        float[] location = new float[2];
        if(!isTurn){
            float[] correct = correctPoint(xNew, yNew);
            location[0] = correct[0];
            location[1] = correct[1];
            route.addPoint(new PointF(location[0], location[1]));
        }else {
            location[0] = (float) xNew;
            location[1] = (float) yNew;
            route.addPoint(new PointF((float)xNew, (float)yNew));
        }
        /*
        关闭地图限制，需要开启以下代码
         */
//        route.addPoint(new PointF((float) xNew, (float) yNew));
//        location[0] = (float) xNew;
//        location[1] = (float) yNew;

        String[] room = Utils.room(location[0], location[1]);
        Graduation_Project.getMainActivity().setRoom(room);
        this.invalidate();  //刷新界面，实现自身不断更新，以使得结果是最新的展示在屏幕上
//        finalX_test += 10;
//        finalY_test += 10;
    }

    public void setStepLength(int a) {
        stepLength = a;
    }


    /**
     * 该方法的闪烁效果并不好，太垃圾了
     *
     * @param canvas
     * @param centerX
     * @param centerY
     */
    private void drawWave(Canvas canvas, int centerX, int centerY) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waveColor = 0xFFFF1493;
        paint.setColor(waveColor);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < waveCount; i++) {
            paint.setAlpha((int) (255 - 255 * waveDegreeArr[i] / radius));
            canvas.drawCircle(centerX, centerY, waveDegreeArr[i], paint);
        }
        for (int i = 0; i < waveDegreeArr.length; i++) {
            if ((waveDegreeArr[i] += 4) > radius) {
                waveDegreeArr[i] = innerRadius;
            }
        }
        if (isRunning) {
            postInvalidateDelayed(300);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                currentX = (int) event.getX();
                currentY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x2 = (int) event.getX();
                int y2 = (int) event.getY();
                this.scrollBy(currentX - x2, currentY - y2);
                //scrollBy移动的是view的内容，里面需要传入负值，画布内容始终存在，
                // 移动的是手机屏幕，所以内容需要右移时，即我们需要看到左边的内容，那么手机需要往左移
                currentY = y2;
                currentX = x2;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = 0;
        int height = 0;
        //获得宽度MODE
        int modeW = MeasureSpec.getMode(widthMeasureSpec);
        //获得宽度的值
        if (modeW == MeasureSpec.AT_MOST) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (modeW == MeasureSpec.EXACTLY) {
            width = widthMeasureSpec;
        }
        if (modeW == MeasureSpec.UNSPECIFIED) {
            width = 800;
        }
        //获得高度MODE
        int modeH = MeasureSpec.getMode(height);
        //获得高度的值
        if (modeH == MeasureSpec.AT_MOST) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        }
        if (modeH == MeasureSpec.EXACTLY) {
            height = heightMeasureSpec;
        }
        if (modeH == MeasureSpec.UNSPECIFIED) {
            //ScrollView和HorizontalScrollView
            height = 1370;
        }
        //设置宽度和高度
        setMeasuredDimension(width, height);
    }

    public int getFinalX() {
        return finalX_test;
    }

    public void setFinalX(int finalX) {
        this.finalX_test = finalX;
    }

    public int getFinalY() {
        return finalY_test;
    }

    public void setFinalY(int finalY) {
        this.finalY_test = finalY;
    }

    public float getScale() {
        return scale;
    }

    public void setSetPoint_state(boolean setPoint_state) {
        this.setPoint_state = setPoint_state;
        route.setGetPoint_State(setPoint_state);
    }

    public boolean isSetPoint_state() {
        return setPoint_state;
    }

    public void setScale(float scale) {
        this.scale = scale;
        route.setScale(scale);
    }

    public void setPoint(int x, int y) {
        route.setPoint(x, y);
        this.invalidate();
    }

    public void setStartPoint(int x, int y) {
        this.startPointX = x;
        this.startPointY = y;
        mPointFList.clear();
        mPointFList.add(new PointF(startPointX, startPointY));
        route = new Route(mPointFList);
    }

    public int[] getPoint() {
        return route.getPoint();
    }

    private float[] correctPoint(double X, double Y) {
        int x = (int) X;
        int y = (int) Y;
        float[] result = new float[2];
        result[0] = x;
        result[1] = y;

        /*
        限制在地图区域
         */
        if (y <= 170 || y >= 1210) {
            if (y <= 170)
                result[1] = 187;
            else
                result[1] = 1184;
        }
        if (x <= 195 || x >= 935) {
            if (x <= 195)
                result[0] = 215;
            else
                result[0] = 917;
        }
        /*
        右下角760
         */
        if (x >= 800 && y >= 1160) {
            if (Math.abs(x - 800) < Math.abs(y - 1160))
                result[0] = 766;
            else
                result[1] = 1108;
        }

        if (x >= 238 && x <= 238 + cornerSize && (((y >= 205 + cornerSize && y <= 558 - cornerSize) ||
                (y >= 592 + cornerSize && y <= 788 - cornerSize)) || (y >= 1000 + cornerSize && y <= 1160 - cornerSize)))
            result[0] = 215; //左边竖直长边
        else if (x >= 733 - cornerSize && x <= 733 && y <= 1160 - cornerSize && y >= 1060 + cornerSize)
            result[0] = 766; //760转角处竖直边
        else if (x >= 900 - cornerSize && x <= 900 && ((y >= 205 + cornerSize && y <= 560 -
                cornerSize) || (y >= 595 + cornerSize && y <= 700 - cornerSize) || (y >= 990 + cornerSize && y <= 1060 - cornerSize)))
            result[0] = 917; //右边竖直长边
        else if (x > 235 + cornerSize && x < 900 - cornerSize) {
            if(x >= 415 - cornerSize && x <= 455 + cornerSize){
                if(y >= 205 && y <= 558){
                    result[0] = 436;
                }
            }else {
                if (y > 205 && y < 205 + cornerSize)
                    result[1] = 187;
                else if (y > 560 - cornerSize && y < 560)
                    result[1] = 577;
                else if (y > 594 && y < 594 + cornerSize)
                    result[1] = 577;
            }
        } else if (x > 235 + cornerSize && x < 733 - cornerSize && y > 1158 - cornerSize && y <
                1158)
            result[1] = 1184;
        else if (x > 733 + cornerSize && x < 900 - cornerSize && y > 1058 - cornerSize && y < 1058)
            result[1] = 1108;
        else if (x > 235 && x < 235 + cornerSize && y > 205 && y < 205 + cornerSize) {
            if (Math.abs(x - 235) < Math.abs(y - 205))
                result[0] = 215;
            else
                result[1] = 187;
        } else if (x > 900 - cornerSize && x < 900 && y > 205 && y < 205 + cornerSize) {
            if (Math.abs(x - 900) < Math.abs(y - 205))
                result[0] = 917;
            else
                result[1] = 187;
        } else if (x > 235 && x < 235 + cornerSize && y > 560 - cornerSize && y < 560) {
            if (Math.abs(x - 235) < Math.abs(y - 560))
                result[0] = 215;
            else
                result[1] = 577;
        } else if (x > 900 - cornerSize && x < 900 && y > 560 - cornerSize && y < 560) {
            if (Math.abs(x - 900) < Math.abs(y - 560))
                result[0] = 917;
            else
                result[1] = 577;
        } else if (x > 235 && x < 235 + cornerSize && y > 594 && y < 594 + cornerSize) {
            if (Math.abs(x - 235) < Math.abs(y - 594))
                result[0] = 215;
            else
                result[1] = 577;
        } else if (x > 900 - cornerSize && x < 900 && y > 594 && y < 594 + cornerSize) {
            if (Math.abs(x - 900) < Math.abs(y - 594))
                result[0] = 917;
            else
                result[1] = 577;
        } else if (x > 235 && x < 235 + cornerSize && y > 1158 - cornerSize && y < 1158) {
            if (Math.abs(x - 235) < Math.abs(y - 1158))
                result[0] = 215;
            else
                result[1] = 1184;
        } else if (x > 733 - cornerSize && x < 733 && y > 1158 - cornerSize && y < 1158) {
            if (Math.abs(x - 733) < Math.abs(y - 1158))
                result[0] = 766;
            else
                result[1] = 1184;
        } else if (x > 900 - cornerSize && x < 900 && y > 1058 - cornerSize && y < 1058) {
            if (Math.abs(x - 900) < Math.abs(y - 1058))
                result[0] = 917;
            else
                result[1] = 1108;
        } else if (x > 733 - cornerSize && x < 733 && y > 1058 && y < 1058 + cornerSize)
            result[0] = 766;
        else if (x > 733 && x < 733 + cornerSize && y > 1058 - cornerSize && y < 1058)
            result[1] = 1108;
        else if (x > 733 - cornerSize && x < 733 && y > 1058 - cornerSize && y < 1058) {
            result[0] = 766;
            result[1] = 1108;
        }

        return result;
    }

    private void getCurrentDirection(double currentx, double currenty, int last_x, int last_y) {
        int current_x = (int) currentx;
        int current_y = (int) currenty;
        if (current_x > last_x) {
            forwardDirection = RIGHT;
        } else if (current_x < last_x) {
            forwardDirection = LEFT;
        } else if (current_y > last_y) {
            forwardDirection = DOWN;
        } else if (current_y < last_y) {
            forwardDirection = UP;
        }
    }

    private int getTheConer(double X, double Y) {
        int x = (int) X;
        int y = (int) Y;
        int result = 0;
        double d = distance(x, y, (int) coner.get(0).x, (int) coner.get(0).y);
        for (int i = 1; i < coner.size(); i++) {
            if (distance(x, y, (int) coner.get(i).x, (int) coner.get(i).y) < d) {
                d = distance(x, y, (int) coner.get(i).x, (int) coner.get(i).y);
                result = i;
            }
        }
        return result;
    }

    private double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public int getStartPointX() {
        return startPointX;
    }

    public int getStartPointY() {
        return startPointY;
    }
}
