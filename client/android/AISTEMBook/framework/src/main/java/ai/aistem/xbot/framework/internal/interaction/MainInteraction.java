package ai.aistem.xbot.framework.internal.interaction;

/**
 * Created by aistem on 2018/3/14.
 * modified by aistem on 2018/5/15.
 */

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.application.GlobalParameter;

public class MainInteraction implements Runnable {

    private static final String TAG = MainInteraction.class.getSimpleName();

    private RobotStateMachine robot_sm = null;

    @Override
    public void run() {
        robot_sm = new RobotStateMachine("robot");
        GlobalParameter.StateMachineHandler = robot_sm;
        //robot_sm.setDbg(true);
        robot_sm.start();


        GlobalParameter.StateEngineHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case GlobalParameter.StateEngine_Init_Finish_Or_Timeout:
                        Log.d(TAG, "-->get StateEngine_Init_Finish message");
                        //初始化或者初始化超时,跳转至闲聊状态
                        //robot_sm.MessageFeed(robot_sm.CMD_WORK);
                        break;
                    case GlobalParameter.StateEngine_T_SS_Timeout:
                        Log.d(TAG, "-->get StateEngine_T_SS_Timeout message");
//                        robot_sm.MessageFeed(robot_sm.CMD_SLEEP);
                        break;
                    case GlobalParameter.StateEngine_T_CS_Timeout:
                        Log.d(TAG, "-->get StateEngine_T_CS_Timeout message");
                        //robot_sm.MessageFeed(robot_sm.CMD_SELF);
                        break;
                    case GlobalParameter.StateEngine_T_WS_Timeout:
                        Log.d(TAG, "-->get StateEngine_T_WS_Timeout message");
                        //robot_sm.MessageFeed(robot_sm.CMD_SELF);
                        break;
                    case GlobalParameter.StateEngine_T_AAEnable_Timeout:
                        Log.d(TAG, "-->get StateEngine_T_AAE_Timeout message");
                        //收到防沉迷定时器,跳转至防沉迷状态
                        //robot_sm.MessageFeed(robot_sm.CMD_ANTI);
                        break;
                    case GlobalParameter.StateEngine_T_AAInactive_Timeout:
                        Log.d(TAG, "-->get StateEngine_T_AA_Timeout message");
                        //收到防沉迷失活定时器,关闭防沉迷功能,跳转至闲聊状态
                        //robot_sm.MessageFeed(robot_sm.CMD_SELF);
                        break;
                    //下面处理UI更新机器人状态
                    case GlobalParameter.StateEngine_CMD_BASE:
                        //robot_sm.MessageFeed(robot_sm.CMD_BASE);
                        break;
                    case GlobalParameter.StateEngine_CMD_CHAT:
                        //robot_sm.MessageFeed(robot_sm.CMD_CHAT);
                        break;
                    case GlobalParameter.StateEngine_CMD_SELF:
                        //robot_sm.MessageFeed(robot_sm.CMD_SELF);
                        break;
                    case GlobalParameter.StateEngine_CMD_SLEEP:
                        //robot_sm.MessageFeed(robot_sm.CMD_SLEEP);
                        break;
                    case GlobalParameter.StateEngine_CMD_ANTI:
                        //robot_sm.MessageFeed(robot_sm.CMD_ANTI);
                        break;
                    case GlobalParameter.StateEngine_CMD_HOME:
                        robot_sm.MessageFeed(robot_sm.CMD_HOME);
                        break;
                    case GlobalParameter.StateEngine_CMD_NIGHT:
                        //如果夜间模式设置为ON,则跳转到夜间状态
                        if (DCApplication.mDataManager.getRobotNightModeSwitch()) {
                            //robot_sm.MessageFeed(robot_sm.CMD_NIGHT);
                        } else {
                            //如果夜间模式设置为OFF,则进不去夜间状态
                        }
                        break;
                    default:
                        Log.d(TAG, "-->get wrong message =" + msg.what);
                        break;
                }
            }
        };
    }

    public String getRobotCurrentState() {
        return robot_sm.getCurrentState().getName();
    }
}
