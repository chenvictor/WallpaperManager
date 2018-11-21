package cvic.wallpapermanager.model.album;

import org.json.JSONObject;

import java.io.File;

public class NullAlbum extends Album {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public File getImage(int index) {
        return null;
    }

    @Override
    public String getName() {
        return "NONE";
    }

    @Override
    public JSONObject jsonify() {
        return null;
    }
}
