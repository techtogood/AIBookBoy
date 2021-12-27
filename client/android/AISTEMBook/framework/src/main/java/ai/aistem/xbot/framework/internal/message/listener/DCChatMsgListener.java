package ai.aistem.xbot.framework.internal.message.listener;

/**
 * @author: aistem
 * @created: 2018/6/27/11:28
 * @desc: DCChatMsgListener
 */
public interface DCChatMsgListener {
    /**
     * 设备接收到手机语音消息的回调接口
     * @param voiceInfo
     * */
    void receiverUserVoice(String voiceInfo);

}
