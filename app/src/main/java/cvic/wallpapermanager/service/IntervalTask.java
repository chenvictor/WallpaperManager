package cvic.wallpapermanager.service;

import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class IntervalTask {

    private static final String TAG = "cvic.wpm.it";

    private final String debugId;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture task;
    private final Runnable runnable;
    private int interval;
    private TimeUnit intervalType;

    IntervalTask(Runnable runnable, String debugId, int interval, TimeUnit intervalType) {
        if (interval <= 0) {
            throw new IllegalArgumentException("Interval must be greater than 0");
        }
        if (intervalType == null) {
            throw new IllegalArgumentException("Interval type is null!");
        }
        scheduler = Executors.newScheduledThreadPool(1);
        Log.i(TAG, "Interval set - " + debugId + ": " + interval + " " + intervalType.name());
        this.interval = interval;
        this.intervalType = intervalType;
        this.debugId = debugId;
        this.runnable = runnable;

        task = scheduler.scheduleAtFixedRate(runnable, interval, interval, intervalType);
    }

    void destroy() {
        Log.i(TAG, "Task destroyed: " + debugId);
        clearTask();
        scheduler.shutdownNow();
    }

    /**
     * Reset the interval
     */
    void reset() {
        Log.i(TAG, "Task reset: " + debugId);
        clearTask();
        //Restart the task
        task = scheduler.scheduleAtFixedRate(runnable, interval, interval, intervalType);
    }

    void intervalChanged(int newValue, TimeUnit newTimeUnit) {
        interval = newValue;
        intervalType = newTimeUnit;
        reset();
    }

    private void clearTask() {
        if (task != null) {
            if (task.cancel(true)) {
                Log.i(TAG, "Cancelled pending: " + debugId);
            } else {
                Log.i(TAG, "Failed to clear Task pending: " + debugId);
            }
        }
    }
}
