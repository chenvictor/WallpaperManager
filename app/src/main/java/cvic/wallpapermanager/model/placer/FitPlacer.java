package cvic.wallpapermanager.model.placer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Places the bitmap so as to fit the source within the destination.
 *  Maintains source aspect ratio.
 */
public class FitPlacer implements BitmapPlacer {

    /**
     * Color to fill blank areas when fitting image
     */
    private static final int FILL_COLOR = Color.BLACK;
    private final Paint ANTI_ALIAS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix fitMatrix = new Matrix();

    @Override
    public void positionBitmap(Bitmap dest, Bitmap src) {
        Canvas canvas = new Canvas(dest);
        //fitMatrix.reset();
        canvas.drawColor(FILL_COLOR); //Fill needs to paint over since it may not cover the whole screen
        RectF targetRect = new RectF(0, 0, dest.getWidth(), dest.getHeight());
        RectF sourceRect = new RectF(0, 0, src.getWidth(), src.getHeight());
        fitMatrix.setRectToRect(sourceRect, targetRect, Matrix.ScaleToFit.CENTER);
        canvas.drawBitmap(src, fitMatrix, ANTI_ALIAS);
    }

}
