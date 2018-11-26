package cvic.wallpapermanager.model.albumable;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.Objects;

import cvic.wallpapermanager.dialogs.ContextMenuDialog;
import cvic.wallpapermanager.dialogs.TextInputDialog;
import cvic.wallpapermanager.model.ImageFile;

public abstract class Albumable implements TextInputDialog.ResultListener, Iterable<ImageFile>{

    public static final String EXTRA_TYPE = "cvic.wpm.extra_album_type";
    public static final String EXTRA_ID = "cvic.wpm.extra_album_id";

    public static final int TYPE_FOLDER = 0;
    public static final int TYPE_TAG = 1;

    public static final int RENAME_SUCCESS = 0;
    public static final int RENAME_FAILED_INVALID_NAME = 1;
    public static final int RENAME_FAILED_ALREADY_EXISTS = 2;
    public static final int RENAME_FAILED_OTHER = 3;

    public static final int PICK_IMAGE = 123;

    private int id = -1;

    AlbumChangeListener mListener;

    public void setListener(AlbumChangeListener listener) {
        mListener = listener;
    }

    public void onClick(Context ctx) {

    }

    public boolean onLongClick(Context ctx) {
        ContextMenuDialog menu = getContextMenu(ctx);
        if (menu != null) {
            menu.show();
            return true;
        }
        return false;
    }

    protected ContextMenuDialog getContextMenu(final Context ctx) {
        ContextMenuDialog dialog = new ContextMenuDialog(ctx, toString());
        dialog.addButton("Rename", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputDialog renameDialog = new TextInputDialog(ctx, Albumable.this, "Rename", getName());
                renameDialog.show();
            }
        });
        dialog.addButton("Delete", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(ctx);
            }
        });
        return dialog;
    }

    @Override
    public void onResult(String input) {
        if (getName().equals(input)) {
            return;
        }
        int code = rename(input);
        if (mListener != null) {
            if (code == RENAME_SUCCESS) {
                mListener.onAlbumRename(this, input);
            } else {
                mListener.onAlbumRenameFailed(this, code);
            }
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public abstract String getName();
    public abstract ImageFile getImage(int idx);
    public abstract int size();
    public abstract ImageFile getPreview();

    public abstract void removeImage(ImageFile imageFile);
    public abstract void addImage(ImageFile imageFile);

    public abstract Class<? extends Activity> addImagesActivityClass();

    /**
     * Rename the album
     * @param newName   new name to set to
     * @return          result code
     */
    protected abstract int rename(String newName);

    /**
     * Deletes the album
     */
    protected abstract void delete(Context ctx);

    public interface AlbumChangeListener {

        /**
         * Notifies the listener that a rename attempt failed
         * @param albumable     this
         * @param errorCode     reason for rename failure
         */
        void onAlbumRenameFailed(Albumable albumable, int errorCode);

        /**
         * Notifies the listener that a rename has succeeded
         * @param albumable      this
         * @param newName        the new name
         */
        void onAlbumRename(Albumable albumable, String newName);

        /**
         * Notifies the listener that this album is being deleted
         * @param albumable     this
         */
        void onAlbumDelete(Albumable albumable);

        /**
         * Notifies that the images in this album have been changed
         * @param albumable     this
         */
        void onAlbumImagesChanged(Albumable albumable);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Albumable albumable = (Albumable) o;
        return id == albumable.id && getName().equals(albumable.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
