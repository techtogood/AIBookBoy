package ai.aistem.xbot.framework.helper;

import ai.aistem.xbot.framework.internal.utils.SoundPlayer;

/**
 * - @Description:  ${desc}
 * - @Author:  ${user}
 * - @Time:  ${date} ${time}
 */
public class NotificationHelper {


    private boolean isPlay=true;

    private static NotificationHelper instance;

    public static NotificationHelper getInstance(){
        if (instance==null){
            synchronized (NotificationHelper.class){
                instance=new NotificationHelper();
            }
        }
        return instance;
    }

    /**
     * 不在对讲界面时播放提示音
     * */
    public void playNotiSound(){
        if (isPlay){
            play();
        }
    }

    public void setBackgroundPlay(boolean isPlay){
        this.isPlay=isPlay;
    }

    /**
     * 在对讲界面播提示音
     * */
    public void playNotiSound(boolean isCurrent){
        if (isCurrent){
            play();
        }
    }



    private void play(){
        SoundPlayer.getInstance().play("20303001");
    }

}
