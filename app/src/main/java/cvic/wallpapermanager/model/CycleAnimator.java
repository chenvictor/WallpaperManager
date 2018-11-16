package cvic.wallpapermanager.model;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.SurfaceHolder;

public abstract class CycleAnimator  {

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
     * @param holder    holder to fetch the canvas from
     * @param from      bitmap to cycle from
     * @param to        bitmap to cycle to
     * @param listener  listener to call when animation is done
     * @param fps       frames per second
     * @param duration  animation duration in milliseconds (ms)
     */
    public void requestCycle(SurfaceHolder holder, Bitmap from, Bitmap to, AnimatorListener listener, int fps, int duration) {
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
    public void cancelCycle() {
        handler.removeCallbacks(cycleRunnable);
        isAnimating = false;
        listener.runOnFinish();
    }

    private void cycle() {
        if (animate() && isAnimating) {
            handler.postDelayed(cycleRunnable, (1000 / fps));
        } else {
            isAnimating = false;
            listener.runOnFinish();
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
