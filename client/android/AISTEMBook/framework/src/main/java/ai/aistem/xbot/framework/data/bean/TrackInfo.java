package ai.aistem.xbot.framework.data.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author: LiQi
 * @created: 2018/7/27/11:52
 * @desc: TrackInfo
 */
public class TrackInfo implements Serializable {

    private String author;
    private List<Integer> categorys;
    private int count;
    private String cover;
    private String create_time;
    private String filter_age;
    private String id;
    private List<TrackBean> list;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Integer> getCategorys() {
        return categorys;
    }

    public void setCategorys(List<Integer> categorys) {
        this.categorys = categorys;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getFilter_age() {
        return filter_age;
    }

    public void setFilter_age(String filter_age) {
        this.filter_age = filter_age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TrackBean> getList() {
        return list;
    }

    public void setList(List<TrackBean> list) {
        this.list = list;
    }
}
