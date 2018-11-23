package cvic.wallpapermanager.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.SurfaceHolder;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.cycler.Cycler;
import cvic.wallpapermanager.model.cycler.CyclerFactory;
import cvic.wallpapermanager.model.placer.BitmapPlacer;
import cvic.wallpapermanager.model.placer.DefaultPlacer;
import cvic.wallpapermanager.model.placer.FillPlacer;
import cvic.wallpapermanager.model.placer.FitPlacer;
import cvic.wallpapermanager.model.placer.StretchPlacer;

class PreviewHandler {

    /**
     * Handles wallpaper preview
     */
    private final Rect textBounds = new Rect();
    private final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint TEXT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap home;
    private Bitmap lock;
    private BitmapPlacer placer;

    @SuppressWarnings("ConstantConditions")
    PreviewHandler(Context ctx) {
        PAINT.setAlpha(200);
        TEXT.setColor(Color.WHITE);
        TEXT.setTextSize(80);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        setPlacer(prefs.getInt(ctx.getString(R.string.key_position), 0));
        Cycler temp = CyclerFactory.create(ctx, prefs.getString(ctx.getString(R.string.key_wallpaper_home_album), null));
        home = temp.getBitmap().copy(temp.getBitmap().getConfig(), true);
        temp.recycle();
        boolean lockUseHome = prefs.getBoolean(ctx.getString(R.string.key_wallpaper_lock_use_home), false);
        boolean lockBlur = prefs.getBoolean(ctx.getString(R.string.key_wallpaper_lock_blur), false);
        if (lockUseHome) {
            lock = home.copy(home.getConfig(), true);
        } else {
            temp = CyclerFactory.create(ctx, prefs.getString(ctx.getString(R.string.key_wallpaper_lock_album), null));
            lock = temp.getBitmap().copy(temp.getBitmap().getConfig(), true);
            temp.recycle();
        }
        if (lockBlur) {
            blur(ctx, lock);
        }
    }

    private void setPlacer(int idx) {
        switch (idx) {
            case 0:
                placer = new FitPlacer();
                break;
            case 1:
                placer = new FillPlacer();
                break;
            case 2:
                placer = new StretchPlacer();
                break;
            default:
                placer = new DefaultPlacer();
        }
    }

    private void blur(Context ctx, Bitmap bitmap) {
        RenderScript script = RenderScript.create(ctx);
        ScriptIntrinsicBlur blurIntrinsic = ScriptIntrinsicBlur.create(script, Element.U8_4(script));
        blurIntrinsic.setRadius(25f);

        Bitmap temp = bitmap.copy(bitmap.getConfig(), true);
        Allocation tmpIn = Allocation.createFromBitmap(script, temp);
        Allocation tmpOut = Allocation.createFromBitmap(script, bitmap);
        blurIntrinsic.setInput(tmpIn);
        blurIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(bitmap);
        temp.recycle();
        script.destroy();
        blurIntrinsic.destroy();
    }

    void draw(SurfaceHolder holder, int width, int height) {
        Bitmap homePlaced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap lockPlaced = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        placer.positionBitmap(homePlaced, home);
        placer.positionBitmap(lockPlaced, lock);
        Canvas canvas = holder.lockCanvas();
        if (height > width) {
            drawTopBottom(canvas, width, height, homePlaced, lockPlaced);
        } else {
            drawLeftRight(canvas, width, height, homePlaced, lockPlaced);
        }
        holder.unlockCanvasAndPost(canvas);
        homePlaced.recycle();
        lockPlaced.recycle();
    }

    private void drawTopBottom(Canvas canvas, int width, int height, Bitmap homePlaced, Bitmap lockPlaced) {
        canvas.save();
        canvas.clipRect(0, 0, width, height/2);
        canvas.drawBitmap(homePlaced, 0, 0, PAINT);
        drawTextCentered(canvas,"Home screen", width/2, height/4);
        canvas.restore();
        canvas.save();
        canvas.clipRect(0, height/2, width, height);
        canvas.drawBitmap(lockPlaced, 0, 0, PAINT);
        drawTextCentered(canvas, "Lock screen", width/2, height/4*3);
        canvas.restore();
    }

    private void drawLeftRight(Canvas canvas, int width, int height, Bitmap homePlaced, Bitmap lockPlaced) {
        canvas.save();
        canvas.clipRect(0, 0, width/2, height);
        canvas.drawBitmap(homePlaced, 0, 0, PAINT);
        drawTextCentered(canvas, "Home screen", width/4, height/2);
        canvas.restore();
        canvas.save();
        canvas.clipRect(width/2, 0, width, height);
        canvas.drawBitmap(lockPlaced, 0, 0, PAINT);
        drawTextCentered(canvas, "Lock screen", width/4*3, height/2);
        canvas.restore();
    }

    void destroy() {
        home.recycle();
        lock.recycle();
    }

    private void drawTextCentered(Canvas canvas, String text, float cx, float cy){
        TEXT.getTextBounds(text, 0, text.length(), textBounds);
        canvas.drawText(text, cx - textBounds.exactCenterX(), cy - textBounds.exactCenterY(), TEXT);
    }
}
