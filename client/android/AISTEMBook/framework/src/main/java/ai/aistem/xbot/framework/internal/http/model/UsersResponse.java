package ai.aistem.xbot.framework.internal.http.model;

public class UsersResponse {
    private int status;

    private String message;

    private Users result;

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

    public Users getResult() {
        return result;
    }

    public void setResult(Users result) {
        this.result = result;
    }
}
