package cvic.wallpapermanager.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.util.concurrent.TimeUnit;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.cycler.Cycler;
import cvic.wallpapermanager.model.cycler.CyclerFactory;
import cvic.wallpapermanager.model.placer.BitmapPlacer;
import cvic.wallpapermanager.model.placer.DefaultPlacer;
import cvic.wallpapermanager.model.placer.FillPlacer;
import cvic.wallpapermanager.model.placer.FitPlacer;
import cvic.wallpapermanager.model.placer.StretchPlacer;
import cvic.wallpapermanager.utils.TimeUtils;

public class BitmapHandler implements IntervalHandler.IntervalListener {

    /**
     * Handles bitmaps states of the lockscreen and homescreen,
     *  cycling on interval when required
     */

    private static final int DEFAULT_WIDTH = 500, DEFAULT_HEIGHT = 1000;

    private static final int POSITION_FIT = 0;
    private static final int POSITION_FILL = 1;
    private static final int POSITION_STRETCH = 2;

    private final Context ctx;
    private final BitmapReceiver receiver;
    private int decodeWidth = DEFAULT_WIDTH;
    private int decodeHeight = DEFAULT_HEIGHT;

    private BitmapPlacer bitmapPlacer;
    private IntervalHandler intervalHandler;

    /**
     * Cyclers will handle cycling of album,
     *  and contain the current decoded bitmap
     *  in its native form
     */
    private Cycler homeCycler;
    private Cycler lockCycler;

    /**
     * Bitmaps will hold the current ready to draw bitmaps,
     *  with positioning and blur effects already applied
     */
    private Bitmap homeBitmap;
    private Bitmap lockBitmap;

    /**
     * Scripts to handle blurring of images
     */
    private RenderScript script;
    private ScriptIntrinsicBlur blurIntrinsic;

    /**
     * True if the device is currently locked,
     *  false otherwise (device unlocked)
     */
    private boolean isLocked = false;
    private boolean lockUseHome;

    /**
     * Whether or not cyclers should cycle in random order
     */
    private boolean randomOrder;

    BitmapHandler(Context ctx, BitmapReceiver receiver) {
        this.receiver = receiver;
        this.ctx = ctx;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        setBlur(prefs.getBoolean(ctx.getString(R.string.key_wallpaper_lock_blur), false));
        setPosition(prefs.getInt(ctx.getString(R.string.key_position),0));
        intervalHandler = new IntervalHandler(this);
        randomOrder = prefs.getBoolean(ctx.getString(R.string.key_random_order_enabled), false);
        setInterval(prefs);
        initCyclers(prefs.getBoolean(ctx.getString(R.string.key_wallpaper_lock_use_home), false), prefs);

    }

    private void initCyclers(boolean lockUseHome, SharedPreferences prefs) {
        this.lockUseHome = lockUseHome;
        createHomeCycler(prefs.getString(ctx.getString(R.string.key_wallpaper_home_album), null));
        setLockUseHome(lockUseHome, prefs);
    }

    void destroy() {
        setBlur(false); //destroy the renderscript
        homeBitmap.recycle();
        lockBitmap.recycle();
        homeCycler.recycle();
        lockCycler.recycle();
        intervalHandler.setIntervalEnabled(false, false, false);
    }

    /**
     * Sets the bitmap decode dimensions for the cyclers
     * @param width     width to set
     * @param height    height to set
     */
    void setDecodeDimens(int width, int height) {
        decodeWidth = width;
        decodeHeight = height;
        if (homeCycler != null) {
            homeCycler.setDimens(width, height);
        }
        if (lockCycler != null) {
            lockCycler.setDimens(width, height);
        }
    }

    /**
     * Fetch the current bitmap to be drawn
     * @return  bitmap to be drawn
     */
    public Bitmap getBitmap() {
       if (isLocked) {
           return lockBitmap;
       } else {
           return homeBitmap;
       }
    }

    /**
     * Cycles the home album
     */
    void cycleHome() {
        if (homeCanCycle()) {
            homeCycler.cycle(randomOrder);
            Bitmap temp = homeBitmap.copy(homeBitmap.getConfig(), true);
            positionHome();
            if (!isLocked) {
                receiver.requestCycle(temp, getBitmap());
            }
            if (lockUseHome) {
                positionLock();
            }
        }
    }

