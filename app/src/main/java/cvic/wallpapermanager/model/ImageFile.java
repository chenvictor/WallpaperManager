package cvic.wallpapermanager.model;

import android.util.Log;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.model.albumable.Tag;
import cvic.wallpapermanager.model.albumable.TagManager;
import cvic.wallpapermanager.utils.FileUtils;

public class ImageFile {

    private static final String TAG = "cvic.wpm.if";

    private File file;
    private Folder folder;

    private Set<Tag> tags;

    private Set<ImageFileListener> listeners;

    public ImageFile(Folder folder, File file) {
        this.folder = folder;
        this.file = file;
        tags = new HashSet<>();
        listeners = new HashSet<>();
        addTag(TagManager.getInstance().getDefault());    // Image is untagged by default
    }

    public synchronized void addListener(ImageFileListener listener) {
        listeners.add(listener);
    }

    public synchronized void remove(ImageFileListener listener) {
        listeners.remove(listener);
    }

    /**
     * Copies the file to the specified folder.
     *  modifies this to reference the new file
     * @param target    destination
     */
    public void copyTo(Folder target) {
        File dest = FileUtils.getValidDest(file, target.getFile());
        if (FileUtils.copyFile(file, dest)) {
            ImageFileManager.getInstance().fileMoved(file, dest);   //Notify the manager
            file = dest;
            target.addImage(this);
            for (ImageFileListener l : listeners) {
                l.onMoved();
            }
            Log.i(TAG, "File copied. Name: " + file.getName());
        } else {
            Log.i(TAG, "Failed to copy file!");
        }
    }

    /**
     * Moves the file to the specified folder.
     *  Updates this to reference the new file
     * @param target    destination
     */
    public void moveTo(Folder target) {
        // Move the actual file
        File dest = FileUtils.getValidDest(file, target.getFile());
        if (file.renameTo(dest)) {
            folder.removeImage(this);
            target.addImage(this);
            folder = target;
            ImageFileManager.getInstance().fileMoved(file, dest);   //Notify the manager
            file = dest;
            for (ImageFileListener l : listeners) {
                l.onMoved();
            }
            Log.i(TAG, "File moved. Name: " + file.getName());
        } else {
            Log.i(TAG, "Failed to move file!");
        }
    }

    public File getFile() {
        return file;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTag(Tag tag) {
        Tag untagged = TagManager.getInstance().getDefault();
        boolean wasUntagged = tags.size() == 1 && tags.contains(untagged);
        if(tags.add(tag)) {
            tag.addImage(this);
            if (wasUntagged) {
                removeTag(untagged);
            }
        }
    }

    public void removeTag(Tag tag) {
        if(tags.remove(tag)) {
            tag.removeImage(this);
        }
        if (tags.size() == 0) {
            // No more tags, assign default tag
            tags.add(TagManager.getInstance().getDefault());
        }
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public int numTags() {
        if (tags.contains(TagManager.getInstance().getDefault())) {
            // Default tag does not count
            return 0;
        }
        return tags.size();
    }

    public void delete() {
        if (file.delete()) {
            for (ImageFileListener l : listeners) {
                l.onDelete();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageFile imageFile = (ImageFile) o;
        return Objects.equals(file, imageFile.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }

    /**
     * Adds this to the manager if it is not already present
     */
    public void addToManager() {
        ImageFileManager ifm = ImageFileManager.getInstance();
        ifm.addImage(this);
        Log.i(TAG, "Adding image to manager");
    }

    public interface ImageFileListener {
        void onDelete();
        void onMoved();
    }

}
