package cvic.wallpapermanager.model.album;

import org.json.JSONObject;

import java.io.File;

public abstract class Album {

    /**
     * Represents a set of images to be used as a wallpaper
     */


    public final File getPreview() {
        if (size() == 0) {
            return null;
        }
        return getImage(0);
    }

    /**
     * @return  the number of images in the album
     */
    public abstract int size();

    /**
     * @return  the file at the specified index
     */
    public abstract File getImage(int index);

    /**
     * @return  the display name of the album
     */
    public abstract String getName();

    /**
     * @return  a JSON representation of the album
     */
    public abstract JSONObject jsonify();

    /*
      @return  true if the album was modified in some way.
     *  eg. (image remove, tag name changed, folder deleted)
     */
    //public abstract boolean changed();

}
