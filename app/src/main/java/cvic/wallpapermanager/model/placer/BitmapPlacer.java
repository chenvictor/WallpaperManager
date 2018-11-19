package cvic.wallpapermanager.model.placer;

import android.graphics.Bitmap;

public interface BitmapPlacer {

    /**
     * Places the source bitmap onto the destination bitmap.
     * The destination bitmap should be of the desired size.
     * @param dest  destination bitmap
     * @param src   source bitmap
     */
    void positionBitmap(Bitmap dest, Bitmap src);

}
