package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cvic.wallpapermanager.model.albumable.Albumable;
import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.utils.FilterUtils;

public class FetchFolderTask extends AsyncTask<String, Void, List<Albumable>> {

    private TaskListener mListener;

    public FetchFolderTask (@NonNull TaskListener listener) {
        mListener = listener;
    }

    @Override
    protected List<Albumable> doInBackground(String... strings) {
        if (strings.length != 1) {
            cancel(true);
            return null;
        }
        File root = new File(strings[0]);
        List<Albumable> list = new ArrayList<>();
//        if (FilterUtils.containsImages(root)) {
//            list.add(new Folder(root, true));
//        }
        for (File dir : root.listFiles(FilterUtils.get(FilterUtils.FOLDER))) {
            list.add(new Folder(dir));
        }
        return list;
    }

    @Override
    protected void onPostExecute(List<Albumable> folders) {
        mListener.onFoldersFetched(folders);
    }

    public interface TaskListener {

        void onFoldersFetched(List<Albumable> folders);

    }

}
