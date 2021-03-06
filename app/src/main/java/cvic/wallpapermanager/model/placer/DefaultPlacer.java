package cvic.wallpapermanager.model.placer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

/**
 * A default placer fallback.
 *  Does not use the source image, instead filling
 *  the destination with a solid fill.
 */
public class DefaultPlacer implements BitmapPlacer {

    private static final int DEFAULT_COLOR = Color.WHITE;

    @Override
    public void positionBitmap(Bitmap dest, Bitmap src) {
        Canvas canvas = new Canvas(dest);
        canvas.drawColor(DEFAULT_COLOR);
    }

}
