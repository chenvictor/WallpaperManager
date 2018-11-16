package cvic.wallpapermanager.model;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cvic.wallpapermanager.dialogs.ContextMenuDialog;
import cvic.wallpapermanager.dialogs.TextInputDialog;

public abstract class Albumable implements TextInputDialog.ResultListener{

    private int id = -1;

    public static final String EXTRA_TYPE = "cvic.wpm.extra_album_type";
    public static final String EXTRA_ID = "cvic.wpm.extra_album_id";
    public static final int TYPE_FOLDER = 0;
    public static final int TYPE_TAG = 1;

    AlbumChangeListener mListener;
    int listenerIdx;

    private Set<ImageFile> images;

    Albumable() {
        images = new HashSet<>();
    }

    public void setListener(AlbumChangeListener listener, int idx) {
        mListener = listener;
        listenerIdx = idx;
    }

    public boolean onLongClick(Context ctx) {
        ContextMenuDialog menu = getContextMenu(ctx);
        if (menu != null) {
            menu.show();
            return true;
        }
        return false;
    }

    private ContextMenuDialog getContextMenu(final Context ctx) {
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
        boolean success = rename(input);
        if (mListener != null) {
            if (success) {
                mListener.onAlbumRename(listenerIdx, input);
            } else {
                mListener.onAlbumRenameFailed();
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
    public abstract File getImage(int idx);
    public abstract int getCount();
    public abstract File getPreview();

    public abstract void refresh();

    public abstract void addImage(Activity activity);

    /**
     * Rename the album
     * @param newName   new name to set to
     * @return          true if the rename was successful, false otherwise
     */
    protected abstract boolean rename(String newName);

    /**
     * Deletes the album
     */
    protected abstract void delete(Context ctx);

    public interface AlbumChangeListener {

        void onAlbumRenameFailed();
        void onAlbumRename(int idx, String newName);
        void onAlbumDelete(int idx);

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
