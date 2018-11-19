package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import cvic.wallpapermanager.model.album.Albumable;
import cvic.wallpapermanager.utils.JSONUtils;

class FetchTagsTask extends AsyncTask<String, Void, List<Albumable>> {

    private final TaskListener mListener;

    public FetchTagsTask (TaskListener listener) {
        mListener = listener;
    }

    @Override
    protected List<Albumable> doInBackground(String... dirPath) {
        if (dirPath.length != 1) {
            cancel(true);
            return null;
        }
        try {
            JSONObject tagData = JSONUtils.getJSON(dirPath[0] + File.separator + "tags.json");
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Albumable> tags) {
        super.onPostExecute(tags);
        mListener.onTagsFetched(tags);
    }

    interface TaskListener {

        void onTagsFetched(List<Albumable> tags);

    }
}
