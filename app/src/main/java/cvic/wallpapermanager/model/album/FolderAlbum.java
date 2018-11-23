package cvic.wallpapermanager.model.album;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cvic.wallpapermanager.JSON;
import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.model.albumable.FolderManager;

public class FolderAlbum extends Album {

    private List<Folder> folders;

    public FolderAlbum() {
        folders = new ArrayList<>();
    }

    public FolderAlbum(String[] folders) {
        this();
        for (String name : folders) {
            this.folders.add(FolderManager.getInstance().getFolder(name));
        }
    }

    @Override
    public int size() {
        int counter = 0;
        for (Folder f : folders) {
            counter += f.size();
        }
        return counter;
    }

    @Override
    public File getImage(int index) {
        Iterator<Folder> iterator = folders.iterator();
        while (index >= 0) {
            if (!iterator.hasNext()) {
                return null;
            }
            Folder search = iterator.next();
            if (index < search.size()) {
                return search.getImage(index);
            }
            index -= search.size();
        }
        return null;
    }

    @Override
    public String getName() {
        StringBuilder builder = new StringBuilder();
        builder.append(folders.get(0).getName());
        for (int i = 1; i < folders.size(); i++) {
            Folder f = folders.get(i);
            builder.append(", ").append(f.getName());
        }
        return String.format(Locale.getDefault(), "Folder: (%s)", builder.toString().trim());
    }

    @Override
    public JSONObject jsonify() {
        JSONObject object = new JSONObject();
        try {
            object.put(JSON.KEY_TYPE, JSON.VALUE_FOLDER);
            JSONArray array = new JSONArray();
            for (Folder f : folders) {
                array.put(f.getName());
            }
            object.put(JSON.KEY_FOLDERS, array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }
}
