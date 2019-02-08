package cvic.wallpapermanager.model.albumable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import cvic.wallpapermanager.JSON;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.SelectImagesActivity;
import cvic.wallpapermanager.dialogs.AlbumSelectDialog;
import cvic.wallpapermanager.model.ImageFile;
import cvic.wallpapermanager.model.ImageFileManager;
import cvic.wallpapermanager.utils.FilterUtils;
import cvic.wallpapermanager.utils.JSONUtils;

public class Folder extends Albumable {

    private static final String TAG = "cvic.wpm.alb_folder";

    private File directory;
    private List<ImageFile> imageList;

    public Folder(File directory) {
        this.directory = directory;
        imageList = new ArrayList<>();
        init();
    }

    private void init() {
        ImageFileManager ifm = ImageFileManager.getInstance();
        TagManager tm = TagManager.getInstance();
        for (File image : directory.listFiles(FilterUtils.get(FilterUtils.IMAGE))) {
            imageList.add(ifm.getImage(this, image));
        }
        // Parse tags and add them to images
        File tagData = new File(directory, JSON.FILE_TAGS);
        try {
            JSONObject tagJson = JSONUtils.getJSON(tagData);
            JSONArray array = tagJson.getJSONArray(JSON.KEY_IMAGES);
            for (int i = 0; i < array.length(); i++) {
                JSONObject imageData = array.getJSONObject(i);
                String name = imageData.getString(JSON.KEY_IMG_NAME);
                JSONArray tags = imageData.getJSONArray(JSON.KEY_IMG_TAGS);
                ImageFile imageFile = ifm.getImage(new File(directory, name));
                for (int j = 0; j < tags.length(); j++) {
                    imageFile.addTag(tm.getTag(tags.getString(j)));
                }
            }
        } catch (FileNotFoundException e) {
            Log.i(TAG, "Folder data tag not found. No tags applied.");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.i(TAG, "JSONException! No tags applied.");
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return directory.getName();
    }

    @Override
    public ImageFile getImage(int idx) {
        return imageList.get(idx);
    }

    @Override
    public int size() {
        return imageList.size();
    }

    public File getFile() {
        return directory;
    }

    @Override
    public ImageFile getPreview() {
        if (size() == 0) {
            return null;
        }
        return imageList.get(0);
    }

    @Override
    public void removeImage(ImageFile imageFile) {
        if (imageFile.getFolder() == this) {
            Log.i(TAG, "ImageFile cannot be removed from a folder before it is moved");
        } else {
            imageList.remove(imageFile);
        }
    }

    @Override
    public void addImage(ImageFile imageFile) {
        if (!imageList.contains(imageFile)) {
            imageList.add(imageFile);
            imageFile.setFolder(this);
        }
    }

    @Override
    public Class<? extends Activity> addImagesActivityClass() {
        return SelectImagesActivity.class;
    }

    @Override
    public int rename(String newName) {
        Log.i(TAG, "Renaming to " + newName);
        if (FilterUtils.isValidName(newName)) {
            File renamed = new File(directory.getParentFile(), newName);
            if (renamed.exists()) {
                return RENAME_FAILED_ALREADY_EXISTS;
            }
            if (directory.renameTo(renamed)) {
                directory = renamed;
                return RENAME_SUCCESS;
            } else {
                return RENAME_FAILED_OTHER;
            }
        } else {
            return RENAME_FAILED_INVALID_NAME;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void delete(final Context ctx) {
        if (size() == 0) {
            // Delete tags.json if exists
            File tagsJson = new File(directory, JSON.FILE_TAGS);
            if (tagsJson.exists()) {
                tagsJson.delete();
                Log.i(TAG, "tag.json deleted");
            }
            directory.delete();
            for (AlbumChangeListener l : listeners) {
                l.onAlbumDelete(this);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setTitle(toString()).setMessage("This folder contains images. Transfer them to another folder?");
            builder.setCancelable(true);
            builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlbumSelectDialog selectFolder = new AlbumSelectDialog(ctx, new AlbumSelectDialog.ResultListener() {
                        @Override
                        public void onResult(int requestCode, int index) {
                            Log.i(TAG, "Selected Folder: " + FolderManager.getInstance().getFolder(index).getName());
                            Folder.this.moveContentsTo(FolderManager.getInstance().getFolder(index));
                        }
                    }, Albumable.TYPE_FOLDER, getId());
                    selectFolder.show();
                    dialogInterface.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //delete the file and its contents
                    for (File file : directory.listFiles()) {
                        file.delete();
                    }
                    directory.delete();
                    for (AlbumChangeListener l : listeners) {
                        l.onAlbumDelete(Folder.this);
                    }
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    private void moveContentsTo(Folder target) {
        List<ImageFile> temp = new ArrayList<>(imageList);
        // Use a temp holder to avoid concurrent modification
        for (ImageFile imageFile : temp) {
            imageFile.moveTo(target);
        }
        // Delete tags.json if exists
        File tagsJson = new File(directory, JSON.FILE_TAGS);
        if (tagsJson.exists()) {
            //noinspection ResultOfMethodCallIgnored
            tagsJson.delete();
            Log.i(TAG, "tag.json deleted");
        }
        if(directory.delete()) {
            for (AlbumChangeListener l : listeners) {
                l.onAlbumDelete(this);
            }
        } else {
            Log.e(TAG, "Failed to delete folder after moving contents!");
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "Folder: " + getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Folder folder = (Folder) o;
        return Objects.equals(directory, folder.directory);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), directory);
    }

    void saveJson() {
        try {
            JSONObject object = new JSONObject();
            JSONArray images = new JSONArray();
            for (ImageFile image : imageList) {
                JSONObject img = new JSONObject();
                img.put(JSON.KEY_IMG_NAME, image.getFile().getName());
                JSONArray tags = new JSONArray();
                for (Tag tag : image.getTags()) {
                    if (!tag.equals(TagManager.getInstance().getDefault())) {
                        tags.put(tag.getName());
                    }
                }
                img.put(JSON.KEY_IMG_TAGS, tags);
                if (tags.length() > 0) {
                    images.put(img);
                }
            }
            object.put(JSON.KEY_IMAGES, images);
            JSONUtils.writeJSON(new File(directory, JSON.FILE_TAGS).getAbsolutePath(), object);
        } catch (JSONException e) {
            Log.i(TAG, "Error saving to json");
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Iterator<ImageFile> iterator() {
        return imageList.iterator();
    }
}
