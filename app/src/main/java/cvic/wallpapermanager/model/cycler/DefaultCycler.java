package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;
import android.graphics.Color;

public class DefaultCycler extends Cycler {

    private Bitmap bitmap;

    DefaultCycler() {
        recalculate();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public boolean cycle(boolean random) {
        return false;
    }

    @Override
    void recalculate() {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.CYAN);
    }

    @Override
    public void recycle() {
        bitmap.recycle();
    }

    @Override
    public int getCount() {
        return 1;
    }
}
