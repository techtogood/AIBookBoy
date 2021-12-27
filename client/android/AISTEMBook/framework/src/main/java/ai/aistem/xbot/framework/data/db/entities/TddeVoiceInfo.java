package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author: LiQi
 * @created: 2018/6/26/17:55
 * @desc: 对讲功能中语音管理类（没有把机器人与手机用户分开是因为音频排序比较麻烦）
 * @modify: aistem
 */

public class TddeVoiceInfo extends LitePalSupport implements Serializable {


    private String userId;
    private String username;
    private String relationshipId;//机器人与对应的绑定用户产生的一个ID
    private boolean isRead;
    private boolean isUpload;
    private boolean isDownload;
    private float duration;
    private String path;
    private String fileName;
    private long createTime;
    private int type;
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(String relationshipId) {
        this.relationshipId = relationshipId;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
