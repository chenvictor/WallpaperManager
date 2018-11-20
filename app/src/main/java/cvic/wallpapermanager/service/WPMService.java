package cvic.wallpapermanager.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.animation.CircleExpandAnimator;
import cvic.wallpapermanager.model.animation.DefaultAnimator;
import cvic.wallpapermanager.model.animation.FadeAnimator;
import cvic.wallpapermanager.model.animation.PageAnimator;
import cvic.wallpapermanager.model.animation.TransitionAnimator;

public class WPMService extends WallpaperService {

    private static final String TAG = "cvic.wpm.service";

    private static final int TRANSITION_FADE = 0;
    private static final int TRANSITION_PAGE = 1;
    private static final int TRANSITION_CIRCLE = 2;

    @Override
    public Engine onCreateEngine() {
        return new WPMEngine();
    }

    private class WPMEngine extends WallpaperService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener, BitmapHandler.BitmapReceiver {

        private final Paint ANTI_ALIAS = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private boolean dimensInit = false;

        private BitmapHandler bitmapHandler;

        private TransitionAnimator transitionAnimator;

        private Context ctx = WPMService.this;
        private GestureDetector gestureDetector;
        private PhoneUnlockedReceiver phoneUnlockedReceiver;
        private LockNotificationReceiver lockNotificationReceiver;
        private LockNotification lockNotification;
        /**
         * Preferences
         */
        private boolean doubleTap;
        private boolean lockNotif;
        private float tapX;
        private float tapY;


