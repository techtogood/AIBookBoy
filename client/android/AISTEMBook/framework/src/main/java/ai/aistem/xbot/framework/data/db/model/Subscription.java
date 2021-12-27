package ai.aistem.xbot.framework.data.db.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

/**
 * @author: aistem
 * @created: 2018/5/30/
 * @desc: Subscription
 * MQTT订阅的主题
 */
public class Subscription extends LitePalSupport {

    @Column(nullable = false)
    private String topic;
    @Column(nullable = false)
    private int qos;
    @Column(nullable = false)
    private boolean enableNotifications;

    @Column(ignore = true)
    private String lastMessage;
    @Column(ignore = true)
    private long persistenceId;


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }
}
