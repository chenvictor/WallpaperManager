package cvic.wallpapermanager.model.album;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Tag extends Albumable {

    private String name;
    Set<ImageFile> imageSet;
    List<ImageFile> imageList;

    Tag(String name) {
        this.name = name;
        imageSet = new HashSet<>();
        imageList = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getImage(int idx) {
        return imageList.get(idx).getFile();
    }

    @Override
    public int getCount() {
        return imageSet.size();
    }

    @Override
    public File getPreview() {
        if (getCount() == 0) {
            return null;
        }
        return imageList.get(0).getFile();
    }

    @Override
    public void refresh() {

    }

    @Override
    public void addImage(Activity activity) {

    }

    @Override
    public boolean rename(String newName) {
        if (TagManager.getInstance().hasTag(newName)) {
            return false;
        }
        this.name = newName;
        return true;
    }

    @Override
    public void delete(Context ctx) {

    }

    @NonNull
    @Override
    public String toString() {
        return "Tag: " + getName();
    }
}
