package cvic.wallpapermanager.tasks;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;

import cvic.wallpapermanager.utils.DisplayUtils;

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
            return DisplayUtils.decodeBitmap(mFile.getAbsolutePath(), width, height);
        } catch (Exception e) {
            e.printStackTrace();
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onTaskComplete(mFile, requestId, bitmap);
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (mListener != null) {
            mListener.onError(requestId);
        }
    }

    public interface TaskListener {

        void onTaskComplete(File file, int requestId, Bitmap bitmap);

        void onError(int requestId);

    }

}
