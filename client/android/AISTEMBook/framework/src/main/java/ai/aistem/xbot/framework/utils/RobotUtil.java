package ai.aistem.xbot.framework.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.hardware.gpio.UnlikeManager;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RobotUtil {

    public static final String ENTER = "ENTER";
    public static final String EXIT = "EXIT";

    public static boolean SCREEN_ON = true;

    /**
     * 保存亮度设置状态，退出app也能保持设置状态
     */
    public static void saveBrightness(Context context, int brightness) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
        if(brightness>0){
            SCREEN_ON = true;
        }else{
            SCREEN_ON = false;
        }
    }


    /**
     * 设置休眠时间
     * @param context
     * @param timeOut
     */
    public static void saveScreenOffTimeOut(Context context, int timeOut) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Settings.System.getUriFor(Settings.System.SCREEN_OFF_TIMEOUT);
        Settings.System.putInt(resolver, android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                timeOut);
        resolver.notifyChange(uri, null);
    }


    public static void enableScreen(Context context, boolean isShow) {

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            try {
                Class clazz = Class.forName("android.os.PowerManager");
                Method m = clazz.getMethod("setBacklightOn", boolean.class);
                m.invoke(powerManager, isShow);
                SCREEN_ON = isShow;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("RobotUtil", "PowerManager is null");
        }
    }

    public static void enableEarLight(boolean enable){
        UnlikeManager.getInstance().switch_ctrl(UnlikeManager.CONTROL_EARLIGHT, enable);
    }
}
