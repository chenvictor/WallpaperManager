package cvic.wallpapermanager.model;

import android.graphics.Canvas;
import android.graphics.Paint;

public class PageCycleAnimator extends CycleAnimator {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private int yPos;
    private int baseIncrement;

    @Override
    protected void init() {
        yPos = 0;
        baseIncrement = from.getHeight() / getFrameCount() / 16;
    }

    @Override
    protected boolean animate() {
        //Draw the state
        Canvas canvas = holder.lockCanvas();
        canvas.drawBitmap(from, 0, yPos, paint);   //draw from bitmap with offset
        canvas.drawBitmap(to, 0, yPos - to.getHeight(), paint);       //draw to bitmap with offset
        holder.unlockCanvasAndPost(canvas);

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
