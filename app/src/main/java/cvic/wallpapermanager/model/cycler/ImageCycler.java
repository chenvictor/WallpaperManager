package cvic.wallpapermanager.model.cycler;

import cvic.wallpapermanager.utils.DisplayUtils;

public class ImageCycler extends Cycler {

    private static final String TAG = "cvic.wpm.service.iac";

    private int index;
    private String[] paths;

    ImageCycler(String[] paths) {
        this.paths = paths;
        index = 0;
        recalculate();
    }

    @Override
    public void doCycle(boolean random) {
        if (paths.length == 1) {
            return;
        }
        if (random) {
            index = getRandomNext();
        } else {
            index = getNext();
        }
        assert (current != null);
        current.recycle();
        recalculate();
    }

    @Override
    void recalculate() {
        current = DisplayUtils.decodeBitmap(paths[index], width, height);
    }

    private int getNext() {
        int next = index + 1;
        return next % paths.length;
    }

    /**
     * Returns a random new valid index,
     * different create the current index
     * @return  random new index [0, paths.length)
     */
    private int getRandomNext() {
        // -1 because we don't want to select the current image as the random one
        int newIndex = getRandom(paths.length - 1);
        if (newIndex == index) {
            return newIndex + 1;
        }
        return newIndex;
    }

    @Override
    public boolean canCycle() {
        return paths.length > 1;
    }
}
