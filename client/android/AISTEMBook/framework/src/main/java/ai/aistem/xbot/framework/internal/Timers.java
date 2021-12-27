package ai.aistem.xbot.framework.internal;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;
import ai.aistem.xbot.framework.message.DCBroadcastMsgImpl;

import static android.content.Context.ALARM_SERVICE;


public class Timers implements Runnable {

    private static final String TAG = Timers.class.getSimpleName();

    /**
     * 基础状态失活定时器
     */
    public final static int T_BSInactiv_Seconds = 3;//秒
    private Timer T_BSInactiv = null;

    /**
     * 闲聊状态(自言自语)失活定时器
     */
    public final static int T_SSInactiv_Seconds = 50;//秒
    //    public final static int T_SSInactiv_Seconds = 60;//秒 Debug
    private Timer T_SSInactiv = null;

    /**
     * 工作状态失活定时器
     */
    public final static int T_WSInactiv_Seconds = 30;//秒
    private Timer T_WSInactiv = null;

    /**
     * 语音交互状态失活定时器
     */
    public final static int T_CSInactiv_Seconds = 60;//秒
    private Timer T_CSInactiv = null;

    /**
     * 防沉迷定时器(允许机器人单次使用时长)
     */
    public int T_AAEnable_Seconds = 1800;
    private Timer T_AAEnable = null;

    /**
     * 防沉迷失活定时器(机器人休息时长)
     */
    public int T_AAInactiv_Seconds = 600;
    private Timer T_AAInactiv = null;

    /**
     * 进入防沉迷前1分钟有语音提示的定时器
     */
    public static int T_AAEnter_Seconds = 60;
    private Timer T_AAEnter = null;

    /**
     * 防沉迷结束30秒前语音提示的定时器
     */
    public static int T_AAExit_Seconds = 30;
    private Timer T_AAExit = null;

    private Context mContext;
    private AlarmManager alarmManager;
    private PendingIntent pendingOnIntent = null;
    private PendingIntent pendingOffIntent = null;

    public Timers(Context context) {
        mContext = context;
        alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
    }

