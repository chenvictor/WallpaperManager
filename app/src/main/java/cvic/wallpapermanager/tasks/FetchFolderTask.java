package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;

import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.model.albumable.FolderManager;
import cvic.wallpapermanager.utils.FilterUtils;

public class FetchFolderTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "cvic.wpm.fft";

    private final TaskListener listener;
    private final File root;

    public FetchFolderTask (@NonNull TaskListener listener, File root) {
        this.listener = listener;
        this.root = root;
    }

    @Override
    protected Void doInBackground(Void... unused) {
        FolderManager manager = FolderManager.getInstance();
        for (File dir : root.listFiles(FilterUtils.get(FilterUtils.FOLDER_EMPTY))) {
            Folder folder = new Folder(dir);
            manager.addFolder(folder);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        listener.onFoldersFetched();
    }

    public interface TaskListener {

        void onFoldersFetched();

    }

}
