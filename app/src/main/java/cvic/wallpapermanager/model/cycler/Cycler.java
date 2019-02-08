package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;

import java.util.Random;

import cvic.wallpapermanager.utils.DisplayUtils;

public class Cycler {

    private static final int DEFAULT_WIDTH = 500, DEFAULT_HEIGHT = 1000;

    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;
    private Bitmap current;
    private Random random;
    private int index;
    private String[] paths;

    Cycler(String[] paths) {
        random = new Random(System.currentTimeMillis());
        this.paths = paths;
        index = 0;
        recalculate();
    }

    public Bitmap getBitmap() {
        if (current != null && current.isRecycled()) {
            return null;
        }
        return current;
    }

    /**
     * Method call to recycle any bitmaps
     * in the cycler before deletion
     */
    public void recycle() {
        current.recycle();
    }

    /**
     * Sets the requested size when fetching bitmaps
     * @param width     requested width
     * @param height    requested height
     */
    public void setDimens(int width, int height) {
        if (this.width == width && this.height == height) {
            return;
        }
        this.width = width;
        this.height = height;
        recalculate();
    }

    public void cycle(boolean random) {
        if (paths.length == 1) {
            return;
        }
        if (random) {
            index = randomIndex();
        } else {
            index = nextIndex();
        }
        assert (current != null);
        current.recycle();
        recalculate();
    }

    /**
     * Force Cycler to recalculate current bitmaps
     */
    protected void recalculate() {
        if (current != null) {
            current.recycle();
        }
        current = DisplayUtils.decodeBitmap(paths[index], width, height);
    }

    /**
     * Whether or not the cycler can cycle
     * @return      true if can cycle, i.e. number of images > 1
     */
    public boolean canCycle() {
        return paths.length > 1;
    }

    private int nextIndex() {
        int next = index + 1;
        return next % paths.length;
    }

    /**
     * Returns a random new valid index,
     * different create the current index
     * @return  random new index [0, paths.length)
     */
    private int randomIndex() {
        // -1 because we don't want to select the current image as the random one
        int newIndex = random.nextInt(paths.length - 1);
        if (newIndex == index) {
            return newIndex + 1;
        }
        return newIndex;
    }
}
