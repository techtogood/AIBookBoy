package ai.aistem.xbot.framework.internal.utils;


import java.io.File;

import ai.aistem.xbot.framework.application.GlobalParameter;


public class SoundPlayer {

    private AudioPlayer audioPlayer;

    private String RESOURCE_DIR = GlobalParameter.SOUND_RESOURCE_DIR;

    private static final SoundPlayer ourInstance = new SoundPlayer();

    public static SoundPlayer getInstance() {
        return ourInstance;
    }

    private SoundPlayer() {
        audioPlayer = new AudioPlayer();
    }

    public void play(String resID) {
        String res_path = RESOURCE_DIR + resID + ".mp3";
        if (isResExisted(res_path)) {
            // int delay = audioPlayer.playLocalDuration(res_path);
            audioPlayer.playLocal(res_path);
        }
    }

    public int playDuration(String resID) {
        String res_path = RESOURCE_DIR + resID + ".mp3";
        if (isResExisted(res_path)) {
            int delay = audioPlayer.playLocalDuration(res_path) / 1000;
            return delay;
        }
        return 0;
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
