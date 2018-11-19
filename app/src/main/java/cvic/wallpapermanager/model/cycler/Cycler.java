package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;

public abstract class Cycler {

    private static final int DEFAULT_WIDTH = 500, DEFAULT_HEIGHT = 1000;

    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;

    public abstract Bitmap getBitmap();

    /**
     * Cycles the cycler
     * @param random    should it cycle randomly?
     * @return      true if a new image was cycled to
     */
    public abstract boolean cycle(boolean random);

    /**
     * Sets the requested size when fetching bitmaps
     * @param width     requested width
     * @param height    requested height
     * @return      true if the size was changed and bitmap recalculated,
     *              false otherwise
     */
    public boolean setDimens(int width, int height) {
        if (this.width == width && this.height == height) {
            return false;
        }
        this.width = width;
        this.height = height;
        recalculate();
        return true;
    }

    /**
     * Force Cycler to recalculate current bitmaps
     */
    abstract void recalculate();

    /**
     * Method call to recycle any bitmaps
     * in the cycler before deletion
     */
    public abstract void recycle();

    /**
     * Returns the number of images in the cycler
     * @return  number of images
     */
    public abstract int getCount();

}
