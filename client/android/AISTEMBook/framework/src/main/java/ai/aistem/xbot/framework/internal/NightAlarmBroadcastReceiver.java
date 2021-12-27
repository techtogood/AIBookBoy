package ai.aistem.xbot.framework.internal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.utils.MessageUtils;
import ai.aistem.xbot.framework.message.DCMessage;
import ai.aistem.xbot.framework.utils.LogUtil;


public class NightAlarmBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(DCMessage.ACTION_NIGHT_ALARM_COMMAND)) {
            String flag = intent.getStringExtra(DCMessage.EXTRA_NIGHT_ALARM_COMMAND);
            if ("START".equals(flag)) {
                LogUtil.getInstance().d("收到夜间模式闹铃---" + flag);
                //发送夜间模式开始消息
                MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                        GlobalParameter.StateEngine_CMD_NIGHT);
            } else if ("END".equals(flag)) {
                LogUtil.getInstance().d("收到夜间模式闹铃---" + flag);
                //夜间模式结束消息,进入工作状态
               /* MessageUtils.MessageFeed(GlobalParameter.StateEngineHandler,
                        GlobalParameter.StateEngine_CMD_SELF);*/
            }
        }
    }
}
