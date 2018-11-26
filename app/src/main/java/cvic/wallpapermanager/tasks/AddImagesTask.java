package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;

import java.io.File;

import cvic.wallpapermanager.model.ImageFile;
import cvic.wallpapermanager.model.albumable.Folder;

public class AddImagesTask extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = "cvic.wpm.ait";

    private TaskListener listener;
    private Folder destination;
    private String[] paths;

    public AddImagesTask (TaskListener listener, Folder destination, String... paths) {
        this.listener = listener;
        this.destination = destination;
        this.paths = paths;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onTaskStarted(paths.length);
    }

    @Override
    protected Void doInBackground(Void... unused) {
        for (String path : paths) {
            File file = new File(path);
            // Copy the file contents to destination dir
            if (file.exists()) {
                addFile(file);
            }
            publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        listener.onProgress();
    }

    private void addFile(File file) {
        ImageFile image = new ImageFile(null, file);
        image.copyTo(destination);
        image.addToManager();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listener.onTaskComplete();
    }

    public interface TaskListener {

        void onTaskStarted(int maxProgress);
        void onProgress();
        void onTaskComplete();

    }

}
