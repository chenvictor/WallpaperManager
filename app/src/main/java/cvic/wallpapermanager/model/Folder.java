package cvic.wallpapermanager.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;

import cvic.wallpapermanager.utils.FilterUtils;

public class Folder extends Albumable {

    private File mFile;
    private boolean root;
    private File[] images;

    public static final Parcelable.Creator<Folder> CREATOR = new Parcelable.Creator<Folder>() {

        @Override
        public Folder createFromParcel(Parcel parcel) {
            return new Folder(parcel);
        }

        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };

    private Folder(Parcel parcel) {
        mFile = new File(parcel.readString());
        boolean[] tempBool = new boolean[1];
        parcel.readBooleanArray(tempBool);
        root = tempBool[0];
        int count = parcel.readInt();
        String[] imageNames = new String[count];
        parcel.readStringArray(imageNames);
        images = new File[count];
        for (int i = 0; i < count; i++) {
            images[i] = new File(mFile, imageNames[i]);
        }
    }

    public Folder(File file, boolean root) {
        mFile = file;
        images = mFile.listFiles(FilterUtils.get(FilterUtils.IMAGE));
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
    public File getImage(int idx) {
        return images[idx];
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public File getPreview() {
        if (getCount() == 0) {
            return null;
        }
        return images[0];
    }

    @Override
    public void addImage() {
        Log.i("cvic.wpm.folder", "Adding Image");
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

    @Override
    public boolean delete() {
        return mFile.delete();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        boolean[] boolArray = {root};
        String[] imageNames = new String[images.length];
        for (int i = 0; i < imageNames.length; i++) {
            imageNames[i] = images[i].getName();
        }
        parcel.writeString(mFile.getAbsolutePath());
        parcel.writeBooleanArray(boolArray);
        parcel.writeInt(imageNames.length);
        parcel.writeStringArray(imageNames);
    }

    @Override
    public String toString() {
        return "Folder: " + getName();
    }
}
