package cvic.wallpapermanager.model.cycler;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cvic.wallpapermanager.JSON;
import cvic.wallpapermanager.utils.FilterUtils;

public class CyclerFactory {

    public static Cycler create(Context ctx, String prefValue) {
        try {
            JSONObject object = new JSONObject(prefValue);
            String type = object.getString(JSON.KEY_TYPE);
            switch (type) {
                case JSON.VALUE_FOLDER:
                    return fromFolder(ctx, object);
                case JSON.VALUE_TAG:
                    return fromTag(object);
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
            pathList.addAll(Arrays.asList(folder.list(FilterUtils.get(FilterUtils.IMAGE))));
        }
        String[] paths = new String[pathList.size()];
        return new ImageCycler(pathList.toArray(paths));
    }

    private static Cycler fromTag(JSONObject object) {
        return null; // TODO
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
        return new ImageCycler(paths);
    }

}
