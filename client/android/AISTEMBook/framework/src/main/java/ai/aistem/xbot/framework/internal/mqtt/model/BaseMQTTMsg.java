package ai.aistem.xbot.framework.internal.mqtt.model;

/**
 * @author: aistem
 * @created: 2018/6/13/16:28
 * @desc: BaseMQTTMsg
 */
public class BaseMQTTMsg {

    private String event;
    private String rid;
    private String version;
    private String url;



    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
