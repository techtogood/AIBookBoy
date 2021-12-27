package ai.aistem.xbot.framework.internal.utils;

/**
 * Created by aistem on 2018/8/17.
 */

public class AudioPlayer {
    static{
        System.loadLibrary("avcodec-56");
        System.loadLibrary("avdevice-56");
        System.loadLibrary("avfilter-5");
        System.loadLibrary("avformat-56");
        System.loadLibrary("avutil-54");
        System.loadLibrary("postproc-53");
        System.loadLibrary("swresample-1");
        System.loadLibrary("swscale-3");
        System.loadLibrary("native-lib");
    }
    public native void playLocal(String file);
    public native int playLocalDuration(String file);
    //public native void playUri(String uri);
    public native void resume();
    public native void pause();
    public native int getPlayerState();//返回0不播放或静音状态，非0则在播放状态
    public native void exit();
}