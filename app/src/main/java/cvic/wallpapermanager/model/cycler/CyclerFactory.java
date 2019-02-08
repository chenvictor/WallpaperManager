package cvic.wallpapermanager.model.cycler;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cvic.wallpapermanager.JSON;
import cvic.wallpapermanager.utils.FilterUtils;
import cvic.wallpapermanager.utils.JSONUtils;

public class CyclerFactory {

    private static final String TAG = "cvic.wpm.cf";

    public static Cycler create(Context ctx, String prefValue) {
        try {
            JSONObject object = new JSONObject(prefValue);
            String type = object.getString(JSON.KEY_TYPE);
            switch (type) {
                case JSON.VALUE_FOLDER:
                    return fromFolder(ctx, object);
                case JSON.VALUE_TAG:
                    return fromTag(ctx, object);
                case JSON.VALUE_IMAGE:
                    return fromImage(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new DefaultCycler();
    }

    private static Cycler fromFolder(Context ctx, JSONObject object) throws JSONException {
        JSONArray array = object.getJSONArray(JSON.KEY_FOLDERS);
        if (array.length() == 0) {
            return new DefaultCycler(); //no folders, use default
        }
        File base = ctx.getExternalFilesDir(null);
        List<String> pathList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            File folder = new File(base, array.getString(i));
            for (File file : folder.listFiles(FilterUtils.get(FilterUtils.IMAGE))) {
                pathList.add(file.getAbsolutePath());
            }
        }
        String[] paths = new String[pathList.size()];
        return new Cycler(pathList.toArray(paths));
    }

    private static Cycler fromTag(Context ctx, JSONObject object) throws JSONException {
        Log.i(TAG, "Building from tag");
        JSONArray include = object.getJSONArray(JSON.KEY_TAG_INCLUDE);
        if (include.length() == 0) {
            return new DefaultCycler(); //no include tags, use default
        }
        Set<String> includeSet = buildSet(include);
        Set<String> excludeSet = buildSet(object.getJSONArray(JSON.KEY_TAG_EXCLUDE));

        List<String> pathList = new ArrayList<>();
        // Grab all the folder tags
        File base = ctx.getExternalFilesDir(null);
        for (File folder : base.listFiles(FilterUtils.get(FilterUtils.FOLDER))) {
            File tagData = new File(folder, JSON.FILE_TAGS);
            Log.i(TAG, "Scanning tagData: " + tagData.getAbsolutePath());
            try {
                JSONObject data = JSONUtils.getJSON(tagData);
                JSONArray array = data.getJSONArray(JSON.KEY_IMAGES);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject image = array.getJSONObject(i);
                    JSONArray tags = image.getJSONArray(JSON.KEY_IMG_TAGS);
                    if (valid(tags, includeSet, excludeSet)) {
                        Log.i(TAG, "Adding file: " + image.getString(JSON.KEY_IMG_NAME));
                        pathList.add(new File(folder, image.getString(JSON.KEY_IMG_NAME)).getAbsolutePath());
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (pathList.size() == 0) {
            return new DefaultCycler(); //no images, use default
        }
        String[] paths = new String[pathList.size()];
        return new Cycler(pathList.toArray(paths));
    }

    private static Cycler fromImage(JSONObject object) throws JSONException {
        JSONArray array = object.getJSONArray(JSON.KEY_IMAGES);
        if (array.length() == 0) {
            return new DefaultCycler(); //no images, use default
        }
        String[] paths = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            paths[i] = array.getString(i);
        }
        return new Cycler(paths);
    }

    private static Set<String> buildSet(JSONArray include) throws JSONException {
        Set<String> includeSet = new HashSet<>();
        for (int i = 0; i < include.length(); i++) {
            includeSet.add(include.getString(i));
        }
        return includeSet;
    }

    /**
     * Scans a JSONArray for accepted and rejected tags
     * @param allTags           JSONArray to scan, assume no duplicates exist
     * @param required          tags to accept
     * @param blacklist         tags to reject
     * @return  true if at all required tags were found, and no blacklisted tags were found
     */
    private static boolean valid(JSONArray allTags, Set<String> required, Set<String> blacklist) throws JSONException {
        int needToFind = required.size(); //number of tags needed
        Log.i(TAG, "Need to find: " + needToFind);
        for (int i = allTags.length() - 1; i >= 0; i--) {
            if (needToFind > i + 1) {
                // Not enough tags to possibly find all tags required
                return false;
            }
            String tag = allTags.getString(i);
            Log.i(TAG, "Looking for tag: " + tag);
            if (blacklist.contains(tag)) {
                Log.i(TAG, "Blacklist hit: " + tag);
                //Contains a blacklist tag
                return false;
            }
            if (required.contains(tag)) {
                Log.i(TAG, "Matched: " + tag);
                if(--needToFind == 0) {
                    // All were found
                    return true;
                }

            }
        }
        return false;
    }

}
