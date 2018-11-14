package cvic.wallpapermanager.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import cvic.wallpapermanager.R;

public class WPMService extends WallpaperService {

    private static final String TAG = "cvic.wpm.service";

    @Override
    public Engine onCreateEngine() {
        return new WPMEngine();
    }

    private class WPMEngine extends WallpaperService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private Context ctx = WPMService.this;
        private GestureDetector gestureDetector;
        private PhoneUnlockedReceiver receiver;
        private boolean touchEnabled;

        private WPMEngine() {
            gestureDetector = new GestureDetector(ctx, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    doubleTapped();
                    return true;
                }
            });
            receiver = new PhoneUnlockedReceiver(this);
            touchEnabled = doubleTapEnabled();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            PreferenceManager.getDefaultSharedPreferences(ctx).registerOnSharedPreferenceChangeListener(this);
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
            draw(holder);
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            if (touchEnabled) {
                gestureDetector.onTouchEvent(event);
            }
        }

        private void notifyLocked() {
            Log.i(TAG, "phone locked!");
        }

        private void notifyUnlocked() {
            Log.i(TAG, "phone unlocked!");
        }

        private boolean doubleTapEnabled() {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WPMService.this);
            return prefs.getBoolean(getString(R.string.key_wallpaper_home_double_tap_enabled), false);
        }

        private void doubleTapped() {
            Toast.makeText(WPMService.this, "Double tapped", Toast.LENGTH_SHORT).show();
        }

        private void draw(SurfaceHolder holder) {
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.CYAN);
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.key_wallpaper_home_double_tap_enabled))) {
                Log.i(TAG, "PreferenceChange detected");
                touchEnabled = doubleTapEnabled();
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
