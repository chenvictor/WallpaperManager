package cvic.wallpapermanager.model.animation;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.SurfaceHolder;

public abstract class TransitionAnimator {

    private final Handler handler = new Handler();
    private final Runnable cycleRunnable = new Runnable() {
        @Override
        public void run() {
            cycle();
        }
    };

    private boolean isAnimating = false;
    private int fps;
    private int duration;
    private AnimatorListener listener;
    SurfaceHolder holder;
    protected Bitmap from;
    protected Bitmap to;

    /**
     * Requests an animation
     * @param holder    holder to fetch the canvas create
     * @param from      bitmap to cycle create
     * @param to        bitmap to cycle to
     * @param listener  listener to call when animation is done
     * @param fps       frames per second
     * @param duration  animation duration in milliseconds (ms)
     *
     * @throws IllegalArgumentException     if FPS or Duration are <= 0
     */
    public void requestCycle(SurfaceHolder holder, Bitmap from, Bitmap to, AnimatorListener listener, int fps, int duration) {
        if (fps <= 0) {
            throw new IllegalArgumentException("FPS must be > 0");
        }
        if (duration <= 0) {
            throw new IllegalArgumentException("Duration must be > 0");
        }
        this.holder = holder;
        this.from = from;
        this.to = to;
        this.listener = listener;
        this.fps = fps;
        this.duration = duration;
        isAnimating = true;
        init();
        cycle();
    }

    /**
     * Cancels the current cycler, firing the callback immediately
     *  usually used if the CycleAnimator is being replaced
     */
    public void stopCycle() {
        handler.removeCallbacks(cycleRunnable);
        isAnimating = false;
        if (listener != null) {
            listener.runOnFinish();
            listener = null;    //set to null to prevent multiple calls to stopCycle notifying the listener
        }
    }

    public void stopIfAnimating() {
        if (isAnimating) {
            stopCycle();
        }
    }

    private void cycle() {
        if (animate() && isAnimating) {
            handler.postDelayed(cycleRunnable, (1000 / fps));
        } else {
            stopCycle();
        }
    }

    /**
     * Resets animation status so animation can be run again
     */
    protected abstract void init();

    /**
     * Runs one animation step, must call Listener.onUpdate
     *  to notify listener
     * @return  true if the animation is continuing,
     *              false otherwise
     */
    protected abstract boolean animate();

    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Helper functions for subclasses to get the approx.
     *  number of frames the animation will use
     * @return  approx. number of frames
     */
    final int getFrameCount() {
        return duration / fps;
    }

    public interface AnimatorListener {
        void runOnFinish();
    }

}
