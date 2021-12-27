package ai.aistem.xbot.framework.internal.http.model;

public class BaseNightResponse {

    private int status;
    private String message;

    private BaseNight result;

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

    public BaseNight getResult() {
        return result;
    }

    public void setResult(BaseNight result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "BaseNightResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
