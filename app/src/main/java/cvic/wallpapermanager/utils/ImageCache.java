package cvic.wallpapermanager.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cvic.wallpapermanager.model.ImageFile;
import cvic.wallpapermanager.tasks.BitmapWorkerTask;

public class ImageCache implements BitmapWorkerTask.TaskListener{

    private static final String TAG = "cvic.wpm.img_cache";
    private static final int DEFAULT_CACHE_SIZE = 16;

    private static final ImageCache mainInstance = new ImageCache(DEFAULT_CACHE_SIZE);

    private BitmapWrapper mPlaceholder;
    private LruCache<File, BitmapWrapper> cache;
    private Set<CacheListener> listeners;
    private Map<File, BitmapWorkerTask> requests;

    public static ImageCache getMainInstance() {
        return mainInstance;
    }

    public synchronized void addListener(CacheListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(CacheListener listener) {
        listeners.remove(listener);
    }

    public ImageCache(int cacheSize) {
        listeners = new HashSet<>();
        mPlaceholder = new BitmapWrapper(Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)) {
            @Override
            public void incRef() {}
            @Override
            public void decRef() {}
        };
        cache = new BitmapCache(cacheSize);
        requests = new HashMap<>();
    }

    public BitmapWrapper requestImage (ImageFile file, int requestId, int width, int height) {
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
    public BitmapWrapper requestImage (File file, int requestId, int width, int height) {

        if (file == null) {
            return mPlaceholder;
        }
        //Log.i(TAG, "Image: " + file.toString() + " requested.");
        BitmapWrapper cached = cache.get(file);
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
            BitmapWrapper wrapped = new BitmapWrapper(bitmap);
            cache.put(file, wrapped);
            synchronized (this) {
                for (CacheListener listener : listeners) {
                    listener.onBitmapAvailable(file, requestId, wrapped);
                }
            }
            requests.remove(file);
        }
    }

    @Override
    public void onError(int requestId) {
        Log.i(TAG, "Error loading bitmap");
    }

    private class BitmapCache extends LruCache<File, BitmapWrapper> {

        private BitmapCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, @NonNull File key, @NonNull BitmapWrapper oldValue, @Nullable BitmapWrapper newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);
            oldValue.decRef();
        }
    }

    public interface CacheListener {

        void onBitmapAvailable(File file, int requestId, BitmapWrapper bitmap);

        /**
         * Hashcode and equals must be implemented to add and remove listeners
         */
        boolean equals(Object o);
        int hashCode();

    }
}
