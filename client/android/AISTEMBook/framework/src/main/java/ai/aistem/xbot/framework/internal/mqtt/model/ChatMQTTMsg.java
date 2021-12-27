package ai.aistem.xbot.framework.internal.mqtt.model;

import ai.aistem.xbot.framework.application.DCApplication;

/**
 * @author: aistem
 * @created: 2018/6/13/17:00
 * @desc: ChatMQTTMsg
 */
public class ChatMQTTMsg extends BaseMQTTMsg {

    private String uid;
    private float duration;

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public ChatMQTTMsg() {
        this.setEvent("chat_sound");
        this.setRid(DCApplication.mDataManager.getRobotID()+"");
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
