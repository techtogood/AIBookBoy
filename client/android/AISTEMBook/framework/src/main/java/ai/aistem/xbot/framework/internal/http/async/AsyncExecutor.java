package ai.aistem.xbot.framework.internal.http.async;

/**
 * A simple async executor to run tasks in background thread.
 *
 * @author Tony Green
 * @citer aistem
 * @since 2017/2/22
 */
public abstract class AsyncExecutor {

    /**
     * Task that pending to run.
     */
    private Runnable pendingTask;

    /**
     * Submit a task for pending executing.
     * @param task
     *          The task with specific database operation.
     */
    public void submit(Runnable task) {
        pendingTask = task;
    }

    /**
     * Run the pending task in background thread.
     */
    void execute() {
        if (pendingTask != null) {
            new Thread(pendingTask).start();
        }
    }

}

