package com.tongji.zhixin.graduation.stepcount;

import com.tongji.zhixin.graduation.R;
import com.tongji.zhixin.graduation.ValueCenter;
import com.tongji.zhixin.graduation.activity.Graduation_Project;

/**
 * Created by zhixin on 2018/3/13.
 */

public class StepCount {
    private int mCount = 0;  //步数大于9时
    private long timeOfLastPeak = 0;
    private long timeOfThisPeak = 0;
    private ValueCenter valueCenter;
    private long firstTime;
    private long timeDifference;
    private boolean isFirst = true;
    private boolean isDebug = false;

    public void countStep() {
        if (firstTime == 0) {
            firstTime = System.currentTimeMillis();
            timeOfLastPeak = firstTime;
        }
        //最开始2秒由于存在滤波的错误波峰，需要忽略，正因为如此，前2秒的步伐检测不到，因此，用户需要等待2秒，需要给出Toast提示。
        this.timeOfThisPeak = System.currentTimeMillis();
        if (isFirst) {
            timeDifference = 500;   //使能第一步，否则第一步检测不到
            isFirst = false;
        } else {
            timeDifference = timeOfThisPeak - timeOfLastPeak;
        }
        if (timeDifference < 3000L && timeDifference > 350L && !isDebug) { //大概一秒走3步，第一步检测不到
            this.mCount++;
            valueCenter.stepChange(mCount);
            valueCenter.pathChange();
            if (Graduation_Project.getMainActivity().getMode().equals(Graduation_Project
                    .getMainActivity().getResources().getString(R.string.pocket))){
                this.mCount++;
                valueCenter.stepChange(mCount);
                valueCenter.pathChange();
            }
        }else if(isDebug){
            this.mCount++;
            valueCenter.stepChange(mCount);
            valueCenter.pathChange();
        }
        this.timeOfLastPeak = this.timeOfThisPeak;
    }

    public void initValueCenter(ValueCenter valueCenter) {
        this.valueCenter = valueCenter;
    }

    public void reset_mCount() {
        mCount = 0;
        valueCenter.stepChange(mCount);
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }
}
