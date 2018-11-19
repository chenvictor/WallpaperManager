package cvic.wallpapermanager.model.placer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Places the source bitmap so as to fill the destination bitmap.
 *  Maintains source aspect ratio.
 */
public class FillPlacer implements BitmapPlacer {

    private final Paint ANTI_ALIAS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Matrix helperMatrix = new Matrix();

    @Override
    public void positionBitmap(Bitmap dest, Bitmap src) {
        Canvas canvas = new Canvas(dest);
        float srcHeight = src.getHeight();
        float srcWidth = src.getWidth();
        float destHeight = dest.getHeight();
        float destWidth = dest.getWidth();
        float aspectRatioSrc = srcHeight / srcWidth;
        float aspectRatioDest = destHeight / destWidth;
        float offsetX;
        float offsetY;
        float scale;
        if (aspectRatioSrc < aspectRatioDest) {
            //Src more landscape than dest, fit the height
            scale = destHeight / srcHeight;
            offsetY = 0;
            offsetX = (destWidth - srcWidth * scale) / 2;
        } else {
            //Src more portrait or equal, fit the width
            scale = destWidth / srcWidth;
            offsetX = 0;
            offsetY = (destHeight - srcHeight * scale) / 2;
        }
        helperMatrix.reset();
        helperMatrix.postTranslate(offsetX, offsetY);
        helperMatrix.preScale(scale, scale);
        canvas.drawBitmap(src, helperMatrix, ANTI_ALIAS);
    }

}
