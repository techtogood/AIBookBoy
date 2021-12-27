package ai.aistem.xbot.framework.data.db.model;

import org.litepal.crud.LitePalSupport;

/**
 * @author: AISTEM
 * @created: 2018/6/19/
 * @desc: 绘本数据
 */
public class PictureBookData extends LitePalSupport {
    private int book_id;  //绘本id

    private int type; // 绘本算法类型 1：SURF, 2:ORB, 3:SIFT

    private String name; // 绘本算法类型 1：SURF, 2:ORB, 3:SIFT

    public int getBookId() {
        return book_id;
    }

    public void setBookId(int book_id) {
        this.book_id = book_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
