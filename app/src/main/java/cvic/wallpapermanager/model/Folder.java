package cvic.wallpapermanager.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import java.io.File;

import cvic.wallpapermanager.AddImagesActivity;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.FilterUtils;

public class Folder extends Albumable {

    public static final String EXTRA_DEST_PATH = "folder.destPath";
    public static final int PICK_IMAGE = 123;

    private File mFile;
    private boolean root;
    private File[] images;

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
        Intent intent = new Intent(parent, AddImagesActivity.class);
        intent.putExtra(EXTRA_DEST_PATH, mFile.getAbsolutePath());
        parent.startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public boolean rename(String newName) {
        Log.i("cvic.wpm.folder", "Renaming from " + getName());
        Log.i("cvic.wpm.folder", "Renaming to " + newName);
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
    public void delete(Context ctx) {
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

    @Override
    public String toString() {
        return "Folder: " + getName();
    }
}
