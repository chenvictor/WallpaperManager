package cvic.wallpapermanager.tasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.File;

public class BitmapWorkerTask extends AsyncTask<Void, Void, Bitmap> {

    private final TaskListener mListener;
    private final BitmapFactory.Options mOptions;
    private final int requestId;
    private final File mFile;

    public BitmapWorkerTask(File file, int requestId, TaskListener listener, BitmapFactory.Options options) {
        mListener = listener;
        mOptions = options;
        mFile = file;
        this.requestId = requestId;
    }

    @Override
    protected Bitmap doInBackground(Void... unused) {
        try {
            return BitmapFactory.decodeFile(mFile.getAbsolutePath(), mOptions);
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

    public interface TaskListener {

        void onTaskComplete(int requestId, Bitmap bitmap);

        void onError(int requestId);

    }

}
