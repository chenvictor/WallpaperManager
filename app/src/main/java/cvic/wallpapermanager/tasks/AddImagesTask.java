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

public class AddImagesTask extends AsyncTask<File, Void, Void> {

    private static final String TAG = "cvic.wpm.ait";

    private TaskListener listener;
    private File destination;

    public AddImagesTask (TaskListener listener, File destinationDir) {
        this.listener = listener;
        destination = destinationDir;
    }

    @Override
    protected Void doInBackground(File... files) {
        for (File file : files) {
            // Copy the file contents to destination dir
            if (file.exists()) {
                addFile(file);
            }
        }
        return null;
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

        void onTaskComplete();

    }

}
