package cvic.wallpapermanager.model.albumable;

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

    public synchronized void setFolders(List<Albumable> folders) {
        removeAll();
        for (Albumable folder : folders) {
            addFolder((Folder) folder);
        }
    }

    public synchronized void addFolder(Folder folder) {
        if (folders.add(folder)) {
            folder.setId(folders.size() - 1);
            FolderTag tag = new FolderTag(folder);
            folder.setAssociated(tag);
            TagManager.getInstance().addTag(folder.getId(), tag);
        }
    }

    private synchronized void removeAll() {
        for (Folder folder : folders) {
            TagManager.getInstance().removeTag(folder.getAssociated());
            folder.setId(-1);
        }
        folders.clear();
    }

    private synchronized void removeFolder(Folder folder) {
        if (folders.remove(folder)) {
            TagManager.getInstance().removeTag(folder.getAssociated());
            for (int idx = 0; idx < folders.size(); idx++) {
                folders.get(idx).setId(idx);
            }
            folder.setId(-1);
        }
    }

    public synchronized void removeFolder(int id) {
        if (id < folders.size()) {
            removeFolder(folders.get(id));
        }
    }

    public synchronized Folder getFolder(int id) {
        return folders.get(id);
    }

    public synchronized Folder getFolder(String name) {
        for (Folder folder : folders) {
            if (folder.getName().equals(name)) {
                return folder;
            }
        }
        return null;
    }

    public synchronized int size() {
        return folders.size();
    }

    public synchronized String[] getFolderNames() {
        String[] names = new String[folders.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = folders.get(i).getName();
        }
        return names;
    }

    @NonNull
    @Override
    public synchronized Iterator<Folder> iterator() {
        return folders.iterator();
    }
}
