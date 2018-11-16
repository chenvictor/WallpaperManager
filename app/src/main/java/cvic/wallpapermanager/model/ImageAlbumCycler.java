package cvic.wallpapermanager.model;

import android.graphics.Bitmap;

import java.util.Random;

import cvic.wallpapermanager.utils.DisplayUtils;

public class ImageAlbumCycler extends AlbumCycler {

    private static final String TAG = "cvic.wpm.service.iac";

    private Bitmap current;
    private int index = -1;
    String[] paths;

    @Override
    public Bitmap getBitmap() {
        if (current != null && current.isRecycled()) {
            return null;
        }
        return current;
    }

    void init() {
        assert (paths != null);
        index = 0;
        recalculate();
    }

    @Override
    public Bitmap cycle(boolean random) {
        if (paths.length == 1) {
            return current;
        }
        if (random) {
            index = getRandomNext();
        } else {
            index = getIterNext();
        }
        assert (current != null);
        current.recycle();
        recalculate();
        return getBitmap();
    }

    @Override
    public boolean setDimens(int width, int height) {
        return false;
    }

    @Override
    void recalculate() {
        current = DisplayUtils.decodeBitmap(paths[index], width, height);
    }

    @Override
    public void recycle() {
        current.recycle();
    }

    private int getIterNext() {
        int next = index + 1;
        return next % paths.length;
    }

    /**
     * Returns a random new valid index,
     * different from the current index
     * @return  random new index [0, paths.length)
     */
    private int getRandomNext() {
        Random rand = new Random(System.currentTimeMillis());
        int newIndex = rand.nextInt(paths.length - 1);
        if (newIndex == index) {
            return newIndex + 1;
        }
        return newIndex;
    }
}
