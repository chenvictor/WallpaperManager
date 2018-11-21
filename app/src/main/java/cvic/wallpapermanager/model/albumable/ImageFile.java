package cvic.wallpapermanager.model.albumable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ImageFile {

    private final File file;
    private Folder folder;

    private Set<Tag> tags;

    public ImageFile(Folder folder, File file) {
        this.folder = folder;
        this.file = file;
        tags = new HashSet<>();
    }

    public void moveFolder(Folder newFolder) {
        this.folder = newFolder;
    }

    public File getFile() {
        return file;
    }

}
