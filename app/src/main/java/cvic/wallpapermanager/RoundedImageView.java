package cvic.wallpapermanager;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

public class RoundedImageView extends android.support.v7.widget.AppCompatImageView {

    private float cornerRadius;

    private BitmapShader mShader;
    private Paint mPaint;
    private RectF mRect;

    public RoundedImageView(Context context) {
        super(context);
        init(null);
    }

    public RoundedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public RoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) {
            cornerRadius = 0f;
        } else {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedImageView, 0, 0);
            try {
                cornerRadius = array.getDimensionPixelSize(R.styleable.RoundedImageView_cornerRadius, 0);
                BitmapDrawable draw = (BitmapDrawable) getDrawable();
                mShader = new BitmapShader(draw.getBitmap(), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                mRect = new RectF(0, 0, getWidth(), getHeight());
                mPaint = new Paint();
                mPaint.setAntiAlias(true);
                mPaint.setShader(mShader);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                array.recycle();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShader == null || mRect == null || mPaint == null) {
            super.onDraw(canvas);
        } else {
            canvas.drawRoundRect(mRect, cornerRadius, cornerRadius, mPaint);
        }
    }
}
