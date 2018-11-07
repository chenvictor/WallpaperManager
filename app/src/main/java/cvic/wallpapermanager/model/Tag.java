package cvic.wallpapermanager.model;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;

public class Tag extends Albumable {

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
    public void delete(Context ctx) {

    }

    @NonNull
    @Override
    public String toString() {
        return "Tag: " + getName();
    }
}
