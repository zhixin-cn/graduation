package com.tongji.zhixin.graduation.UI;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.tongji.zhixin.graduation.R;
import com.tongji.zhixin.graduation.activity.Graduation_Project;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by zhixin on 2018/3/9.
 */

public class Route {
    private Path path;
    private Path trianglePath;
    private List<PointF> pointFList;
    private int finalX;
    private int finalY;
    private int startPoint_X;
    private int startPoint_Y;
    private Paint paint;
    private float scale = 1.0f;
    private float pathSize = 8.0f;
    private float circleSize = 10.0f;
    private Bitmap map;
    private Bitmap getPoint;
    private Bitmap currentLocation;
    private float mapSize = 0.1f;
    private int xP = 500;
    private int yP = 500;
    private int getPointBitmapWidth;
    private int getPointBitmapHeight;
    private boolean getPoint_State = false;
    private int lastStepX;
    private int lastStepY;
    private int currentPW;
    private int currentPH;
    private int currentLocationSize = 5;


    //该构造方法目前暂未使用，如需使用，请补充完整
    public Route() {
        path = new Path();
        pointFList = new ArrayList<>();
    }

    public Route(List<PointF> pointFS) {
        pointFList = new ArrayList<>();
        paint = new Paint();
        pointFList = pointFS;
        path = new Path();
        /*
        设置地图
         */
        BitmapDrawable temp = (BitmapDrawable) ContextCompat.getDrawable(Graduation_Project
                .getMainActivity(), R.mipmap.map);
        BitmapDrawable getPointTemp = (BitmapDrawable) ContextCompat.getDrawable
                (Graduation_Project.getMainActivity(), R.mipmap.add);
        getPoint = getPointTemp.getBitmap();
        getPointBitmapWidth = getPoint.getWidth() / 8;
        getPointBitmapHeight = getPoint.getHeight() / 8;
        BitmapDrawable temp1 = (BitmapDrawable) ContextCompat.getDrawable(Graduation_Project
                .getMainActivity(), R.mipmap.currentlocation);
        currentLocation = temp1.getBitmap();
        currentPW = currentLocation.getWidth() / currentLocationSize;
        currentPH = currentLocation.getHeight() / currentLocationSize;

        map = temp.getBitmap();
        trianglePath = new Path();
        path.moveTo(pointFList.get(0).x, pointFList.get(0).y);
        for (int i = 0; i < pointFS.size(); i++) {
            if (i == 0) {
                startPoint_X = (int) pointFS.get(i).x;
                startPoint_Y = (int) pointFS.get(i).y;
            }
            if (i != 0) {
                path.lineTo(pointFS.get(i).x, pointFS.get(i).y);
            }
            if (i == pointFS.size() - 1) {
                finalX = (int) pointFS.get(i).x;
                finalY = (int) pointFS.get(i).y;
                setLastStepX(finalX);
                setLastStepY(finalY);
            }
        }
    }

    public void addPoint(PointF point) {   //加点之后需要调用自己的drawLine方法
        pointFList.add(point);
        path.lineTo(point.x, point.y);
        /*
        设置上一步位置
         */
        setLastStepX(getFinalX());
        setLastStepY(getFinalY());

        setFinalX((int) point.x);
        setFinalY((int) point.y);
    }

    public void reset() {
        PointF firstPoint = pointFList.get(0);
        pointFList.clear();
        pointFList.add(firstPoint);
        path.reset();  //重置path，使得里面的内容为空，这一步骤竟然花了我一下午，哎，都是泪啊！！！！！
        path.moveTo(firstPoint.x, firstPoint.y);
        finalX = (int) firstPoint.x;
        finalY = (int) firstPoint.y;
    }

    public void drawPath(Canvas canvas) {

        /*
        画地图
         */
        drawMap(canvas);
        /*
        画起始点
         */
        drawStartPoint(canvas);
        /*
        画路线
         */
        drawRoute(canvas);
        /*
        画终点
         */
        drawEndPoint(canvas);
        /*
        画终点图标
         */
        drawCurrentLocation(canvas);
        /*
        画取点标志
         */
        if (getPoint_State) {
            drawPoint(canvas);
        }
    }

