package ai.aistem.xbot.framework.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.sensor.ProximityManager;
import android.support.annotation.Nullable;

import ai.aistem.xbot.framework.application.GlobalParameter;
import ai.aistem.xbot.framework.internal.utils.SoundPlayer;
import ai.aistem.xbot.framework.utils.SPUtils;

public class RobotEyecareService extends Service {
    private boolean push = false;
    private Object sharObj = new Object();
    private int Count = 0;
//    private getCurrentDistenceThread thread = null;
    @Override
    public void onCreate() {
        super.onCreate();
        new getCurrentDistenceThread().start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (GlobalParameter.PROTECT_EYE_ENABLE && SPUtils.getInstance().getProtectEyeStatus())
        {
            push = false;
            synchronized (sharObj)
            {
                sharObj.notifyAll();
            }
        }
        else
        {
            push = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        push = true;
        Count = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class getCurrentDistenceThread extends Thread{
        @Override
        public void run() {
            while (true)
            {
                try
                {
                    while (push)
                    {
                        synchronized (sharObj)
                        {
                            sharObj.wait();
                        }
                    }

                    int distence = ProximityManager.getInstance().getCurrentDistance();
//                    Log.d("EyecareService","当前距离:　" + distence);
                    if(distence > 180)
                    {
                        Count ++;
                    }
                    else
                    {
                        Count = 0;
                    }

                    if(8 == Count || 14 == Count)
                    {
                        SoundPlayer.getInstance().play("20403001");

                    }
                    else if(20 == Count)
                    {
                        SoundPlayer.getInstance().play("20402004");
                    }
                    else if(24 == Count)
                    {
                        Count = 0;
                    }
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
