package cvic.wallpapermanager.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FilterUtils {

    private static final String[] ACCEPTED = {".png", ".jpg", ".jpeg"};

    public static final int FOLDER = 0;
    public static final int IMAGE = 1;
    public static final int EITHER = 2;

    public static FilenameFilter get(int type) {
        switch (type) {
            case FOLDER:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        File dir = new File(file, s);
                        return dir.isDirectory();
                    }
                };
            case IMAGE:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        if (new File(file, s).isDirectory())
                            return false;
                        for (String extension : ACCEPTED) {
                            if (s.endsWith(extension)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            case EITHER:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        if (file.isDirectory())
                            return true;
                        for (String extension : ACCEPTED) {
                            if (s.endsWith(extension)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            default:
                throw new UnsupportedOperationException();
        }
    }

}