    @Override
    public void run() {
        GlobalParameter.TimerHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GlobalParameter.T_BSInactiv_Start:
                        Log.i(TAG, "启动-基础状态失活定时器...");
                        if (T_BSInactiv != null) {
                            T_BSInactiv.purge();
                            T_BSInactiv.cancel();
                            T_BSInactiv = null;
                        }
                        T_BSInactiv = new Timer("BaseStateInactivity", true);
                        T_BSInactiv.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                Log.i(TAG, "基础状态失活定时器超时!");
                                MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                        GlobalParameter.StateEngine_Init_Finish_Or_Timeout);
                            }
                        }, T_BSInactiv_Seconds * 1000);
                        break;
                    case GlobalParameter.T_BSInactiv_Cancel:
                        Log.i(TAG, "取消-基础状态失活定时器...");
                        if (T_BSInactiv != null) {
                            T_BSInactiv.purge();
                            T_BSInactiv.cancel();
                            T_BSInactiv = null;
                        }
                        break;
                    case GlobalParameter.T_WSInactiv_Cancel:
                        Log.i(TAG, "取消-工作状态失活定时器...");
                        if (T_WSInactiv != null) {
                            T_WSInactiv.purge();
                            T_WSInactiv.cancel();
                            T_WSInactiv = null;
                        }
                        break;
                    case GlobalParameter.T_WSInactiv_Start:
                        Log.i(TAG, "启动-工作状态失活定时器...");
                        if (T_WSInactiv != null) {
                            T_WSInactiv.purge();
                            T_WSInactiv.cancel();
                            T_WSInactiv = null;
                        }
                        T_WSInactiv = new Timer("WorkStateInactivity", true);
                        T_WSInactiv.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "工作状态失活定时器超时!");
                                DCBroadcastMsgImpl.sendNOPTimeoutBroadcast();
                                /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                        GlobalParameter.StateEngine_T_WS_Timeout);*/
                            }
                        }, T_WSInactiv_Seconds * 1000);
                        break;
                    case GlobalParameter.T_SSInactiv_Start:
                        Log.i(TAG, "启动-闲聊状态失活定时器...");
                        if (T_SSInactiv != null) {
                            T_SSInactiv.purge();
                            T_SSInactiv.cancel();
                            T_SSInactiv = null;
                        }
                        T_SSInactiv = new Timer("SelfStateInactivity", true);
                        T_SSInactiv.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "闲聊状态失活定时器超时!");


                            }
                        }, T_SSInactiv_Seconds * 1000);
                        break;
                    case GlobalParameter.T_SSInactiv_Cancel:
                        Log.i(TAG, "取消-闲聊状态失活定时器...");
                        if (T_SSInactiv != null) {
                            T_SSInactiv.purge();
                            T_SSInactiv.cancel();
                            T_SSInactiv = null;
                        }
                        break;
                    case GlobalParameter.T_CSInactiv_Start:
                        Log.i(TAG, "启动-语音交互状态失活定时器...");
                        if (T_CSInactiv != null) {
                            T_CSInactiv.purge();
                            T_CSInactiv.cancel();
                            T_CSInactiv = null;
                        }
                        T_CSInactiv = new Timer("ChattingStateInactivity", true);
                        T_CSInactiv.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                //发送状态机Handler语音交互状态失活定时器超时消息
                                Log.i(TAG, "语音交互状态失活定时器超时!");
                                /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                        GlobalParameter.StateEngine_T_CS_Timeout);*/
                                //DCBroadcastMsgImpl.sendSelfTalkStateBroadcast("ENTER");
                            }
                        }, T_CSInactiv_Seconds * 1000);
                        break;
                    case GlobalParameter.T_CSInactiv_Cancel:
                        Log.i(TAG, "取消-语音交互状态失活定时器...");
                        if (T_CSInactiv != null) {
                            T_CSInactiv.purge();
                            T_CSInactiv.cancel();
                            T_CSInactiv = null;
                        }
                        break;
                    case GlobalParameter.T_AAInactiv_Start:
                        T_AAInactiv_Seconds =
                                DCApplication.mDataManager.getRobotAntiAddictionRestDuration() * 60;
                        //防沉迷结束30秒前语音提示的定时器
                        T_AAExit_Seconds = T_AAInactiv_Seconds - 30;

                        Log.i(TAG, "启动-防沉迷失活定时器...");
                        if (T_AAInactiv != null) {
                            T_AAInactiv.purge();
                            T_AAInactiv.cancel();
                            T_AAInactiv = null;
                        }
                        Log.i(TAG, "启动-防沉迷失活前30秒定时器...");
                        if (T_AAExit != null) {
                            T_AAExit.purge();
                            T_AAExit.cancel();
                            T_AAExit = null;
                        }
                        T_AAInactiv = new Timer("AntiAddictionInactivity", true);
                        T_AAInactiv.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "防沉迷失活定时器超时,退出防沉迷状态(已包含广播通知UI处理)");
                                /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                        GlobalParameter.StateEngine_T_AAInactive_Timeout);*/
                            }
                        }, T_AAInactiv_Seconds * 1000);
                        T_AAExit = new Timer("AntiAddictionExit", true);

                        T_AAExit.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "防沉迷失活前30秒定时器超时,广播通知UI处理");
                                DCBroadcastMsgImpl.sendPreAntiAddiStateBroadcast("EXIT");
                            }
                        }, T_AAExit_Seconds * 1000);
                        break;
                    case GlobalParameter.T_AAInactiv_Cancel:
                        Log.i(TAG, "取消-防沉迷失活定时器...");
                        if (T_AAInactiv != null) {
                            T_AAInactiv.purge();
                            T_AAInactiv.cancel();
                            T_AAInactiv = null;
                        }
                        Log.i(TAG, "取消-防沉迷失活前30秒定时器...");
                        if (T_AAExit != null) {
                            T_AAExit.purge();
                            T_AAExit.cancel();
                            T_AAExit = null;
                        }
                        break;
                    case GlobalParameter.T_AAEnable_Start:
                        T_AAEnable_Seconds =
                                DCApplication.mDataManager.getRobotAntiAddictionDuration() * 60;
                        //防沉迷结束1分钟前语音提示的定时器
                        T_AAEnter_Seconds = T_AAEnable_Seconds - 60;

                        Log.i(TAG, "启动-进入防沉迷定时器...");
                        if (T_AAEnable != null) {
                            T_AAEnable.purge();
                            T_AAEnable.cancel();
                            T_AAEnable = null;
                        }
                        Log.i(TAG, "启动-进入防沉迷前1分钟定时器...");
                        if (T_AAEnter != null) {
                            T_AAEnter.purge();
                            T_AAEnter.cancel();
                            T_AAEnter = null;
                        }
                        T_AAEnable = new Timer("AntiAddictionEnable", true);
                        T_AAEnable.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "进入防沉迷定时器超时,跳转至防沉迷状态(已包含广播通知UI处理)");
                               /* MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                        GlobalParameter.StateEngine_T_AAEnable_Timeout);*/
                            }
                        }, T_AAEnable_Seconds * 1000);

                        T_AAEnter = new Timer("AntiAddictionEnter", true);
                        T_AAEnter.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                Log.i(TAG, "进入防沉迷前1分钟定时器超时,广播通知UI处理");
                                DCBroadcastMsgImpl.sendPreAntiAddiStateBroadcast("ENTER");
                            }
                        }, T_AAEnter_Seconds * 1000);
                        break;
                    case GlobalParameter.T_AAEnable_Cancel:
                        Log.i(TAG, "取消-进入防沉迷定时器...");
                        if (T_AAEnable != null) {
                            T_AAEnable.purge();
                            T_AAEnable.cancel();
                            T_AAEnable = null;
                        }
                        Log.i(TAG, "启动-进入防沉迷前1分钟定时器...");
                        if (T_AAEnter != null) {
                            T_AAEnter.purge();
                            T_AAEnter.cancel();
                            T_AAEnter = null;
                        }
                        break;
                    case GlobalParameter.A_NightMode_Set:
                        Log.i(TAG, "启动-设置夜间模式闹铃...");
                        if (!(msg.obj instanceof String[])) break;
                        String[] time = (String[]) msg.obj;
                        int start_hour = getHour(time[0]);
                        int start_minute = getMin(time[0]);
                        int end_hour = getHour(time[1]);
                        int end_minute = getMin(time[1]);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8")); //  这里时区需要设置一下，不然会有8个小时的时间差
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        long current_stamp = calendar.getTimeInMillis();

                        calendar.set(Calendar.MINUTE, start_minute);
                        calendar.set(Calendar.HOUR_OF_DAY, start_hour);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        //如果start的时间小于current时间,那么start+1天
                        long start_stamp = calendar.getTimeInMillis();

                        if (start_stamp < current_stamp)
                            calendar.add(Calendar.DAY_OF_MONTH, 1);


                        pendingOnIntent = PendingIntent.getBroadcast(mContext, 0,
                                DCBroadcastMsgImpl.getNightAlarmIntent("START"), 0);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, pendingOnIntent);

                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.MINUTE, end_minute);
                        calendar.set(Calendar.HOUR_OF_DAY, end_hour);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        //判断如果开始小时大于结束小时,说明跨天
                        if (end_hour < start_hour)
                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                        long end_stamp = calendar.getTimeInMillis();
                        //如果end的时间小于current时间,那么end+1天
                        if (end_stamp < current_stamp)
                            calendar.add(Calendar.DAY_OF_MONTH, 1);

                        pendingOffIntent = PendingIntent.getBroadcast(mContext, 1,
                                DCBroadcastMsgImpl.getNightAlarmIntent("END"), 0);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                                calendar.getTimeInMillis(),
                                AlarmManager.INTERVAL_DAY, pendingOffIntent);

                        //发送夜间模式开始消息
                        if (current_stamp < end_stamp && current_stamp > start_stamp) {
                            Log.v(TAG, "当前时间在夜间模式期间,应该立刻设置为夜间状态");
                           /* MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                                    GlobalParameter.StateEngine_CMD_NIGHT);*/
                        } else {
                            Log.v(TAG, "当前时间不在夜间模式期间,如果在夜间状态时跳转至工作状态");
//                            if (DCApplication.mMainInteraction.getRobotCurrentState().equals("NightState")) {
//                                /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
//                                        GlobalParameter.StateEngine_CMD_HOME);*/
//                            }
                        }
                        //register();
                        break;
                    case GlobalParameter.A_NightMode_Close:
                        alarmManager.cancel(pendingOnIntent);
                        alarmManager.cancel(pendingOffIntent);
                        //夜间模式结束消息,只有在夜间状态时进入工作状态
//                        if (DCApplication.mMainInteraction.getRobotCurrentState().equals("NightState")) {
//                            /*MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
//                                    GlobalParameter.StateEngine_CMD_HOME);*/
//                        }
                        //unRegister();
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private static int getHour(String strTime) {
        String[] time = strTime.split(":");
        int hour;
        try {
            hour = Integer.valueOf(time[0]);
            if (hour < 0 || hour > 24) throw new Exception();
        } catch (Exception e) {
            hour = 0;
        }
        return hour;
    }

    private static int getMin(String strTime) {
        String[] time = strTime.split(":");
        int min;
        try {
            min = Integer.valueOf(time[1]);
            if (min < 0 || min > 60) throw new Exception();
        } catch (Exception e) {
            min = 0;
        }
        return min;
    }
}
