package cvic.wallpapermanager.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.model.albumable.FolderManager;

public class ImageFileManager {

    /**
     * Manager to ensure no duplicate ImageFiles will exist
     */
    private static final ImageFileManager ourInstance = new ImageFileManager();

    private Map<File, ImageFile> fileMap;

    public static ImageFileManager getInstance() {
        return ourInstance;
    }

    private ImageFileManager() {
        fileMap = new HashMap<>();
    }

    public ImageFile getImage(Folder folder, File file) {
        if (fileMap.containsKey(file)) {
            return fileMap.get(file);
        }
        ImageFile newImage = new ImageFile(folder, file);
        fileMap.put(file, newImage);
        return newImage;
    }

    public ImageFile getImage(File file) {
        if (fileMap.containsKey(file)) {
            return fileMap.get(file);
        }
        Folder folder = FolderManager.getInstance().getFolder(file.getParentFile().getName());
        ImageFile newImage = new ImageFile(folder, file);
        fileMap.put(file, newImage);
        return newImage;
    }

    public List<ImageFile> getImages() {
        return Collections.unmodifiableList(new ArrayList<>(fileMap.values()));
    }

    public void clear() {
        fileMap.clear();
    }

    /**
     * @param file  file to remove, if it exists
     */
    void removeFile(File file) {
        fileMap.remove(file);
    }

    void fileMoved(File src, File dest) {
        if (fileMap.containsKey(src)) {
            fileMap.put(dest, fileMap.remove(src));
        }
    }

    void addImage(ImageFile imageFile) {
        fileMap.put(imageFile.getFile(), imageFile);
    }
}
