package cvic.wallpapermanager.model;

import java.io.File;

import cvic.wallpapermanager.utils.FilterUtils;

public class Folder implements Albumable {

    private final File mFile;
    private File[] images;
    private int count;
    private boolean root;

    public Folder(File file, boolean root) {
        mFile = file;
        images = mFile.listFiles(FilterUtils.get(FilterUtils.IMAGE));
        count = images.length;
        this.root = root;
    }

    public Folder (File file) {
        this (file, false);
    }

    @Override
    public String getName() {
        if (root) {
            return "default";
        }
        return mFile.getName();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public File getPreview() {
        return images[0];
    }

    @Override
    public void onClick() {
        //TODO
    }

    @Override
    public boolean onLongClick() {
        //TODO
        return false;
    }
}
