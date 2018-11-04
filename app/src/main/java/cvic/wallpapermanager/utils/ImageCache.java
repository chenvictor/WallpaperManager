package cvic.wallpapermanager.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;

import cvic.wallpapermanager.tasks.BitmapWorkerTask;

public class ImageCache implements BitmapWorkerTask.TaskListener{

    private static final String TAG = "cvic.wpm.imgcache";

    private Bitmap mPlaceholder;
    private LruCache<Integer, Bitmap> cache;
    private CacheListener mListener;

    public ImageCache(CacheListener listener) {
        mPlaceholder = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        mListener = listener;
        cache = new LruCache<Integer, Bitmap>((int) Runtime.getRuntime().maxMemory() / (1024 * 8)) {
            @Override
            protected int sizeOf(@NonNull Integer key, @NonNull Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    /**
     * Request an image from the cache
     * @param file          File corresponding the the image
     * @param requestId     a requestId, used to identify the request in the callback
     * @param options       bitmap decoding options
     * @return      the cached image, if available.
     *              otherwise, a BitmapWorkerTask will be created to fetch the image,
     *              and a placeholder image is returned.
     *              If the file is null, the placeholder image will be returned.
     */
    public Bitmap requestImage (File file, int requestId, BitmapFactory.Options options) {
        Bitmap cached = cache.get(requestId);
        if (cached != null) {
            return cached;
        } else {
            if (file != null) {
                BitmapWorkerTask task = new BitmapWorkerTask(file, requestId, this, options);
                task.execute();
            }
            return mPlaceholder;
        }
    }

    public void flush () {
        cache.evictAll();
    }

    @Override
    public void onTaskComplete(int requestId, Bitmap bitmap) {
        if (bitmap != null) {
            cache.put(requestId, bitmap);
            if (mListener != null) {
                mListener.onBitmapAvailable(requestId, bitmap);
            }
        }
    }

    @Override
    public void onError(int requestId) {
        Log.i(TAG, "Error loading bitmap");
    }

    public interface CacheListener {

        void onBitmapAvailable(int requestId, Bitmap bitmap);

    }
}
