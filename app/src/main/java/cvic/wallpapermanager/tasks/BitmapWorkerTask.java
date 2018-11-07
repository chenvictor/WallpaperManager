package cvic.wallpapermanager.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;

public class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {

    private final TaskListener mListener;
    private final int width;
    private final int height;
    private final int requestId;
    private final File mFile;

    public BitmapWorkerTask(File file, int requestId, TaskListener listener, int width, int height) {
        mListener = listener;
        this.width = width;
        this.height = height;
        mFile = file;
        this.requestId = requestId;
    }

    @Override
    protected Bitmap doInBackground(Void... unused) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mFile.getAbsolutePath(), options);  //decode just the size of the image
            options.inSampleSize = calculateInSampleSize(options, width, height);
            options.outWidth = width;
            options.outHeight = height;
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(mFile.getAbsolutePath(), options);
        } catch (Exception e) {
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onTaskComplete(requestId, bitmap);
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onError(requestId);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public interface TaskListener {

        void onTaskComplete(int requestId, Bitmap bitmap);

        void onError(int requestId);

    }

}
