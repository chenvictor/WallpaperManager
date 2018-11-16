package cvic.wallpapermanager.model;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 *  Provides a cross-fade animation.
 *     The incoming image grows in opacity
 *     until it is fully opaque.
 */
public class FadeCycleAnimator extends CycleAnimator {
    private int alpha = 0;
    private Paint fromPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Paint toPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private int increment;

    @Override
    protected void init() {
        final int MAX_ALPHA = 255;
        alpha = 0;
        increment = MAX_ALPHA / getFrameCount();
    }

    @Override
    protected boolean animate() {
        //Draw the state
        Canvas canvas = holder.lockCanvas();
        toPaint.setAlpha(alpha);
        //fromPaint.setAlpha(255 - alpha);
        canvas.drawBitmap(from, 0, 0, fromPaint);   //draw from bitmap
        canvas.drawBitmap(to, 0, 0, toPaint);       //draw to bitmap with opacity
        holder.unlockCanvasAndPost(canvas);

        //Increment opacity
        alpha += increment;
        return alpha < 255;
    }
}
