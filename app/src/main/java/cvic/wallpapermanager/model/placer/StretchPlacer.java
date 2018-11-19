package cvic.wallpapermanager.model.placer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public class StretchPlacer implements BitmapPlacer {

    private final Paint ANTI_ALIAS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    @Override
    public void positionBitmap(Bitmap dest, Bitmap src) {
        Canvas canvas = new Canvas(dest);
        RectF targetRect = new RectF(0, 0, dest.getWidth(), dest.getHeight());
        canvas.drawBitmap(src, null, targetRect, ANTI_ALIAS);
    }

}
