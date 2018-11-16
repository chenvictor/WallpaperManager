package cvic.wallpapermanager.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.SelectImagesActivity;
import cvic.wallpapermanager.dialogs.AlbumSelectDialog;
import cvic.wallpapermanager.utils.FilterUtils;

public class Folder extends Albumable {

    private static final String TAG = "cvic.wpm.alb_folder";

    public static final int PICK_IMAGE = 123;

    private File mFile;
    private boolean root;
    private File[] images;

    private FolderTag associated;

    public Folder(File file, boolean root) {
        mFile = file;
        refresh();
        this.root = root;
    }

    public Folder (File file) {
        this (file, false);
    }

    @Override
    public void refresh() {
        images = mFile.listFiles(FilterUtils.get(FilterUtils.IMAGE));
    }

    @Override
    public String getName() {
        if (root) {
            return "default";
        }
        return mFile.getName();
    }

    @Override
    public File getImage(int idx) {
        return images[idx];
    }

    @Override
    public int getCount() {
        return images.length;
    }

    public File getFile() {
        return mFile;
    }

    @Override
    public File getPreview() {
        if (getCount() == 0) {
            return null;
        }
        return images[0];
    }

    @Override
    public void addImage(Activity parent) {
        Intent intent = new Intent(parent, SelectImagesActivity.class);
        parent.startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public boolean rename(String newName) {
        Log.i(TAG, "Renaming from " + getName());
        Log.i(TAG, "Renaming to " + newName);
        if (newName == null) {
            return false;
        }
        File renamed = new File(mFile.getParentFile(), newName);
        if (renamed.exists()) {
            return false;
        }
        boolean temp = mFile.renameTo(renamed);
        mFile = renamed;
        return temp;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void delete(final Context ctx) {
        if (getCount() == 0) {
            mFile.delete();
            mListener.onAlbumDelete(listenerIdx);
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
                    for (File file : mFile.listFiles()) {
                        file.delete();
                    }
                    mFile.delete();
                    mListener.onAlbumDelete(listenerIdx);
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        }
    }

    private void moveContentsTo(Folder target) {
        for (File file : images) {
            if(!file.renameTo(getDestination(target, file))) {
                Log.e(TAG, "Rename failed!");
            }
        }
        target.refresh();
        if(mFile.delete()) {
            mListener.onAlbumDelete(listenerIdx);
        } else {
            Log.e(TAG, "Failed to delete folder after moving contents!");
        }
    }

    private File getDestination(Folder target, File file) {
        int numFiles = target.getCount();  //lists the number of files already in the folder
        File ret = new File(target.mFile, file.getName());
        if (!ret.exists()) {
            return ret;
        }
        for (int i = 0; i < numFiles; i++) {
            ret = new File(target.mFile, String.valueOf(i) + file.getName());
            if (!ret.exists()) {
                return ret;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        return "Folder: " + getName();
    }

    public FolderTag getAssociated() {
        return associated;
    }

    public void setAssociated(FolderTag associated) {
        this.associated = associated;
    }
}
