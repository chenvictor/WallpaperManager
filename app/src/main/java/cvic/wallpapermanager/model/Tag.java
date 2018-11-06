package cvic.wallpapermanager.model;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class Tag extends Albumable {

    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {

        @Override
        public Tag createFromParcel(Parcel parcel) {
            return new Tag(parcel);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };

    public Tag(Parcel parcel) {

    }

    public Tag () {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public File getImage(int idx) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public File getPreview() {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void addImage(Activity activity) {

    }

    @Override
    public boolean rename(String newName) {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {

    }

    @Override
    public String toString() {
        return "Tag: " + getName();
    }
}
