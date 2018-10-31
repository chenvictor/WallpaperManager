package cvic.wallpapermanager.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FilterUtils {

    public static final int FOLDER = 0;
    public static final int IMAGE = 1;

    public static FilenameFilter get(int type) {
        switch (type) {
            case FOLDER:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        File dir = new File(file, s);
                        return dir.isDirectory() && containsImages(dir);
                    }
                };
            case IMAGE:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return s.endsWith("png") || s.endsWith("jpg");
                    }
                };
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static boolean containsImages(File folder) {
        return folder.list(get(IMAGE)).length != 0;
    }

}
