package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

import cvic.wallpapermanager.JSON;
import cvic.wallpapermanager.model.albumable.Tag;
import cvic.wallpapermanager.model.albumable.TagManager;
import cvic.wallpapermanager.utils.JSONUtils;

public class FetchTagsTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "cvic.wpm.ftt";

    private final File root;
    private final TaskListener listener;

    public FetchTagsTask (TaskListener listener, File root) {
        this.listener = listener;
        this.root = root;
    }

    @Override
    protected Void doInBackground(Void... unused) {
        TagManager manager = TagManager.getInstance();
        try {
            JSONObject tagData = JSONUtils.getJSON(new File(root, JSON.FILE_TAGS).getAbsolutePath());
            JSONArray array = tagData.getJSONArray(JSON.KEY_TAGS);
            for (int i = 0; i < array.length(); i++) {
                manager.addTag(new Tag(array.getString(i)));
            }
            Log.i(TAG, array.length() + " tags loaded.");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "tags.json file not found. Starting with no user tags.");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.i(TAG, "json data corrupt. Starting with no user tags.");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        listener.onTagsFetched();
    }

    public interface TaskListener {

        void onTagsFetched();

    }
}
