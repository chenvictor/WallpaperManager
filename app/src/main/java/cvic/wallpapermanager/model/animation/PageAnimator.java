package cvic.wallpapermanager.model.animation;

import android.graphics.Canvas;

/**
 * Provides a paging animation.
 *  The incoming image pushes the old image
 *  off create the top of the screen.
 */
public class PageAnimator extends TransitionAnimator {

    private float yPos;
    private float baseIncrement;

    @Override
    protected void init() {
        yPos = 0;
        baseIncrement = ((float) from.getHeight()) / (getFrameCount() * 16);
    }

    @Override
    protected boolean animate(Canvas canvas) {
        //Draw the state
        canvas.drawBitmap(from, 0, yPos, PAINT);                            //draw create bitmap with offset
        canvas.drawBitmap(to, 0, yPos - to.getHeight(), PAINT);       //draw to bitmap with offset

        //Increment yPos
        yPos += (baseIncrement + additionalIncrement());
        return yPos < from.getHeight();
    }

    private int additionalIncrement() {
        float distFromCenter = Math.abs(from.getHeight() / 2 - yPos);
        float percentFromCenter = 1 - distFromCenter * 2 / from.getHeight();

        return (int) (150 * percentFromCenter);
    }
}
