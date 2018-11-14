package cvic.wallpapermanager.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import cvic.wallpapermanager.R;
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
        Dialog menu = getContextMenu(ctx);
        if (menu != null) {
            menu.show();
            return true;
        }
        return false;
    }

    private Dialog getContextMenu(final Context ctx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(toString());
        @SuppressLint("InflateParams") View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_albumable_ctx_menu, null);
        builder.setView(view);
        final Dialog dialog = builder.create();
        Button btnRename = view.findViewById(R.id.btn_rename);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        btnRename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                TextInputDialog renameDialog = new TextInputDialog(ctx, Albumable.this, "Rename", getName());
                renameDialog.show();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
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
