package ai.aistem.xbot.framework.internal.http.model;

public class AuthTokenResponse {
    private int status;
    private String message;
   // private List<Robot> result = new ArrayList<Robot>();

    private Robot result;

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

   /* public List<Robot> getResult() {
        return result;
    }

    public void setResult(List<Robot> result) {
        this.result = result;
    }

    public void addResult(Robot robot) {
        result.add(robot);
    }*/

    public Robot getResult() {
        return result;
    }

    public void setResult(Robot result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AuthTokenResponse{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
