package cvic.wallpapermanager.model.albumable;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FolderManager implements Iterable<Folder>{

    /**
     * Singleton Pattern
     */
    private static final FolderManager instance = new FolderManager();
    private static File root;
    private List<Folder> folders;

    public static FolderManager getInstance() {
        return instance;
    }

    public static void setRoot(File root) {
        FolderManager.root = root;
    }

    private FolderManager() {
        folders = new ArrayList<>();
    }

    public synchronized void addFolder(Folder folder) {
        if (folders.add(folder)) {
            folder.setId(folders.size() - 1);
        }
    }

    public synchronized void removeFolder(Folder folder) {
        if (folders.remove(folder)) {
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

    public void clear() {
        folders.clear();
    }

    public void saveJson() {
        for (Folder folder : folders) {
            folder.saveJson();
        }
    }

}
