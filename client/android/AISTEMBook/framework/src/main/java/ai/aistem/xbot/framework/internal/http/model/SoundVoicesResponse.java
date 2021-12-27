package ai.aistem.xbot.framework.internal.http.model;


import java.util.List;

import ai.aistem.xbot.framework.data.bean.TrackBean;

public class SoundVoicesResponse {
    private int status;
    private String message;
    private Result result;

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

    public class Result {
        private int count;
        private List<TrackBean> list;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public List<TrackBean> getList() {
            return list;
        }

        public void setList(List<TrackBean> list) {
            this.list = list;
        }
    }
}
