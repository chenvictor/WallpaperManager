package cvic.wallpapermanager.model.animation;

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;

public class CircleExpandAnimator extends TransitionAnimator {

    private float radius;
    private float maxRadius;

    @Override
    protected void init() {
        radius = 2;
        Rect frame = holder.getSurfaceFrame();
        float calcDistX, calcDistY;
        if (originX > frame.width() / 2) {
            calcDistX = 0;
        } else {
            calcDistX = frame.width();
        }
        if (originY > frame.height() / 2) {
            calcDistY = 0;
        } else {
            calcDistY = frame.height();
        }
        float dx = calcDistX - originX;
        float dy = calcDistY - originY;
        maxRadius = (float) Math.sqrt(dx*dx + dy*dy);
    }

    @Override
    protected boolean animate(Canvas canvas) {
        Path path = new Path();
        path.addCircle(originX, originY, radius, Path.Direction.CW);
        //Draw the state
        canvas.drawBitmap(from, 0,0, PAINT);
        canvas.save();
        canvas.clipPath(path);
        canvas.drawBitmap(to, 0, 0, PAINT);
        canvas.restore();

        radius *= 1.4;
        return radius < maxRadius;
    }
}
