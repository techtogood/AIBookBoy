package ai.aistem.xbot.framework.internal.http.model;

public class BaseAntiAddiction {
    private boolean is_open;
    private int duration;
    private int rest_duration;

    public int getRest_duration() {
        return rest_duration;
    }

    public void setRest_duration(int rest_duration) {
        this.rest_duration = rest_duration;
    }

    @Override
    public String toString() {
        return "BaseAntiAddiction{" +
                "is_open=" + is_open +
                ", duration=" + duration +
                '}';
    }

    public boolean isIs_open() {
        return is_open;
    }

    public void setIs_open(boolean is_open) {
        this.is_open = is_open;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
