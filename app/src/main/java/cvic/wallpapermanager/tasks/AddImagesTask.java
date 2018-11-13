package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class AddImagesTask extends AsyncTask<String, Integer, Void> {

    private static final String TAG = "cvic.wpm.ait";

    private TaskListener listener;
    private File destination;
    private int filesToAdd;

    public AddImagesTask (TaskListener listener, File destinationDir) {
        this.listener = listener;
        destination = destinationDir;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onTaskStarted();
    }

    @Override
    protected Void doInBackground(String... paths) {
        filesToAdd = paths.length;
        int addedSoFar = 0;
        for (String path : paths) {
            publishProgress(addedSoFar++);
            File file = new File(path);
            // Copy the file contents to destination dir
            if (file.exists()) {
                addFile(file);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        listener.onProgress(String.format(Locale.getDefault(), "%d / %d", values[0], filesToAdd));
    }

    private void addFile(File file) {
        File dest = getDestination(file);
        if (dest == null) {
            return;
        }
        try {
            InputStream in = new FileInputStream(file);
            OutputStream out = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            Log.i(TAG, "File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "IO Exception!");
            e.printStackTrace();
        }
    }

    private File getDestination(File file) {
        int numFiles = destination.listFiles().length;  //lists the number of files already in the folder
        File ret = new File(destination, file.getName());
        if (!ret.exists()) {
            return ret;
        }
        for (int i = 0; i < numFiles; i++) {
            ret = new File(destination, String.valueOf(i) + file.getName());
            if (!ret.exists()) {
                return ret;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        listener.onTaskComplete();
    }

    public interface TaskListener {

        void onTaskStarted();
        void onProgress(String textToShow);
        void onTaskComplete();

    }

}
