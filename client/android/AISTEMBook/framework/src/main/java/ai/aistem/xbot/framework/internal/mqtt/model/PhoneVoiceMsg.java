package ai.aistem.xbot.framework.internal.mqtt.model;

/**
 * @author: aistem
 * @created: 2018/7/10/20:36
 * @desc: PhoneVoiceMsg
 */
public class PhoneVoiceMsg {

    private int duration;
    private String fileName;
    private String filePath;
    private boolean isRead;
    private String uid;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
