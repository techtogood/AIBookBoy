package ai.aistem.xbot.framework.internal.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import ai.aistem.xbot.framework.application.GlobalParameter;


public class MessageUtils {

    private static String TAG = GlobalParameter.class.getSimpleName();

    private static boolean isDebug = false;

    public static void setIsDebug(boolean isDebug) {
        MessageUtils.isDebug = isDebug;
    }

    public static void MessageFeed(Handler handler, int msgid) {
        if (handler != null) {
            Message msg = handler.obtainMessage(msgid);
            handler.sendMessage(msg);
        } else {
            Log.e(TAG, "handler is null!");
        }
    }

    public static void MessageFeed(Handler handler, int msgid, Object obj) {
        if (handler != null) {
            Message msg = handler.obtainMessage(msgid, obj);
            handler.sendMessage(msg);
        } else {
            Log.e(TAG, "handler is null!");
        }
    }

    public static void MessageFeedDebug(Handler handler, int msgid, Object obj){
        if(!isDebug) return;
        if (handler != null) {
            Message msg = handler.obtainMessage(msgid, obj);
            handler.sendMessage(msg);
        } else {
            Log.e(TAG, "handler is null!");
        }
    }

    public static void MessageFeedDebug(Handler handler, int msgid){
        if(!isDebug) return;
        if (handler != null) {
            Message msg = handler.obtainMessage(msgid);
            handler.sendMessage(msg);
        } else {
            Log.e(TAG, "handler is null!");
        }
    }
}
