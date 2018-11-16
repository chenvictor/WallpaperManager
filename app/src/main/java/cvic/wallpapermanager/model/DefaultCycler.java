package cvic.wallpapermanager.model;

import android.graphics.Bitmap;
import android.graphics.Color;

public class DefaultCycler extends AlbumCycler {

    private Bitmap bitmap;

    DefaultCycler() {
        recalculate();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public Bitmap cycle(boolean random) {
        return bitmap;
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
}
