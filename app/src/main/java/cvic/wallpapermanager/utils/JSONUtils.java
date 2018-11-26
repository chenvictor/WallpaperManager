package cvic.wallpapermanager.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONUtils {

    public static JSONObject getJSON(File file) throws FileNotFoundException, JSONException {
        return new JSONObject(readString(file));
    }

    public static JSONObject getJSON(String path) throws FileNotFoundException, JSONException{
        return getJSON(new File(path));
    }

    public static boolean writeJSON(File file, JSONObject data) {
        try {
            writeString(file, data.toString(0));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeJSON(String path, JSONObject data) {
        return writeJSON(new File(path), data);
    }

    private static String readString(File file) throws FileNotFoundException {
        FileInputStream inputStream;
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        try {
            inputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            inputStream.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void writeString(File file, String data) {
        FileOutputStream outputStream;
        try {
            file.getParentFile().mkdirs();
            if (file.exists()) {
                file.delete();
            }
            outputStream = new FileOutputStream(file);
            outputStream.write(data.getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            Log.i("temp", "FileNotFoundException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("temp", "IOException");
            e.printStackTrace();
        }
    }

    public static String jsonifyImages(String[] images) {
        JSONObject data = new JSONObject();
        try {
            data.put("type", "IMAGE");
            JSONArray array = new JSONArray();
            for (String image : images) {
                array.put(image);
            }
            data.put("images", array);
            return data.toString(0);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
