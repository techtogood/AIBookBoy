package ai.aistem.xbot.framework.internal.http.model;


import java.util.List;

import ai.aistem.xbot.framework.data.bean.TrackBean;

public class SoundAlbumResponse {
    private int status;
    private String message;
    private Result result;

    public class Result{
        int id;
        String title;
        String cover;
        String author;
        String filter_age;
        int count;
        String update_time;
        String create_time;
        String delete_time;
        List<TrackBean> list;
        List<Integer> categorys;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getFilter_age() {
            return filter_age;
        }

        public void setFilter_age(String filter_age) {
            this.filter_age = filter_age;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getUpdate_time() {
            return update_time;
        }

        public void setUpdate_time(String update_time) {
            this.update_time = update_time;
        }

        public String getCreate_time() {
            return create_time;
        }

        public void setCreate_time(String create_time) {
            this.create_time = create_time;
        }

        public String getDelete_time() {
            return delete_time;
        }

        public void setDelete_time(String delete_time) {
            this.delete_time = delete_time;
        }

        public List<TrackBean> getList() {
            return list;
        }

        public void setList(List<TrackBean> list) {
            this.list = list;
        }

        public List<Integer> getCategorys() {
            return categorys;
        }

        public void setCategorys(List<Integer> categorys) {
            this.categorys = categorys;
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
