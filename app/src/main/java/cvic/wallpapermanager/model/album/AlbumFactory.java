package cvic.wallpapermanager.model.album;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cvic.wallpapermanager.JSON;

public class AlbumFactory {

    private final static String TAG = "cvic.wpm.af";

    public static Album create(String prefValue) {
        try {
            JSONObject object = new JSONObject(prefValue);
            String type = object.getString(JSON.KEY_TYPE);
            switch (type) {
                case JSON.VALUE_FOLDER:
                    return fromFolder(object);
                case JSON.VALUE_TAG:
                    return fromTag(object);
                case JSON.VALUE_IMAGE:
                    return fromImage(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Could not build album, using NullAlbum");
        return new NullAlbum();
    }

    private static Album fromFolder(JSONObject object) throws JSONException {
        JSONArray array = object.getJSONArray(JSON.KEY_FOLDERS);
        if (array.length() == 0) {
            throw new IllegalArgumentException("No folders in the album!");
        }
        String[] folders = getStrings(array);
        return new FolderAlbum(folders);
    }

    private static Album fromTag(JSONObject object) throws JSONException {
        JSONArray include = object.getJSONArray(JSON.KEY_TAG_INCLUDE);
        if (include.length() == 0) {
            throw new IllegalArgumentException("No included tags in the album!");
        }
        JSONArray exclude = object.getJSONArray(JSON.KEY_TAG_EXCLUDE);
        String[] includeArray = getStrings(include);
        String[] excludeArray = getStrings(exclude);
        return new TagAlbum(includeArray, excludeArray);
    }

    private static Album fromImage(JSONObject object) throws JSONException {
        JSONArray array = object.getJSONArray(JSON.KEY_IMAGES);
        if (array.length() == 0) {
            throw new IllegalArgumentException("No images in the album!");
        }
        String[] paths = getStrings(array);
        return new ImageAlbum(paths);
    }

    @NonNull
    private static String[] getStrings(JSONArray include) throws JSONException {
        String[] includeArray = new String[include.length()];
        for (int i = 0; i < include.length(); i++) {
            includeArray[i] = include.getString(i);
        }
        return includeArray;
    }

}
