package cvic.wallpapermanager.service;

import java.util.concurrent.TimeUnit;

class IntervalHandler {

    private IntervalTask homeIntervalTask;
    private IntervalTask lockIntervalTask;

    private final IntervalListener listener;
    private boolean enabled;
    private int value;
    private TimeUnit type;

    IntervalHandler(IntervalListener listener) {
        this.listener = listener;
    }

    boolean isEnabled() {
        return enabled;
    }

    void notifyHomeTriggered() {
        if (homeIntervalTask != null) {
            homeIntervalTask.reset();
        }
    }

    void notifyLockTriggered() {
        if (lockIntervalTask != null) {
            lockIntervalTask.reset();
        }
    }

    void notifyHomeChanged(boolean homeCanCycle) {
        if (homeCanCycle) {
            createHomeInterval();
        } else {
            removeHomeInterval();
        }
    }

    void notifyLockChanged(boolean lockCanCycle) {
        if (lockCanCycle) {
            createLockInterval();
        } else {
            removeLockInterval();
        }
    }

    void changeInterval(int intervalValue, TimeUnit intervalType) {
        value = intervalValue;
        type = intervalType;
        if (homeIntervalTask != null) {
            homeIntervalTask.intervalChanged(value, type);
        }
        if (lockIntervalTask != null) {
            lockIntervalTask.intervalChanged(value, type);
        }
    }

    void setIntervalEnabled(boolean enabled, boolean homeCanCycle, boolean lockCanCycle) {
        this.enabled = enabled;
        if (!enabled) {
            removeHomeInterval();
            removeLockInterval();
        } else {
            notifyHomeChanged(homeCanCycle);
            notifyLockChanged(lockCanCycle);
        }
    }

    private void createHomeInterval() {
        if (homeIntervalTask == null) {
            homeIntervalTask = new IntervalTask(new Runnable() {
                @Override
                public void run() {
                    listener.homeTriggered();
                }
            }, "Homeinterval", value, type);
        }
    }

    private void createLockInterval() {
        if (lockIntervalTask == null) {
            lockIntervalTask = new IntervalTask(new Runnable() {
                @Override
                public void run() {
                    listener.lockTriggered();
                }
            }, "Lockinterval", value, type);
        }
    }

    private void removeHomeInterval() {
        if (homeIntervalTask != null) {
            homeIntervalTask.destroy();
        }
    }

    private void removeLockInterval() {
        if (lockIntervalTask != null) {
            lockIntervalTask.destroy();
        }
    }

    public interface IntervalListener {

        void homeTriggered();

        void lockTriggered();

    }

}
