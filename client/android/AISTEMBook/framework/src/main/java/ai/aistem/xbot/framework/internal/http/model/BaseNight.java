package ai.aistem.xbot.framework.internal.http.model;

public class BaseNight {

    private boolean is_open;
    private String start_time;
    private String end_time;

    @Override
    public String toString() {
        return "BaseNight{" +
                "is_open=" + is_open +
                ", start_time='" + start_time + '\'' +
                ", end_time='" + end_time + '\'' +
                '}';
    }

    public boolean isIs_open() {
        return is_open;
    }

    public void setIs_open(boolean is_open) {
        this.is_open = is_open;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }
}
