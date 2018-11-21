package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;

import java.util.Random;

public abstract class Cycler {

    private static final int DEFAULT_WIDTH = 500, DEFAULT_HEIGHT = 1000;

    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;
    Bitmap current;
    private Random random;

    Cycler() {
        random = new Random(System.currentTimeMillis());
    }

    public final Bitmap getBitmap() {
        if (current != null && current.isRecycled()) {
            return null;
        }
        return current;
    }

    public final void cycle(boolean random) {
        doCycle(random);
    }

    /**
     * Method call to recycle any bitmaps
     * in the cycler before deletion
     */
    public final void recycle() {
        current.recycle();
    }

    /**
     * Sets the requested size when fetching bitmaps
     * @param width     requested width
     * @param height    requested height
     */
    public final void setDimens(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;
        recalculate();
    }

    /**
     * Helper function subclasses can call to get a random number
     * @param bound     upper bound
     * @return          a number in the range [0, bound)
     */
    final int getRandom(int bound) {
        return random.nextInt(bound);
    }

    abstract void doCycle(boolean random);

    /**
     * Force Cycler to recalculate current bitmaps
     */
    abstract void recalculate();

    /**
     * Whether or not the cycler can cycle
     * @return      true if can cycle, i.e. number of images > 1
     */
    public abstract boolean canCycle();
}
