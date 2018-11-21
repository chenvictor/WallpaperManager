package cvic.wallpapermanager.model.album;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cvic.wallpapermanager.JSON;

public class ImageAlbum extends Album {

    private List<File> paths;

    public ImageAlbum() {
        paths = new ArrayList<>();
    }
    public ImageAlbum(String[] paths) {
        this();
        for (String s : paths) {
            this.paths.add(new File(s));
        }
    }

    @Override
    public int size() {
        return paths.size();
    }

    @Override
    public File getImage(int index) {
        return paths.get(index);
    }

    @Override
    public String getName() {
        return String.format(Locale.getDefault(), "Image: (%d)", size());
    }

    @Override
    public JSONObject jsonify() {
        JSONObject object = new JSONObject();
        try {
            object.put(JSON.KEY_TYPE, JSON.VALUE_IMAGE);
            JSONArray array = new JSONArray();
            for (int i = 0; i < paths.size(); i++) {
                array.put(paths.get(i).getAbsolutePath());
            }
            object.put(JSON.KEY_IMAGES, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
