package ai.aistem.xbot.framework.internal.http.model;


public class VoiceResponse {
    private int status;
    private String message;
    private Voice result;

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

    public Voice getResult() {
        return result;
    }

    public void setResult(Voice result) {
        this.result = result;
    }
}
