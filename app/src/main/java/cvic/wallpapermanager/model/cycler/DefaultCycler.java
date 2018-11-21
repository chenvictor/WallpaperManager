package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;
import android.graphics.Color;

public class DefaultCycler extends Cycler {

    private Bitmap bitmap;

    DefaultCycler() {
        recalculate();
    }

    @Override
    public void doCycle(boolean random) {
        //No-op
    }

    @Override
    void recalculate() {
        bitmap.recycle();
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.CYAN);
    }

    @Override
    public boolean canCycle() {
        return false;
    }
}
