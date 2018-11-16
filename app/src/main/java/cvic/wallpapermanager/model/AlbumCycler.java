package cvic.wallpapermanager.model;

import android.graphics.Bitmap;

public abstract class AlbumCycler {

    private static final int DEFAULT_WIDTH = 500, DEFAULT_HEIGHT = 1000;

    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;

    public abstract Bitmap getBitmap();
    public abstract Bitmap cycle(boolean random);

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

}
