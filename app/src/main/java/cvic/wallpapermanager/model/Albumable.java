package cvic.wallpapermanager.model;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.io.File;

import cvic.wallpapermanager.AlbumablePreviewActivity;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.TextInputDialog;

public abstract class Albumable implements Parcelable, TextInputDialog.ResultListener{

    private AlbumChangeListener mListener;
    private int listenerIdx;

    public void setListener(AlbumChangeListener listener, int idx) {
        mListener = listener;
        listenerIdx = idx;
    }

    public void onClick(Context ctx) {
        Intent intent = new Intent(ctx, AlbumablePreviewActivity.class);
        intent.putExtra(AlbumablePreviewActivity.EXTRA_ALBUM_PARCEL, this);
        ctx.startActivity(intent);
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
                //TODO: If album is not empty, prompt user to cancel, move images to other folder, or proceed
                delete();
                if (mListener != null) {
                    mListener.onAlbumDelete(listenerIdx);
                }
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
    public abstract boolean rename(String newName);

    /**
     * Deletes the album
     * @return          true if deletion was successful, false otherwise
     */
    public abstract boolean delete();

    public interface AlbumChangeListener {

        void onAlbumRenameFailed();
        void onAlbumRename(int idx, String newName);
        void onAlbumDelete(int idx);

    }

}
