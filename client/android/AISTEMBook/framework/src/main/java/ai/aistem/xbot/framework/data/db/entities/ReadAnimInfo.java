package ai.aistem.xbot.framework.data.db.entities;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: aistem
 * @created: 2019/3/25
 * @desc: 绘本一页一个Audio，Audio的信息
 */
public class ReadAnimInfo extends LitePalSupport {

    private int id;

    private int coverId;

    private int contentId;

    public int getId() {
        return id;
    }

    public int getCoverId() {
        return coverId;
    }

    public void setCoverId(int coverId) {
        this.coverId = coverId;
    }

    public int getContentId() {
        return contentId;
    }

    public void setContentId(int contentId) {
        this.contentId = contentId;
    }
}
