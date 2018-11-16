package cvic.wallpapermanager.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.AlbumCycler;
import cvic.wallpapermanager.model.AlbumCyclerFactory;
import cvic.wallpapermanager.model.CrossFadeCycleAnimator;
import cvic.wallpapermanager.model.CycleAnimator;
import cvic.wallpapermanager.model.FlipCycleAnimator;
import cvic.wallpapermanager.model.NullCycleAnimator;
import cvic.wallpapermanager.model.PageCycleAnimator;

public class WPMService extends WallpaperService {

    private static final int DEFAULT_WIDTH = 500, DEFAULT_HEIGHT = 1000;
    private static final String TAG = "cvic.wpm.service";
    private static final int POSITION_FIT = 0;
    private static final int POSITION_FILL = 1;
    private static final int POSITION_STRETCH = 2;

    private static final int TRANSITION_FADE = 0;
    private static final int TRANSITION_PAGE = 1;
    private static final int TRANSITION_FLIP = 2;

    @Override
    public Engine onCreateEngine() {
        return new WPMEngine();
    }

    private class WPMEngine extends WallpaperService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private final Paint ANTI_ALIAS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        private boolean dimensInit = false;

        private CycleAnimator cycleAnimator;
        private AlbumCycler homeCycler;
        private AlbumCycler lockCycler;

        private RenderScript script;
        private ScriptIntrinsicBlur blurIntrinsic;
        private Context ctx = WPMService.this;
        private GestureDetector gestureDetector;
        private PhoneUnlockedReceiver receiver;

        private Matrix helperMatrix = new Matrix();
        private int width = DEFAULT_WIDTH;
        private int height = DEFAULT_HEIGHT;

        /**
         * Preferences
         */
        private boolean doubleTap;
        private boolean blurLock;
        private boolean lockUseHome;
        private int position;
        private boolean randomOrder;
        private boolean changeOnInterval;

