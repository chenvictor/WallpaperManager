package cvic.wallpapermanager.model.animation;

import android.graphics.Canvas;

/**
 *  Provides a cross-fade animation.
 *     The incoming image grows in opacity
 *     until it is fully opaque.
 */
public class FadeAnimator extends TransitionAnimator {
    private static final float MAX_ALPHA = 255f;
    private float alpha = 0;
    private float increment;

    @Override
    protected void init() {
        alpha = 0;
        increment = MAX_ALPHA / (getFrameCount() * 2);
    }

    @Override
    protected boolean animate(Canvas canvas) {
        //Draw the state
        canvas.drawBitmap(from, 0, 0, PAINT);   //draw create bitmap
        PAINT.setAlpha((int) alpha);
        canvas.drawBitmap(to, 0, 0, PAINT);       //draw to bitmap with opacity
        PAINT.setAlpha((int) MAX_ALPHA);

        //Increment opacity
        alpha += increment;
        return alpha < MAX_ALPHA;
    }
}
