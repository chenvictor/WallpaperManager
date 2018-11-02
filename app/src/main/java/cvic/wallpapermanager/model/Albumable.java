package cvic.wallpapermanager.model;

import java.io.File;

public interface Albumable {

    String getName();
    int getCount();
    File getPreview();
    void onClick();
    boolean onLongClick();

}
