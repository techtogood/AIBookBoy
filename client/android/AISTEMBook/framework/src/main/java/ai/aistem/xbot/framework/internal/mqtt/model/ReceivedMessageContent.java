package ai.aistem.xbot.framework.internal.mqtt.model;

import java.util.ArrayList;

public class ReceivedMessageContent {

    private String event;

    private String uid;

    private int id;

    private ArrayList<Integer> ids;

    private String message;

    private int face;

    private String url;

    private int duration;

    private String nickname;

    private String relation;

    private String avatar;

    private String eid;

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public ArrayList<Integer> getIds() {
        return ids;
    }

    public void setIds(ArrayList<Integer> ids) {
        this.ids = ids;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFace() {
        return face;
    }

    public void setFace(int face) {
        this.face = face;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "ReceivedMessageContent{" +
                "event='" + event + '\'' +
                ", uid='" + uid + '\'' +
                ", id=" + id +
                ", message='" + message + '\'' +
                ", face=" + face +
                ", url='" + url + '\'' +
                ", duration=" + duration +
                ", nickname='" + nickname + '\'' +
                ", relation='" + relation + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
