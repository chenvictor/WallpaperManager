package cvic.wallpapermanager.model.albumable;

import android.app.Activity;
import android.content.Context;

import java.io.File;

public class FolderTag extends Tag {

    private Folder folder;

    FolderTag(Folder folder) {
        super (folder.getName());
        this.folder = folder;
        imageSet = null;    //null since we do not need these
        imageList = null;
    }

    @Override
    public String getName() {
        return String.format("folder:%s", folder.getName());
    }

    @Override
    public File getImage(int idx) {
        return folder.getImage(idx);
    }

    @Override
    public int size() {
        return folder.size();
    }

    @Override
    public File getPreview() {
        return folder.getPreview();
    }

    @Override
    public void refresh() {
        folder.refresh();
    }

    @Override
    public void addImage(Activity activity) {
        throw new UnsupportedOperationException("Cannot add images to FolderTag");
    }

    @Override
    public boolean rename(String newName) {
        throw new UnsupportedOperationException("Cannot rename FolderTag");
    }

    @Override
    public void delete(Context ctx) {
        throw new UnsupportedOperationException("Cannot delete FolderTag");
    }
}
