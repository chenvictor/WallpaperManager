package cvic.wallpapermanager.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cvic.wallpapermanager.tasks.BitmapWorkerTask;

public class ImageCache implements BitmapWorkerTask.TaskListener{

    private static final String TAG = "cvic.wpm.img_cache";

    private Bitmap mPlaceholder;
    private LruCache<Integer, Bitmap> cache;
    private CacheListener mListener;
    private Map<Integer, BitmapWorkerTask> requests;

    @SuppressLint("UseSparseArrays")
    public ImageCache(CacheListener listener) {
        mPlaceholder = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        mListener = listener;
        cache = new LruCache<>(16);
        requests = new HashMap<>();
    }

    /**
     * Request an image from the cache
     * @param file          File corresponding the the image
     * @param requestId     a requestId, used to identify the request in the callback
     * @param width         requested width
     * @param height        requested height
     * @return      the cached image, if available.
     *              otherwise, a BitmapWorkerTask will be created to fetch the image,
     *              and a placeholder image is returned.
     *              If the file is null, the placeholder image will be returned.
     */
    public Bitmap requestImage (File file, int requestId, int width, int height) {
        if (file == null) {
            cache.remove(requestId);
            return mPlaceholder;
        }
        Bitmap cached = cache.get(requestId);
        if (cached != null) {
            return cached;
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask(file, requestId, this, width, height);
            requests.put(requestId, task);
            task.execute();
            return mPlaceholder;
        }
    }

    /**
     * Cancels the BitmapWorkerTask associated with the requestId if it is still running
     * @param requestId     requestId to cancel
     */
    public void cancelRequest (int requestId) {
        if (requests.containsKey(requestId)) {
            try {
                BitmapWorkerTask task = requests.get(requestId);
                if (task != null) {
                    task.cancel(true);
                }
            } catch (Exception ignored) {
            } finally {
                requests.remove(requestId);
            }
        }
    }

    public void flush () {
        for (BitmapWorkerTask task : requests.values()) {
            if (task != null) {
                task.cancel(true);
            }
        }
        requests.clear();
        cache.evictAll();
    }

    @Override
    public void onTaskComplete(int requestId, Bitmap bitmap) {
        if (bitmap != null) {
            cache.put(requestId, bitmap);
            if (mListener != null) {
                mListener.onBitmapAvailable(requestId, bitmap);
            }
            requests.remove(requestId);
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
