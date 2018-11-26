package cvic.wallpapermanager.model.albumable;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cvic.wallpapermanager.JSON;
import cvic.wallpapermanager.utils.JSONUtils;

public class TagManager implements Iterable<Tag>{

    private static final String TAG = "cvic.wpm.tagm";

    /**
     * Singleton Pattern
     */
    private static TagManager instance;

    private List<Tag> tags;

    public static TagManager getInstance() {
        if (instance == null) {
            instance = new TagManager();
        }
        return instance;
    }

    private TagManager() {
        tags = new ArrayList<>();
        addTag(new DefaultTag());
    }

    public void addTag(Tag tag) {
        if (tags.add(tag)) {
            tag.setId(tags.size() - 1);
        }
    }

    public boolean hasTag(String name) {
        for (Tag tag : tags) {
            if (tag.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void removeTag(Tag tag) {
        if (tags.remove(tag)) {
            tag.setId(-1);
        }
    }

    public void removeTag(int idx) {
        if (idx > 0 && idx < tags.size()) {
            Tag tag = tags.remove(idx);
            tag.setId(-1);
        }
    }

    public Tag getTag(int id) {
        return tags.get(id);
    }

    public Tag getTag(String name) {
        for (Tag tag : tags) {
            if (tag.getName().equals(name)) {
                return tag;
            }
        }
        Tag newTag = new Tag(name);
        tags.add(newTag);
        return newTag;
    }

    public int size() {
        return tags.size();
    }

    public String[] getTagNames() {
        String[] names = new String[tags.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = tags.get(i).getName();
        }
        return names;
    }

    public void clear() {
        tags.clear();
        addTag(new DefaultTag());
    }

    @NonNull
    @Override
    public Iterator<Tag> iterator() {
        return tags.iterator();
    }

    public void saveJson(File file) {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        for (int i = 1; i < tags.size(); i++) {
            //Starting at 1 to exclude DefaultTag
            Tag tag = tags.get(i);
            array.put(tag.getName());
        }
        try {
            object.put(JSON.KEY_TAGS, array);
            JSONUtils.writeJSON(file, object);
            Log.i(TAG, "Saved tags to: " + file.getAbsolutePath());
        } catch (JSONException e) {
            Log.i(TAG, "Error saving to json");
            e.printStackTrace();
        }

    }

    public Tag getDefault() {
        return getInstance().getTag(0);
    }
}
