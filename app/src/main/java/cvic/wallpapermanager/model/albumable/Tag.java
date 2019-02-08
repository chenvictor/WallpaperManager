package cvic.wallpapermanager.model.albumable;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import cvic.wallpapermanager.model.ImageFile;

public class Tag extends Albumable {

    private String name;
    private Set<ImageFile> imageSet;
    private List<ImageFile> imageList;

    public Tag(String name) {
        this.name = name;
        imageSet = new HashSet<>();
        imageList = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ImageFile getImage(int idx) {
        return imageList.get(idx);
    }

    @Override
    public int size() {
        return imageSet.size();
    }

    @Override
    public ImageFile getPreview() {
        if (size() == 0) {
            return null;
        }
        return imageList.get(0);
    }

    @Override
    public void addImage(ImageFile file) {
        if (imageSet.add(file)) {
            imageList.add(file);
            file.addTag(this);
        }
    }

    @Override
    public Class<? extends Activity> addImagesActivityClass() {
        return null;
    }

    @Override
    public void removeImage(ImageFile file) {
        if (imageSet.remove(file)) {
            imageList.remove(file);
            file.removeTag(this);
        }
    }

    @Override
    public int rename(String newName) {
        if (TagManager.getInstance().hasTag(newName)) {
            return RENAME_FAILED_ALREADY_EXISTS;
        }
        this.name = newName;
        return RENAME_SUCCESS;
    }

    @Override
    public void delete(Context ctx) {
        TagManager tm = TagManager.getInstance();
        Tag untagged = tm.getTag(0);
        // Add any files from this one that have no other tags to the untagged tag
        for (ImageFile file : this) {
            file.removeTag(this);
            if (file.numTags() == 0) {
                file.addTag(untagged);
                untagged.imageList.add(file);
                untagged.imageSet.add(file);
            }
        }
        untagged.imageSet.addAll(imageSet);
        untagged.imageList.addAll(imageSet);
        for (AlbumChangeListener l : listeners) {
            l.onAlbumDelete(this);
        }
    }

    public boolean hasImage(ImageFile image) {
        return imageSet.contains(image);
    }

    @NonNull
    @Override
    public String toString() {
        return "Tag: " + getName();
    }

    @NonNull
    @Override
    public Iterator<ImageFile> iterator() {
        return imageList.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Tag that = (Tag) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
