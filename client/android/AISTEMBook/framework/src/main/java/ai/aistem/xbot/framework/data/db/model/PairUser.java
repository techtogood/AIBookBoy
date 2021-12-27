package ai.aistem.xbot.framework.data.db.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class PairUser extends LitePalSupport {
    @Column(unique = true)
    private int uid;

    private String avatar;

    private String nickname;

    private String relation;

    private String photo;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}
