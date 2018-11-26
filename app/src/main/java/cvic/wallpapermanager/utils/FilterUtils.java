package cvic.wallpapermanager.utils;

import java.io.File;
import java.io.FilenameFilter;

public class FilterUtils {

    private static final String[] ACCEPTED = {".png", ".jpg", ".jpeg"};

    public static final int FOLDER = 0;
    public static final int IMAGE = 1;
    public static final int EITHER = 2;
    public static final int FOLDER_EMPTY = 3;

    public static FilenameFilter get(int type) {
        switch (type) {
            case FOLDER:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        File dir = new File(file, s);
                        return dir.isDirectory() && dir.list().length > 0;
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
                        if (file.isDirectory() && file.list().length > 0)
                            return true;
                        for (String extension : ACCEPTED) {
                            if (s.endsWith(extension)) {
                                return true;
                            }
                        }
                        return false;
                    }
                };
            case FOLDER_EMPTY:
                return new FilenameFilter() {
                    @Override
                    public boolean accept(File file, String s) {
                        return new File(file, s).isDirectory();
                    }
                };
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static boolean isValidName(String text) {
        return text.matches("[a-zA-Z0-9]+");
    }

}