        private WPMEngine() {
            gestureDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    doubleTapped();
                    return true;
                }
            });
            receiver = new PhoneUnlockedReceiver(this);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WPMService.this);
            doubleTap = doubleTapEnabled(prefs);
            blurLock = blurLockEnabled(prefs);
            lockUseHome = lockUseHomeEnabled(prefs);
            position = getPositionType(prefs);
            loadRenderScript();
            loadCyclers(prefs);
            //cycleAnimator = new CrossFadeCycleAnimator();
            updateTransitionType(prefs);
        }

        private void loadRenderScript() {
            script = RenderScript.create(WPMService.this);
            blurIntrinsic = ScriptIntrinsicBlur.create(script, Element.U8_4(script));
            blurIntrinsic.setRadius(25f);
        }

        private void loadCyclers(SharedPreferences prefs) {
            if (homeCycler != null) {
                homeCycler.recycle();
            }
            if (lockCycler != null) {
                lockCycler.recycle();
            }
            homeCycler = AlbumCyclerFactory.from(prefs.getString(getString(R.string.key_wallpaper_home_album), getString(R.string.uninitialized)));
            lockCycler = AlbumCyclerFactory.from(prefs.getString(getString(R.string.key_wallpaper_lock_album), getString(R.string.uninitialized)));
        }

        private void setDimens(int width, int height) {
            this.width = width;
            this.height = height;
            boolean temp = homeCycler.setDimens(width, height);
            boolean temp2 = lockCycler.setDimens(width, height);
            if (temp || temp2) {
                requestDraw();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            prefs.registerOnSharedPreferenceChangeListener(this);
            IntentFilter lockUnlock = new IntentFilter();
            lockUnlock.addAction(Intent.ACTION_USER_PRESENT);
            lockUnlock.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(receiver, lockUnlock);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            PreferenceManager.getDefaultSharedPreferences(ctx).unregisterOnSharedPreferenceChangeListener(this);
            unregisterReceiver(receiver);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            loadCyclers(prefs);
            requestDraw();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (!dimensInit) {
                Log.i(TAG, "Initializing dimens: " + width + "x" + height);
                setDimens(width, height);
                dimensInit = true;
            }
            requestDraw();
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (doubleTap) {
                gestureDetector.onTouchEvent(event);
            }
        }

        private void notifyLocked() {
            Log.i(TAG, "phone locked!");
            requestDraw();
        }

        private void notifyUnlocked() {
            Log.i(TAG, "phone unlocked!");
            requestDraw();
        }

        private void doubleTapped() {
            Log.i(TAG, "double tapped");
            if (cycleAnimator.isAnimating()) {
                return; //in middle of another cycle, wait
            }

            final Bitmap from = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            final Bitmap to = from.copy(from.getConfig(), true);
            positionBitmap(from , homeCycler.getBitmap());
            homeCycler.cycle(randomOrder);
            positionBitmap(to, homeCycler.getBitmap());
            cycleAnimator.requestCycle(getSurfaceHolder(), from, to, new CycleAnimator.AnimatorListener() {
                @Override
                public void runOnFinish() {
                    from.recycle();
                    to.recycle();
                    requestDraw();
                }
            }, 40, 500);
        }

        private boolean doubleTapEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_wallpaper_home_double_tap_enabled), false);
        }

        private boolean blurLockEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_wallpaper_lock_blur), false);
        }

        private boolean lockUseHomeEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_wallpaper_lock_use_home), true);
        }

        private int getPositionType(SharedPreferences prefs) {
            return prefs.getInt(getString(R.string.key_position), POSITION_FIT);
        }

        private void updateTransitionType(SharedPreferences prefs) {
            switch (prefs.getInt(getString(R.string.key_transition), TRANSITION_FADE)) {
                case TRANSITION_FADE:
                    cycleAnimator = new CrossFadeCycleAnimator();
                    break;
                case TRANSITION_PAGE:
                    cycleAnimator = new PageCycleAnimator();
                    break;
                case TRANSITION_FLIP:
                    cycleAnimator = new FlipCycleAnimator();
                default:
                    cycleAnimator = new NullCycleAnimator();
            }
        }

        private boolean getRandomOrderEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_random_order_enabled), false);
        }

        private boolean getChangeOnIntervalEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_wallpaper_interval_enabled), true);
        }

        private void requestDraw() {
            Bitmap toDraw;
            if (receiver.locked && !lockUseHome) {
                Log.i(TAG, "Drawing lockscreen");
                toDraw = lockCycler.getBitmap();
            } else {
                Log.i(TAG, "Drawing homescreen");
                toDraw = homeCycler.getBitmap();
            }
            if (receiver.locked && blurLock) {
                Bitmap blur = toDraw.copy(toDraw.getConfig(), true);
                applyBlur(toDraw, blur);
                drawImage(blur);
                blur.recycle();
            } else {
                drawImage(toDraw);
            }
        }

        private void drawImage(Bitmap bitmap) {
            if (bitmap == null) {
                Log.i(TAG, "bitmap was null");
                return;
            }
            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null) {
                    switch (position) {
                        case POSITION_FIT:
                            drawFit(canvas, holder, bitmap);
                            break;
                        case POSITION_FILL:
                            drawFill(canvas, holder, bitmap);
                            break;
                        case POSITION_STRETCH:
                            drawStretch(canvas, holder, bitmap);
                            break;
                        default:
                            drawStretch(canvas, holder, bitmap);
                    }
                } else {
                    Log.i(TAG, "canvas was null");
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void drawFit(Canvas canvas, SurfaceHolder holder, Bitmap bitmap) {
            helperMatrix.reset();
            canvas.drawColor(Color.BLACK); //Fill needs to paint over since it may not cover the whole screen
            if (helperMatrix.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(holder.getSurfaceFrame()), Matrix.ScaleToFit.CENTER)) {
                canvas.drawBitmap(bitmap, helperMatrix, ANTI_ALIAS);
            } else {
                //Default to stretch
                Log.e(TAG, "Failed to set fill matrix, default to stretch");
                drawStretch(canvas, holder, bitmap);
            }
        }

        private void drawFill(Canvas canvas, SurfaceHolder holder, Bitmap bitmap) {
            helperMatrix.reset();
            float srcHeight = bitmap.getHeight();
            float srcWidth = bitmap.getWidth();
            float destHeight = holder.getSurfaceFrame().height();
            float destWidth = holder.getSurfaceFrame().width();
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
            helperMatrix.postTranslate(offsetX, offsetY);
            helperMatrix.preScale(scale, scale);
            canvas.drawBitmap(bitmap, helperMatrix, ANTI_ALIAS);
        }

        private void drawStretch(Canvas canvas, SurfaceHolder holder, Bitmap bitmap) {
            canvas.drawBitmap(bitmap, null, holder.getSurfaceFrame(), ANTI_ALIAS);
        }

        /**
         * Positions a src bitmap onto the target according to the position type
         * @param target        target bitmap, should be of the desired height
         * @param src           source bitmap
         */
        private void positionBitmap(Bitmap target, Bitmap src) {
            switch (position) {
                case POSITION_FIT:
                    positionFit(target, src);
                    break;
                case POSITION_FILL:
                    positionFill(target, src);
                    break;
                case POSITION_STRETCH:
                    positionStretch(target, src);
                    break;
            }
        }

        private void positionFit(Bitmap target, Bitmap src) {
            Canvas canvas = new Canvas(target);
            helperMatrix.reset();
            canvas.drawColor(Color.BLACK); //Fill needs to paint over since it may not cover the whole screen
            RectF targetRect = new RectF(0, 0, target.getWidth(), target.getHeight());
            RectF sourceRect = new RectF(0, 0, src.getWidth(), src.getHeight());
            if (helperMatrix.setRectToRect(sourceRect, targetRect, Matrix.ScaleToFit.CENTER)) {
                canvas.drawBitmap(src, helperMatrix, ANTI_ALIAS);
            }
        }

        private void positionFill(Bitmap target, Bitmap src) {
            Canvas canvas = new Canvas(target);
            helperMatrix.reset();
            float srcHeight = src.getHeight();
            float srcWidth = src.getWidth();
            float destHeight = target.getHeight();
            float destWidth = target.getWidth();
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
            helperMatrix.postTranslate(offsetX, offsetY);
            helperMatrix.preScale(scale, scale);
            canvas.drawBitmap(src, helperMatrix, ANTI_ALIAS);
        }

        private void positionStretch(Bitmap target, Bitmap src) {
            Canvas canvas = new Canvas(target);
            RectF targetRect = new RectF(0, 0, target.getWidth(), target.getHeight());
            canvas.drawBitmap(src, null, targetRect, ANTI_ALIAS);
        }

        /**
         * Applies a gaussian blur using Renderscript
         * @param original      bitmap to blur
         * @param destination   bitmap to receive blurred image
         */
        private void applyBlur(Bitmap original, Bitmap destination) {
            Allocation tmpIn = Allocation.createFromBitmap(script, original);
            Allocation tmpOut = Allocation.createFromBitmap(script, destination);
            blurIntrinsic.setInput(tmpIn);
            blurIntrinsic.forEach(tmpOut);
            tmpOut.copyTo(destination);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            // Recalculate preferences
            if (key.equals(getString(R.string.key_wallpaper_home_double_tap_enabled))) {
                doubleTap = doubleTapEnabled(prefs);
            } else if (key.equals(getString(R.string.key_wallpaper_lock_blur))) {
                blurLock = blurLockEnabled(prefs);
            } else if (key.equals(getString(R.string.key_wallpaper_lock_use_home))) {
                lockUseHome = lockUseHomeEnabled(prefs);
            } else if (key.equals(getString(R.string.key_wallpaper_lock_album)) ||
                    key.equals(getString(R.string.key_wallpaper_home_album))) {
                loadCyclers(prefs);
                requestDraw();
            } else if (key.equals(getString(R.string.key_position))) {
                position = getPositionType(prefs);
                requestDraw();
            } else if (key.equals(getString(R.string.key_random_order_enabled))) {
                randomOrder = getRandomOrderEnabled(prefs);
            } else if (key.equals(getString(R.string.key_wallpaper_interval_enabled))) {
                changeOnInterval = getChangeOnIntervalEnabled(prefs);
                //requestCycle();
            } else if (key.equals(getString(R.string.key_transition))) {
                cycleAnimator.cancelCycle();
                updateTransitionType(prefs);
            }
        }
    }

    private class PhoneUnlockedReceiver extends BroadcastReceiver {

        private WPMEngine engine;
        private boolean locked = false;

        public PhoneUnlockedReceiver(WPMEngine engine) {
            this.engine = engine;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    if (!currentlyLocked()) {
                        if (locked) {
                            locked = false;
                            engine.notifyUnlocked();
                        }
                    }
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    if (currentlyLocked()) {
                        if (!locked) {
                            locked = true;
                            engine.notifyLocked();
                        }
                    }
                }
            }
        }

        @SuppressWarnings("deprecation")
        private boolean currentlyLocked() {
            KeyguardManager keyguardManager = (KeyguardManager) WPMService.this.getSystemService(Context.KEYGUARD_SERVICE);
            assert (keyguardManager != null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                return keyguardManager.isDeviceLocked();
            } else {
                //Alternative for pre api 22
                return keyguardManager.inKeyguardRestrictedInputMode();
            }
        }
    }

}
