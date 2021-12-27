package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

public class TddeAchievement extends LitePalSupport {

    private int achievement_template_id;
    private String name;
    private int status;
    private int type_id;
    private String conditions;
    private String schedule;
    private boolean isUpload;
    private long createTime;
    private long updateTime;


    public int getType_id() {
        return type_id;
    }

    public void setType_id(int type_id) {
        this.type_id = type_id;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }


    public int getAchievement_template_id() {
        return achievement_template_id;
    }

    public void setAchievement_template_id(int achievement_template_id) {
        this.achievement_template_id = achievement_template_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getConditions() {
        return conditions;
    }

    public void setConditions(String conditions) {
        this.conditions = conditions;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

}
