package cvic.wallpapermanager.model.album;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cvic.wallpapermanager.model.ImageFile;
import cvic.wallpapermanager.model.albumable.Tag;
import cvic.wallpapermanager.model.albumable.TagManager;

public class TagAlbum extends Album {

    private List<Tag> include;
    private List<Tag> exclude;
    private List<ImageFile> images;

    public TagAlbum(String[] include, String[] exclude) {
        super();
        TagManager tm = TagManager.getInstance();
        this.include = new ArrayList<>();
        this.exclude = new ArrayList<>();
        for (String anInclude : include) {
            this.include.add(tm.getTag(anInclude));
        }
        for (String anExclude : exclude) {
            this.exclude.add(tm.getTag(anExclude));
        }
        init();
    }

    private void init() {
        Set<ImageFile> temp = new HashSet<>();
        for (Tag t : include) {
            for (ImageFile image : t) {
                temp.add(image);
            }
        }
        for (Tag e : exclude) {
            for (ImageFile image : e) {
                temp.remove(image);
            }
        }
        images = new ArrayList<>();
        images.addAll(temp);
    }

    @Override
    public int size() {
        return images.size();
    }

    @Override
    public File getImage(int index) {
        return images.get(index).getFile();
    }

    @Override
    public String getName() {
        StringBuilder includeBuilder = new StringBuilder();
        includeBuilder.append(include.get(0).getName());
        for (int i = 1; i < include.size(); i++) {
            includeBuilder.append(", ").append(include.get(i).getName());
        }
        if (!exclude.isEmpty()) {
            StringBuilder excludeBuilder = new StringBuilder();
            excludeBuilder.append(exclude.get(0).getName());
            for (int i = 1; i < exclude.size(); i++) {
                excludeBuilder.append(", ").append(exclude.get(i).getName());
            }
            return String.format(Locale.getDefault(), "Tag: (+%s : -%s)", includeBuilder.toString().trim(), excludeBuilder.toString().trim());
        }
        return String.format(Locale.getDefault(), "Tag: (+%s)", includeBuilder.toString().trim());
    }

    @Override
    public JSONObject jsonify() {
        throw new RuntimeException("Stub!");
//        JSONObject object = new JSONObject();
//        try {
//            object.put(JSON.KEY_TYPE, JSON.VALUE_TAG);
//            JSONArray includeArray = new JSONArray();
//            for (Tag t : include) {
//                includeArray.put(t.getName());
//            }
//            object.put(JSON.KEY_TAG_INCLUDE, includeArray);
//            JSONArray excludeArray = new JSONArray();
//            for (Tag t : exclude) {
//                excludeArray.put(t.getName());
//            }
//            object.put(JSON.KEY_TAG_EXCLUDE, excludeArray);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return object;
    }
}
