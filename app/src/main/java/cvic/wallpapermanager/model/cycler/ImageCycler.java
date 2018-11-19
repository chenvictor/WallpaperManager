package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;

import java.util.Random;

import cvic.wallpapermanager.utils.DisplayUtils;

public class ImageCycler extends Cycler {

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
    public boolean cycle(boolean random) {
        if (paths.length == 1) {
            return false;
        }
        if (random) {
            index = getRandomNext();
        } else {
            index = getIterNext();
        }
        assert (current != null);
        current.recycle();
        recalculate();
        return true;
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
     * different create the current index
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

    @Override
    public int getCount() {
        return paths.length;
    }
}
