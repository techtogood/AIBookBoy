package ai.aistem.xbot.framework.internal.http.async;

public class PostOrGetExecutor extends AsyncExecutor {

    private PostOrGetCallback cb;

    /**
     * Register a callback listener and async task will start executing right away.
     * @param callback
     *          Callback for update or delete records in background.
     */
    public void listen(PostOrGetCallback callback) {
        cb = callback;
        execute();
    }

    public PostOrGetCallback getListener() {
        return  cb;
    }
}
