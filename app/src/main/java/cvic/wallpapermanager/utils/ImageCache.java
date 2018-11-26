package cvic.wallpapermanager.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cvic.wallpapermanager.model.ImageFile;
import cvic.wallpapermanager.tasks.BitmapWorkerTask;

public class ImageCache implements BitmapWorkerTask.TaskListener{

    private static final String TAG = "cvic.wpm.img_cache";
    private static final int DEFAULT_CACHE_SIZE = 16;

    private Bitmap mPlaceholder;
    private LruCache<File, Bitmap> cache;
    private CacheListener mListener;
    private Map<File, BitmapWorkerTask> requests;

    @SuppressLint("UseSparseArrays")
    public ImageCache(CacheListener listener, int cacheSize) {
        mPlaceholder = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        mListener = listener;
        cache = new LruCache<>(cacheSize);
        requests = new HashMap<>();
    }

    @SuppressLint("UseSparseArrays")
    public ImageCache(CacheListener listener) {
        this(listener, DEFAULT_CACHE_SIZE);
    }

    public Bitmap requestImage (ImageFile file, int requestId, int width, int height) {
        if (file == null) {
            return mPlaceholder;
        }
        return requestImage(file.getFile(), requestId, width, height);
    }

    /**
     * Request an image create the cache
     * @param file          ImageFile corresponding the the image
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
            return mPlaceholder;
        }
        //Log.i(TAG, "Image: " + file.toString() + " requested.");
        Bitmap cached = cache.get(file);
        if (cached != null) {
            return cached;
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask(file, requestId, this, width, height);
            requests.put(file, task);
            task.execute();
            return mPlaceholder;
        }
    }

    /**
     * Cancels the BitmapWorkerTask associated with the requestId if it is still running
     */
    public void cancelRequest (File file) {
        if (requests.containsKey(file)) {
            try {
                BitmapWorkerTask task = requests.get(file);
                if (task != null) {
                    task.cancel(true);
                }
            } catch (Exception ignored) {
            } finally {
                requests.remove(file);
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
    public void onTaskComplete(File file, int requestId, Bitmap bitmap) {
        if (bitmap != null) {
            cache.put(file, bitmap);
            if (mListener != null) {
                mListener.onBitmapAvailable(file, requestId, bitmap);
            }
            requests.remove(file);
        }
    }

    @Override
    public void onError(int requestId) {
        Log.i(TAG, "Error loading bitmap");
    }

    public interface CacheListener {

        void onBitmapAvailable(File file, int requestId, Bitmap bitmap);

    }
}
