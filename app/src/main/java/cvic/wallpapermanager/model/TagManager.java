package cvic.wallpapermanager.model;

import java.util.ArrayList;
import java.util.List;

public class TagManager {

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
    }

    protected void addTag(int idx, Tag tag) {
        tags.add(idx, tag);
        for (int i = idx; i < tags.size(); i++) {
            tags.get(i).setId(i);
        }
    }

    public void addTag(Tag tag) {
        if (tags.add(tag)) {
            tag.setId(tags.size() - 1);
        }
    }

    boolean hasTag(String name) {
        for (Tag tag : tags) {
            if (tag.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void removeTag(Tag tag) {
        if (tags.remove(tag)) {
            for (int idx = 0; idx < tags.size(); idx++) {
                tags.get(idx).setId(idx);
            }
            tag.setId(-1);
        }
    }

    public void removeTag(int idx) {
        if (idx < tags.size()) {
            Tag tag = tags.remove(idx);
            tag.setId(-1);
        }
    }

    public void initialize() {
        for (Folder folder : FolderManager.getInstance()) {
            addTag(new FolderTag(folder));
        }
    }

    public Tag getTag(int id) {
        return tags.get(id);
    }

    public int size() {
        return tags.size();
    }
}
