package ai.aistem.xbot.framework.internal.utils;


import java.io.File;

import ai.aistem.xbot.framework.application.DCApplication;
import ai.aistem.xbot.framework.application.GlobalParameter;

public class MenuPlayer {

    private AudioPlayer audioPlayer;

    private String RESOURCE_DIR = GlobalParameter.SOUND_RESOURCE_DIR;


    private static final MenuPlayer ourInstance = new MenuPlayer();

    public static MenuPlayer getInstance() {
        return ourInstance;
    }

    private MenuPlayer() {
        audioPlayer = new AudioPlayer();
    }

    public void play(int resID) {
        String res_path = RESOURCE_DIR + resID + ".mp3";
        boolean playFlag = DCApplication.mDataManager.getMenuVoicePromptSwitch();
        if (isResExisted(res_path) && playFlag) {
            System.out.println("菜单播报---" + res_path);
            audioPlayer.playLocal(res_path);
        }
    }

    private boolean isResExisted(String res_path) {
        if (!(new File(res_path)).exists()) {
            System.out.println("音频资源不存在---" + res_path);
            return false;
        } else {
            return true;
        }
    }
}
