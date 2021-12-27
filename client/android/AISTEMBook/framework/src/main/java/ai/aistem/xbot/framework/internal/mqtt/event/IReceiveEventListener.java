package ai.aistem.xbot.framework.internal.mqtt.event;

import java.util.List;

@Deprecated
public interface IReceiveEventListener {

    /**
     * 心跳
     * @param uid
     */
    void OnAliveEventReceived(String uid);

    /**
     * 绑定
     * @param uid
     */
    void OnBindEventReceived(String uid);

    /**
     * 解绑定
     * @param uid
     */
    void OnUnBindEventReceived(String uid);

    /**
     * 设置夜间模式
     * @param uid
     */
    void OnSetNightEventReceived(String uid);

    /**
     * 关闭夜间模式
     * @param uid
     */
    void OnCloseNightEventReceived(String uid);

    /**
     * 设置防沉迷
     * @param uid
     */
    void OnSetAntiAddictionEventRecived(String uid);

    /**
     * 关闭防沉迷
     * @param uid
     */
    void OnCloseAntiAddicitonEventReceived(String uid);

    /**
     * 音乐推送
     * @param uid
     * @param ids
     */
    void OnPlaySoundEventReceived(String uid, int ids);

    /**
     * 音乐推送
     * @param uid
     * @param ids
     */
    void OnPlaySoundsEventReceived(String uid, List<Integer> ids);

    /**
     * 添加绘本
     * @param uid
     * @param id
     */
    void OnAddBook(String uid, int id);

    /**
     * 密语文字
     * @param uid
     * @param message
     */
    void OnChatTextEventReceive(String uid, String message);

    /**
     * 密语表情
     * @param uid
     * @param face
     */
    void OnChatFaceEventReceive(String uid, int face);

    /**
     * 对讲机语音
     * @param uid
     * @param url
     * @param duration
     */
    void OnChatSoundEventReceive(String uid, String url, int duration);
}
