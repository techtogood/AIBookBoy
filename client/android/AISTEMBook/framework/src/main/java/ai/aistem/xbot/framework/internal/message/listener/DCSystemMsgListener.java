package ai.aistem.xbot.framework.internal.message.listener;

/**
 * @author: aistem
 * @created: 2018/6/11/13:56
 * @desc: 系统设置类的消息处理，如夜间模式与白天模式的切换，语音控制等
 */
public interface DCSystemMsgListener {

    /**
     * 心跳检测回调
     * @param aliveMsg
     */
    void aliveCheckCallBack(String aliveMsg);

    /**
     * 手机与设备绑定的回调
     * @param bindMsg*/
    void bindPhoneUserCallBack(String bindMsg);


    /**
     * 用户解绑的回调
     * @param unBindMsg*/
    void unBindPhoneUserCallBack(String unBindMsg);


    /**
     *
     * @param setNightMsg
     */
    void setNightModeCallBack(String setNightMsg);

    /**
     *
     * @param closeNightMsg
     */
    void closeNightModeCallBack(String closeNightMsg);

    /**
     * @param setAntiAddMsg
     */
    void setAntiAddictionCallBack(String setAntiAddMsg);

    /**
     * @param closeAntiAddMsg
     */
    void closeAntiAddictionCallBack(String closeAntiAddMsg);


    /**
     * @param userMsg
     */
    void updateUserCallBack(String userMsg);
}
