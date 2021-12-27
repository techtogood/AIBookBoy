package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

/**
 * @author: LiQi
 * @created: 2018/6/26/17:55
 * @desc: 替换TddeVoiceInfo
 * @modify: aistem
 */

public class TddeVoiceInfo2 extends LitePalSupport implements Serializable {

    private int id;
    private int userId; //用户id，与服务器一致
    private boolean isRead; //是否已读
    private boolean isUpload;//发送语音是否上传
    private boolean isDownload;//接收语音是否下载
    private float duration;//语音长度
    private String path;//语音文件本地保存路径，包括文件名
    private String fileName;//语音文件名
    private String url;//语音文件OSS服务器路径
    private long createTime;//创造时间
    private int type;//类型，0：发送，1：接收

    public int getUserId() {
        return userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
