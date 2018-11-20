package cvic.wallpapermanager.model.animation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.view.SurfaceHolder;

public abstract class TransitionAnimator {

    private final static int FPS = 40;
    private final static int DURATION = 500;
    private final static int FRAME_COUNT = DURATION / FPS;

    final static Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    private final Handler handler = new Handler();
    private final Runnable cycleRunnable = new Runnable() {
        @Override
        public void run() {
            cycle();
        }
    };

    private boolean isAnimating = false;
    private AnimatorListener listener;
    SurfaceHolder holder;
    float originX;
    float originY;
    protected Bitmap from;
    protected Bitmap to;

    /**
     * Requests an animation
     * @param holder    holder to fetch the canvas create
     * @param from      bitmap to cycle create
     * @param to        bitmap to cycle to
     * @param listener  listener to call when animation is done
     * @param originX    x origin to animate from
     * @param originY    y origin to animate from
     */
    public void requestCycle(SurfaceHolder holder, Bitmap from, Bitmap to, AnimatorListener listener, float originX, float originY) {
        this.holder = holder;
        this.from = from;
        this.to = to;
        this.listener = listener;
        this.originX = originX;
        this.originY = originY;
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
        Canvas canvas = holder.lockCanvas();
        boolean temp = animate(canvas);
        holder.unlockCanvasAndPost(canvas);
        if (temp && isAnimating) {
            handler.postDelayed(cycleRunnable, (1000 / FPS));
        } else {
            stopCycle();
        }
    }

    /**
     * Resets animation status so animation can be run again
     */
    protected abstract void init();

    /**
     * @return  true if the animation is continuing,
     *              false otherwise
     */
    protected abstract boolean animate(Canvas canvas);

    public boolean isAnimating() {
        return isAnimating;
    }

    /**
     * Helper functions for subclasses to get the approx.
     *  number of frames the animation will use
     * @return  approx. number of frames
     */
    final int getFrameCount() {
        return FRAME_COUNT;
    }

    public interface AnimatorListener {
        void runOnFinish();
    }

}
