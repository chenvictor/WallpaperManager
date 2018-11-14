package cvic.wallpapermanager.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FolderManager implements Iterable<Folder>{

    /**
     * Singleton Pattern
     */
    private static FolderManager instance;

    private List<Folder> folders;

    public static FolderManager getInstance() {
        if (instance == null) {
            instance = new FolderManager();
        }
        return instance;
    }

    private FolderManager() {
        folders = new ArrayList<>();
    }

    public void setFolders(List<Albumable> folders) {
        removeAll();
        for (Albumable folder : folders) {
            addFolder((Folder) folder);
        }
    }

    public void addFolder(Folder folder) {
        if (folders.add(folder)) {
            folder.setId(folders.size() - 1);
            FolderTag tag = new FolderTag(folder);
            folder.setAssociated(tag);
            TagManager.getInstance().addTag(folder.getId(), tag);
        }
    }

    private void removeAll() {
        for (Folder folder : folders) {
            TagManager.getInstance().removeTag(folder.getAssociated());
            folder.setId(-1);
        }
        folders.clear();
    }

    public void removeFolder(Folder folder) {
        if (folders.remove(folder)) {
            TagManager.getInstance().removeTag(folder.getAssociated());
            for (int idx = 0; idx < folders.size(); idx++) {
                folders.get(idx).setId(idx);
            }
            folder.setId(-1);
        }
    }

    public void removeFolder(int id) {
        if (id < folders.size()) {
            removeFolder(folders.get(id));
        }
    }

    public Folder getFolder(int id) {
        return folders.get(id);
    }

    public int size() {
        return folders.size();
    }

    @NonNull
    @Override
    public Iterator<Folder> iterator() {
        return folders.iterator();
    }
}