    private void drawMap(Canvas canvas) {
        Rect mTopSrcRect = new Rect(0, 0, map.getWidth(), map.getHeight());
        //第一个Rect代表要绘制的bitmap的区域
        Rect mTopDestRect = new Rect(60, 0, map.getWidth(), map.getHeight());
        //第二个Rect代表要将bitmap绘制在屏幕上哪个位置
//        canvas.scale(0.1f, 0.1f, 400,525);
        canvas.drawBitmap(map, mTopSrcRect, mTopDestRect, paint);
//        canvas.scale(10f, 10f, 400,525);
    }

    private void drawStartPoint(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);
        canvas.drawCircle(startPoint_X, startPoint_Y, circleSize / scale, paint);
    }

    private void drawRoute(Canvas canvas) {
//        paint.setStrokeJoin(Paint.Join.ROUND);   //这个平滑方法貌似没有用
        CornerPathEffect cornerPathEffect = new CornerPathEffect(25);  //设置平滑的度数
        paint.setPathEffect(cornerPathEffect);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);  //设置抗锯齿，图像看起来更加顺滑
        paint.setStrokeWidth(pathSize / scale);

        /*
        setShadowLayer
        第一个参数：模糊半径，值越大模糊半径越大；
        第二个参数指阴影向右移动多少像素；
        第三个参数指阴影乡下移动多少参数
         */
//        paint.setShadowLayer(2f, 2f, 2f, Color.DKGRAY);  //给线条添加阴影
        canvas.drawPath(path, paint);
    }

    private void drawEndPoint(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.RED);
        Log.i("finalX", String.valueOf(getFinalX()));
        canvas.drawCircle(getFinalX(), getFinalY(), circleSize / scale, paint);
    }

    private void drawPoint(Canvas canvas) {
        Rect src = new Rect(0, 0, getPoint.getWidth(), getPoint.getHeight());
//        Rect des = new Rect(xP - getPointBitmapWidth / 2, yP - getPointBitmapHeight / 2,
//                getPointBitmapWidth, getPointBitmapHeight);
        Rect des = new Rect(xP - getPointBitmapWidth / 2, yP - getPointBitmapHeight / 2, xP -
                getPointBitmapWidth / 2 + getPointBitmapWidth, yP - getPointBitmapHeight / 2 +
                getPointBitmapHeight);
        canvas.drawBitmap(getPoint, src, des, paint);
    }

    private void drawCurrentLocation(Canvas canvas) {
        Rect src = new Rect(0, 0, currentLocation.getWidth(), currentLocation.getHeight());
        Rect des = new Rect(finalX - currentPW / 2, finalY - currentPH, finalX - currentPW / 2 +
                currentPW, finalY - currentPH + currentPH + 3);
        canvas.drawBitmap(currentLocation, src, des, paint);
    }


    public int getFinalX() {
        return finalX;
    }

    public void setFinalX(int finalX) {
        this.finalX = finalX;
    }

    public int getFinalY() {
        return finalY;
    }

    public void setLastStepX(int lastStepX) {
        this.lastStepX = lastStepX;
    }

    public int getLastStepX() {
        return lastStepX;
    }

    public void setLastStepY(int lastStepY) {
        this.lastStepY = lastStepY;
    }

    public int getLastStepY() {
        return lastStepY;
    }

    public void setFinalY(int finalY) {
        this.finalY = finalY;
    }

    public List<PointF> getPointFList() {
        return pointFList;
    }

    public Path getPath() {
        return path;
    }

    public float getScale() {
        return scale;
    }

    public int getStartPoint_X() {
        return startPoint_X;
    }

    public int getStartPoint_Y() {
        return startPoint_Y;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setPoint(int x, int y) {
        this.xP = x;
        this.yP = y;
    }

    public void setGetPoint_State(boolean getPoint_State) {
        this.getPoint_State = getPoint_State;
    }

    public int[] getPoint() {
        return new int[]{xP, yP};
    }

    public int getRouteLength() {
        return pointFList.size();
    }
}
