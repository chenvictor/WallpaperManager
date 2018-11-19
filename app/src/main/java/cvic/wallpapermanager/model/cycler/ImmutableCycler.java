package cvic.wallpapermanager.model.cycler;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

public class ImmutableCycler extends Cycler {

    /**
     * Immutable cycler wrapper
     */
    private final Cycler source;

    public ImmutableCycler(@NonNull Cycler source) {
        this.source = source;
    }

    @Override
    public Bitmap getBitmap() {
        return source.getBitmap();
    }

    @Override
    public boolean cycle(boolean random) {
        return false;
    }

    @Override
    void recalculate() {
        //No-op
    }

    @Override
    public void recycle() {
        //No-op
    }

    @Override
    public boolean setDimens(int width, int height) {
        return false;
        //No-op
    }

    @Override
    public int getCount() {
        return source.getCount();
    }
}
