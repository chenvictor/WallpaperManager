package cvic.wallpapermanager.utils;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static final String TAG = "cvic.wpm.fu";

    public static boolean copyFile (File src, File dest) {
        if (dest.exists()) {
            Log.i(TAG, "Destination file already exists!");
            return false;
        }
        try (InputStream in = new FileInputStream(src)) {
            try(OutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);
            }
        } catch (IOException e) {
            Log.i(TAG, "IOException");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static File getValidDest(File srcFile, File targetDir) {
        File dest;
        int counter = 0;
        final int MAX = targetDir.list().length + 5;
        String append = "";
        do {
            dest = new File(targetDir, append + srcFile.getName());
            append = String.valueOf(counter++);
            if (counter > MAX) {
                throw new RuntimeException("Valid Destination could not be found! " + counter + " max: " + MAX);
            }
        } while(dest.exists());
        return dest;
    }

}