        private PreviewHandler previewHandler;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            if (isPreview()) {
                previewHandler = new PreviewHandler(ctx);
            } else {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WPMService.this);
                gestureDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        doubleTapped(e.getX(), e.getY());
                        return true;
                    }
                });
                phoneUnlockedReceiver = new PhoneUnlockedReceiver(this);
                lockNotificationReceiver = new LockNotificationReceiver(this);
                doubleTap = doubleTapEnabled(prefs);
                lockNotif = lockNotifEnabled(prefs);
                updateTransitionType(prefs);
                lockNotification = new LockNotification(ctx);
                prefs.registerOnSharedPreferenceChangeListener(this);
                IntentFilter lockUnlock = new IntentFilter();
                lockUnlock.addAction(Intent.ACTION_USER_PRESENT);
                lockUnlock.addAction(Intent.ACTION_SCREEN_OFF);
                registerReceiver(phoneUnlockedReceiver, lockUnlock);
                registerReceiver(lockNotificationReceiver, new IntentFilter(LockNotification.ACTION_CUSTOM));
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (!isPreview()) {
                if (!visible) {
                    if (transitionAnimator.isAnimating()) {
                        transitionAnimator.stopCycle();  //cancel any ongoing animation if wallpaper not visible
                    }
                } else {
                    phoneUnlockedReceiver.notifyVisible();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (isPreview()) {
                previewHandler.destroy();
            } else {
                PreferenceManager.getDefaultSharedPreferences(ctx).unregisterOnSharedPreferenceChangeListener(this);
                unregisterReceiver(phoneUnlockedReceiver);
                unregisterReceiver(lockNotificationReceiver);
                bitmapHandler.destroy();
                lockNotification.destroy(ctx);
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            if (!isPreview()) {
                bitmapHandler = new BitmapHandler(ctx, this);
                requestDraw();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            if (isPreview()) {
                previewHandler.draw(holder, width, height);
            } else {
                if (!dimensInit) {
                    Log.i(TAG, "Initializing dimens: " + width + "x" + height);
                    tapX = width / 2;
                    tapY = width / 2;
                    bitmapHandler.setDecodeDimens(width, height);
                    dimensInit = true;
                }
                if (transitionAnimator.isAnimating()) {
                    transitionAnimator.stopCycle();
                }
                bitmapHandler.notifyScreenSizeChanged(width, height);
                requestDraw();
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (!isPreview()) {
                if (doubleTap) {
                    gestureDetector.onTouchEvent(event);
                }
            }
        }

        private void notifyLocked() {
            Log.i(TAG, "phone locked!");
            if (transitionAnimator.isAnimating()) {
                transitionAnimator.stopCycle();
            }
            bitmapHandler.notifyLocked();
            if (lockNotif) {
                lockNotification.show();
            }
        }

        private void notifyUnlocked() {
            Log.i(TAG, "phone unlocked!");
            if (transitionAnimator.isAnimating()) {
                transitionAnimator.stopCycle();
            }
            bitmapHandler.notifyUnlocked();
            lockNotification.hide();
        }

        private void doubleTapped(float mouseX, float mouseY) {
            Log.i(TAG, "double tapped");
            if (!transitionAnimator.isAnimating()) {
                tapX = mouseX;
                tapY = mouseY;
                bitmapHandler.cycleHome();
            }
        }

        private void notifyLockNotifTapped() {
            Log.i(TAG, "lock notif tapped");
            if (!transitionAnimator.isAnimating()) {
                bitmapHandler.cycleLock();
            }
        }

        @Override
        public void requestCycle(final Bitmap from, final Bitmap to) {
            transitionAnimator.stopIfAnimating();
            if (isVisible()) {
                transitionAnimator.requestCycle(getSurfaceHolder(), from, to, new TransitionAnimator.AnimatorListener() {
                    @Override
                    public void runOnFinish() {
                        from.recycle();
                        tapX = getSurfaceHolder().getSurfaceFrame().width() / 2;
                        tapY = getSurfaceHolder().getSurfaceFrame().height() / 2;
                        drawImage(to);
                    }
                }, tapX, tapY);
            } else {
                from.recycle();
                tapX = getSurfaceHolder().getSurfaceFrame().width() / 2;
                tapY = getSurfaceHolder().getSurfaceFrame().height() / 2;
                drawImage(to);
            }
        }

        private boolean doubleTapEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_wallpaper_home_double_tap_enabled), false);
        }

        private boolean lockNotifEnabled(SharedPreferences prefs) {
            return prefs.getBoolean(getString(R.string.key_wallpaper_lock_notification), false);
        }

        private void updateTransitionType(SharedPreferences prefs) {
            switch (prefs.getInt(getString(R.string.key_transition), TRANSITION_FADE)) {
                case TRANSITION_FADE:
                    transitionAnimator = new FadeAnimator();
                    break;
                case TRANSITION_PAGE:
                    transitionAnimator = new PageAnimator();
                    break;
                case TRANSITION_CIRCLE:
                    transitionAnimator = new CircleExpandAnimator();
                    break;
                default:
                    transitionAnimator = new DefaultAnimator();
            }
        }

        public void requestDraw() {
            drawImage(bitmapHandler.getBitmap());
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
                    canvas.drawBitmap(bitmap, 0, 0, ANTI_ALIAS);
                } else {
                    Log.i(TAG, "canvas was null");
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (!isPreview()) {
                // Recalculate preferences

                if (key.equals(getString(R.string.key_wallpaper_home_double_tap_enabled))) {
                    doubleTap = doubleTapEnabled(prefs);
                } else if (key.equals(getString(R.string.key_wallpaper_lock_notification))) {
                    lockNotif = lockNotifEnabled(prefs);
                } else if (key.equals(getString(R.string.key_transition))) {
                    transitionAnimator.stopCycle();
                    updateTransitionType(prefs);
                } else {
                    //Delegate to BitmapHandler
                    bitmapHandler.notifyPreferenceChanged(prefs, key);
                }
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

        void notifyVisible() {
            //Fallback for if the phone locks after idling and doesn't trigger properly
            if (currentlyLocked() && !locked) {
                locked = true;
                engine.notifyLocked();
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

    private class LockNotificationReceiver extends BroadcastReceiver {

        private WPMEngine engine;

        public LockNotificationReceiver(WPMEngine engine) {
            this.engine = engine;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(LockNotification.EXTRA)) {
                engine.notifyLockNotifTapped();
            }
        }
    }

}
