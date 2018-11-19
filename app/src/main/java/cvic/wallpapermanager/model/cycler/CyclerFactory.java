package cvic.wallpapermanager.model.cycler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CyclerFactory {

    public static Cycler create(String prefValue) {
        try {
            JSONObject object = new JSONObject(prefValue);
            String type = object.getString("type");
            switch (type) {
                case "FOLDER":
                    return fromFolder(object);
                case "TAG":
                    return fromTag(object);
                case "IMAGE":
                    return fromImage(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new DefaultCycler();
    }

    private static Cycler fromFolder(JSONObject object) {
        return null;
    }

    private static Cycler fromTag(JSONObject object) {
        return null;
    }

    private static Cycler fromImage(JSONObject object) throws JSONException {
        JSONArray array = object.getJSONArray("images");
        if (array.length() == 0) {
            return new DefaultCycler(); //no images, use default
        }
        ImageCycler cycler = new ImageCycler();
        cycler.paths = new String[array.length()];
        for (int i = 0; i < array.length(); i++) {
            cycler.paths[i] = array.getString(i);
        }
        cycler.init();
        return cycler;
    }

}