    /**
     * Cycles the lock album
     */
    void cycleLock() {
        if (lockCanCycle()) {
            lockCycler.cycle(randomOrder);
            if (isLocked) {
                Bitmap temp = lockBitmap.copy(lockBitmap.getConfig(), true);
                positionLock();
                receiver.requestCycle(temp, getBitmap());
            } else {
                positionLock();
            }
            if (lockUseHome) {
                positionHome();
            }
        }
    }

    void notifyLocked() {
        isLocked = true;
        if (!lockUseHome || script != null) {
            receiver.requestDraw();
        }
    }

    void notifyUnlocked() {
        isLocked = false;
        if (!lockUseHome || script != null) {
            receiver.requestDraw();
        }
    }

    /**
     * Recalculate the home screen only (lock screen cannot be in landscape mode)
     * @param width     new width
     * @param height    new height
     */
    void notifyScreenSizeChanged(int width, int height) {
        if (homeBitmap != null) {
            homeBitmap.recycle();
        }
        if (lockBitmap == null) {
            lockBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        homeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        positionHome();
        positionLock();
    }

    private void positionHome() {
        if (homeBitmap != null) {
            bitmapPlacer.positionBitmap(homeBitmap, homeCycler.getBitmap());
        }
    }

    private void positionLock() {
        if (lockBitmap != null) {
            bitmapPlacer.positionBitmap(lockBitmap, lockCycler.getBitmap());
            applyBlur(lockBitmap);
        }
    }

    /**
     * Handles preference changes
     * @param prefs     a reference to SharedPreferences
     * @param key       key of the preference that was changed
     */
    void notifyPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(ctx.getString(R.string.key_wallpaper_lock_blur))) {
            setBlur(prefs.getBoolean(key, false));
        } else if (key.equals(ctx.getString(R.string.key_wallpaper_lock_use_home))) {
            setLockUseHome(prefs.getBoolean(key, false), prefs);
        } else if (key.equals(ctx.getString(R.string.key_position))) {
            setPosition(prefs.getInt(key, 0));
            //Reposition home and lock screen
            positionHome();
            positionLock();
            receiver.requestDraw();
        } else if (key.equals(ctx.getString(R.string.key_random_order_enabled))) {
            randomOrder = prefs.getBoolean(key, false);
        } else if (key.equals(ctx.getString(R.string.key_wallpaper_home_album))) {
            homeAlbumChanged(prefs.getString(key, null));
        } else if (key.equals(ctx.getString(R.string.key_wallpaper_lock_album))) {
            lockAlbumChanged(prefs.getString(key, null));
        } else if (key.equals(ctx.getString(R.string.key_wallpaper_interval_enabled))) {
            intervalHandler.setIntervalEnabled(prefs.getBoolean(key, false), homeCanCycle(), lockCanCycle());
        } else if (key.equals(ctx.getString(R.string.key_wallpaper_interval_type)) || key.equals(ctx.getString(R.string.key_wallpaper_interval_value))) {
            if (intervalHandler.isEnabled()) {
                setInterval(prefs);
            }
        }
    }

    private void setInterval(SharedPreferences prefs) {
        int value = prefs.getInt(ctx.getString(R.string.key_wallpaper_interval_value), 0);
        TimeUnit type = TimeUtils.getTimeUnit(prefs.getInt(ctx.getString(R.string.key_wallpaper_interval_type), 0));
        intervalHandler.changeInterval(value, type);
    }

    /**
     * Initializes the blur script if the lock screen should be blurred.
     *  Otherwise, sets the script to null
     * @param shouldBlur     whether or not the lock screen should be blurred
     */
    private void setBlur(boolean shouldBlur) {
        if (shouldBlur) {
            script = RenderScript.create(ctx);
            blurIntrinsic = ScriptIntrinsicBlur.create(script, Element.U8_4(script));
            blurIntrinsic.setRadius(25f);
        } else {
            if (script != null) {
                blurIntrinsic.destroy();
                script.destroy();
            }
            script = null;
            blurIntrinsic = null;
        }
        //Reposition the lock screen
        positionLock();
    }

    private void setLockUseHome(boolean lockUseHome, SharedPreferences prefs) {
        this.lockUseHome = lockUseHome;
        if (lockUseHome) {
            //Free the lock cycler, simply reference the home cycler to retrieve images
            lockCycler = homeCycler;
        } else {
            //Otherwise, create independent lock cycler
            createLockCycler(prefs.getString(ctx.getString(R.string.key_wallpaper_lock_album), null));
        }
        positionLock(); //recalculate the lock screen
        if (isLocked) {
            receiver.requestDraw(); //redraw lock screen
        }
    }

    private void homeAlbumChanged(String prefValue) {
        createHomeCycler(prefValue);
        if (isLocked) {
            if (lockUseHome) {
                //Cycle the lock, and request the lock screen be animated
                Bitmap temp = lockBitmap.copy(lockBitmap.getConfig(), true);
                positionHome();
                positionLock();
                receiver.requestCycle(temp, lockBitmap);
            }
        } else {
            //Cycle home, request home screen be animated
            Bitmap temp = homeBitmap.copy(homeBitmap.getConfig(), true);
            positionHome();
            if (lockUseHome) {
                //Reposition the lock as well
                positionLock();
            }
            receiver.requestCycle(temp, homeBitmap);
        }
    }

    private void lockAlbumChanged(String prefValue) {
        if (!lockUseHome) {
            createLockCycler(prefValue);
            if (isLocked) {
                // Holder a copy of the old bitmap to animate
                Bitmap temp = lockBitmap.copy(lockBitmap.getConfig(), true);
                positionLock();
                receiver.requestCycle(temp, lockBitmap);
            } else {
                positionLock();
            }
        }
    }

    private void createHomeCycler(String prefValue) {
        if (homeCycler != null) {
            homeCycler.recycle();
        }
        homeCycler = CyclerFactory.create(ctx, prefValue);
        homeCycler.setDimens(decodeWidth, decodeHeight);
        if (lockUseHome) {
            lockCycler = homeCycler;
        }
        intervalHandler.notifyHomeChanged(homeCanCycle());
    }

    private void createLockCycler(String prefValue) {
        if (lockCycler != null) {
            lockCycler.recycle();
        }
        lockCycler = CyclerFactory.create(ctx, prefValue);
        lockCycler.setDimens(decodeWidth, decodeHeight);
        intervalHandler.notifyLockChanged(lockCanCycle());
    }

    private void setPosition(int positionType) {
        switch (positionType) {
            case POSITION_FIT:
                bitmapPlacer = new FitPlacer();
                break;
            case POSITION_FILL:
                bitmapPlacer = new FillPlacer();
                break;
            case POSITION_STRETCH:
                bitmapPlacer = new StretchPlacer();
                break;
            default:
                bitmapPlacer = new DefaultPlacer();
        }
    }

    /**
     * Applies a gaussian blur using Renderscript
     *  iff script is not null
     * @param original      bitmap to blur
     */
    private void applyBlur(Bitmap original) {
        if (script == null) {
            //Script is null, therefore skip blur
            return;
        }
        Bitmap temp = original.copy(original.getConfig(), true);
        Allocation tmpIn = Allocation.createFromBitmap(script, temp);
        Allocation tmpOut = Allocation.createFromBitmap(script, original);
        blurIntrinsic.setInput(tmpIn);
        blurIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(original);
        temp.recycle();
    }

    private boolean homeCanCycle() {
        return homeCycler.canCycle();
    }

    private boolean lockCanCycle() {
        return lockCycler.canCycle();
    }

    @Override
    public void homeTriggered() {
        cycleHome();
    }

    @Override
    public void lockTriggered() {
        cycleLock();
    }

    public interface BitmapReceiver {

        /**
         * Notifies a receiver that a bitmap cycle has occurred
         *  on the current screen. (home vs lock)
         * @param from              bitmap being cycled from
         *                              NOTE: Should be recycled after use
         * @param to                bitmap being cycled to
         */
        void requestCycle(Bitmap from, Bitmap to);

        /**
         * Notifies a receiver that a new bitmap should be drawn
         */
        void requestDraw();

    }

}
