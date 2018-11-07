package cvic.wallpapermanager.model;

import java.util.ArrayList;
import java.util.List;

public class FolderManager {

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
        }
    }

    private void removeAll() {
        for (Folder folder : folders) {
            folder.setId(-1);
        }
        folders.clear();
    }

    public void removeFolder(Folder folder) {
        if (folders.remove(folder)) {
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
}
