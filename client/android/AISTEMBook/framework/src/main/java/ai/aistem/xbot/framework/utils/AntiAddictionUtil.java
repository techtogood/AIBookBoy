package ai.aistem.xbot.framework.utils;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.internal.utils.SoundPlayer;

public class AntiAddictionUtil {

    private static final String TAG = AntiAddictionUtil.class.getSimpleName();

    private static AntiAddictionUtil instance = null;

    public synchronized static AntiAddictionUtil getInstance() {
        if (instance == null) {
            instance = new AntiAddictionUtil();
        }
        return instance;
    }

    /**
     * 防沉迷定时器(工作时长)
     */
    private Timer playGameTimer = null;
    private boolean isWarning = false;
    private boolean isPlaying = false;
    private long startPlayTime;

    public AntiAddictionUtil() {

    }

    public boolean start() {
        boolean IsOpen = DCApplication.mDataManager.getRobotAntiAddictionSwitch();
        int restDuration = DCApplication.mDataManager.getRobotAntiAddictionRestDuration();
        int duration = DCApplication.mDataManager.getRobotAntiAddictionDuration();
//        int duration = 1;//测试 1分钟
//        int restDuration = 1;//测试 1分钟
        Log.i(TAG, "防沉迷开关：" + IsOpen + " 共能使用多久：" + duration
                + "分钟 共需休息多久：" + restDuration + "分钟");
        //判断防沉迷开关是否开启，若开启，则判断是否满足休息条件：
        //      (是否休息够了)
        //          若满足，则返回true并开启防沉迷定时器;
        //          若不满足则返回false;
        //判断防沉迷开关是否开启，若关闭，则返回true；
        if (IsOpen) {
            Long currentTimeStamp = System.currentTimeMillis();
            Long restTimeStamp = SPUtils.getInstance().getAntiAddiRestTimeStamp();
            if ((currentTimeStamp - restTimeStamp) > (restDuration * 60 * 1000)) {
                //满足休息条件
                long workTime = SPUtils.getInstance().getAntiAddiWorkTime();
                long remain = duration * 60 * 1000 - workTime;
                startPlayTime = System.currentTimeMillis();
                startWorkTimer(remain);
                isPlaying = true;
                return true;
            } else {
                //不满足休息条件
                isPlaying = false;
                Log.i(TAG, "已经休息了：" + (currentTimeStamp - restTimeStamp) / 1000 +
                        "秒 还需要休息：" +
                        (restDuration * 60 - (currentTimeStamp - restTimeStamp) / 1000) + "秒");
                return false;
            }
        } else {
            isPlaying = true;
            return true;
        }
    }

    //游戏中处理手机的防沉迷设置 //如果在游戏中收到手机的防沉迷设置，退出是初始化已使用时间和开始休息时间戳
    public void restart(){
        if(isPlaying){
            cancelTimer();
            isWarning = false;
            SPUtils.getInstance().removeAntiAddiWorkTime();
            SPUtils.getInstance().removeAntiAddiRestTimeStamp();

            boolean IsOpen = DCApplication.mDataManager.getRobotAntiAddictionSwitch();
            int restDuration = DCApplication.mDataManager.getRobotAntiAddictionRestDuration();
            int duration = DCApplication.mDataManager.getRobotAntiAddictionDuration();
//        int duration = 1;//测试 1分钟
//        int restDuration = 1;//测试 1分钟
            Log.i(TAG, "防沉迷开关：" + IsOpen + " 共能使用多久：" + duration
                    + "分钟 共需休息多久：" + restDuration + "分钟");
            //判断防沉迷开关是否开启，若开启，则判断是否满足休息条件：
            //      (是否休息够了)
            //          若满足，则返回true并开启防沉迷定时器;
            //          若不满足则返回false;
            //判断防沉迷开关是否开启，若关闭，则返回true；
            if (IsOpen) {
                Long currentTimeStamp = System.currentTimeMillis();
                Long restTimeStamp = SPUtils.getInstance().getAntiAddiRestTimeStamp();
                if ((currentTimeStamp - restTimeStamp) > (restDuration * 60 * 1000)) {
                    //满足休息条件
                    long workTime = SPUtils.getInstance().getAntiAddiWorkTime();
                    long remain = duration * 60 * 1000 - workTime;
                    startPlayTime = System.currentTimeMillis();
                    startWorkTimer(remain);
                    isPlaying = true;
                } else {
                    //不满足休息条件
                    isPlaying = false;
                    Log.i(TAG, "已经休息了：" + (currentTimeStamp - restTimeStamp) / 1000 +
                            "秒 还需要休息：" +
                            (restDuration * 60 - (currentTimeStamp - restTimeStamp) / 1000) + "秒");
                }
            } else {
                isPlaying = true;
            }
        }else{
            isWarning = false;
            SPUtils.getInstance().removeAntiAddiWorkTime();
            SPUtils.getInstance().removeAntiAddiRestTimeStamp();
        }
    }

    public void stop() {
        isPlaying = false;
        cancelTimer();
        boolean IsOpen = DCApplication.mDataManager.getRobotAntiAddictionSwitch();
        if (IsOpen) {
            //退出时，如果警告，则1.取消警告 2.设置开始的休息时间 3.重置已使用时间
            if (isWarning) {
                //1.取消警告
                isWarning = false;
                //2.设置开始的休息时间
                SPUtils.getInstance().saveAntiAddiRestTimeStamp();
                //3.重置已使用时间
                SPUtils.getInstance().saveAntiAddiWorkTime(0L);
            } else {
                //如果不警告，说明还没有超时，记录已经使用了多久
                long now = System.currentTimeMillis();
                long duration = now - startPlayTime + SPUtils.getInstance().getAntiAddiWorkTime();
                SPUtils.getInstance().saveAntiAddiWorkTime(duration);
                Log.i(TAG, "使用了：" + (duration / 1000) + "秒");
            }
        } else {
            isWarning = false;
            SPUtils.getInstance().removeAntiAddiWorkTime();
            SPUtils.getInstance().removeAntiAddiRestTimeStamp();
        }
    }

    private void startWorkTimer(long remain) {
        cancelTimer();
        Log.i(TAG, "启动-防沉迷工作定时器...倒计时：" + (remain / 1000) + "秒");
        playGameTimer = new Timer("AntiAddictionWork", true);
        playGameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.i(TAG, "防沉迷工作定时器超时");
                isWarning = true;
                SoundPlayer.getInstance().play("20402001");
                try {
                    Thread.sleep(60 * 1000);
                    while (isWarning) {
                        SoundPlayer.getInstance().play("20402002");
                        Thread.sleep(10000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, remain);
    }

    private void cancelTimer() {
        Log.i(TAG, "取消-防沉迷工作定时器...");
        if (playGameTimer != null) {
            playGameTimer.purge();
            playGameTimer.cancel();
            playGameTimer = null;
        }
    }


}
