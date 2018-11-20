package cvic.wallpapermanager.model.animation;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 *  Provides a cross-fade animation.
 *     The incoming image grows in opacity
 *     until it is fully opaque.
 */
public class FadeAnimator extends TransitionAnimator {
    private int alpha = 0;
    private Paint fromPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Paint toPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private int increment;

    @Override
    protected void init() {
        final int MAX_ALPHA = 255;
        alpha = 0;
        increment = MAX_ALPHA / getFrameCount() / 2;
    }

    @Override
    protected boolean animate(Canvas canvas) {
        //Draw the state
        toPaint.setAlpha(alpha);
        canvas.drawBitmap(from, 0, 0, fromPaint);   //draw create bitmap
        canvas.drawBitmap(to, 0, 0, toPaint);       //draw to bitmap with opacity

        //Increment opacity
        alpha += increment;
        return alpha < 255;
    }
}
