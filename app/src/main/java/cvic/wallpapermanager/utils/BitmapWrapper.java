package cvic.wallpapermanager.utils;

import android.graphics.Bitmap;

public class BitmapWrapper {

    private int refCount;
    private final Bitmap bitmap;

    BitmapWrapper(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.refCount = 1;
    }

    public Bitmap getBitmap() {
        if (refCount == 0 || bitmap.isRecycled()) {
            throw new RuntimeException("The bitmap has already been recycled!");
        }
        return this.bitmap;
    }

    public void incRef() {
        refCount++;
    }

    public void decRef() {
        if (refCount-- == 1) {
            bitmap.recycle();
        }
    }

}
