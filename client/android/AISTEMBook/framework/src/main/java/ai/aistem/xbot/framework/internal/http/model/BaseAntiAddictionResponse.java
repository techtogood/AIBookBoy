package ai.aistem.xbot.framework.internal.http.model;

public class BaseAntiAddictionResponse {

    private int status;
    private String message;

    private BaseAntiAddiction result;

    @Override
    public String toString() {
        return "BaseAntiAddictionResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
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

    public BaseAntiAddiction getResult() {
        return result;
    }

    public void setResult(BaseAntiAddiction result) {
        this.result = result;
    }
}
