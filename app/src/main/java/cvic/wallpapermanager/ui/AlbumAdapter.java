package cvic.wallpapermanager.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import cvic.wallpapermanager.AlbumableViewActivity;
import cvic.wallpapermanager.ImageViewHolder;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.albumable.Albumable;
import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.model.albumable.FolderManager;
import cvic.wallpapermanager.model.albumable.Tag;
import cvic.wallpapermanager.model.albumable.TagManager;

public class AlbumAdapter extends Adapter<ImageViewHolder> implements Albumable.AlbumChangeListener {

    private static final String TAG = "cvic.wpm.a_a";

    private int size;

    private int viewType = 0;

    private final AlbumsFragment mFrag;
    private final Context mCtx;

    private Map<Albumable, Integer> adapterPositionMap;

    AlbumAdapter(AlbumsFragment fragment, int gridSize) {
        mFrag = fragment;
        mCtx = fragment.getContext();
        size = gridSize;
        adapterPositionMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_preview_albumable, viewGroup, false);

        final ImageViewHolder holder = new ImageViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClicked(holder.getAdapterPosition());
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Albumable item = getItem(holder.getAdapterPosition());
                assert (item != null);
                return item.onLongClick(view.getContext());
            }
        });
        return holder;
    }

    private void onClicked(int idx) {
        Albumable item = getItem(idx);
        Intent intent = new Intent(mCtx, AlbumableViewActivity.class);
        intent.putExtra(Albumable.EXTRA_TYPE, viewType);
        assert item != null;
        intent.putExtra(Albumable.EXTRA_ID, item.getId());
        mCtx.startActivity(intent);
    }

    private Albumable getItem(int idx) {
        switch (viewType) {
            case Albumable.TYPE_FOLDER:
                return FolderManager.getInstance().getFolder(idx);
            case Albumable.TYPE_TAG:
                return TagManager.getInstance().getTag(idx);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder viewHolder, int index) {
        Albumable item = getItem(index);
        assert item != null;
        adapterPositionMap.put(item, index);
        item.addListener(this);
        TextView label = viewHolder.getLabel();
        label.setText(mCtx.getResources().getQuantityString(R.plurals.folder_title_plural, item.size(), item.getName(), item.size()));
        Glide.with(mCtx).load(item.getPreview()).into(viewHolder.getImage());
    }

    @Override
    public int getItemCount() {
        switch (viewType) {
            case Albumable.TYPE_FOLDER:
                return FolderManager.getInstance().size();
            case Albumable.TYPE_TAG:
                return TagManager.getInstance().size();
            default: return 0;
        }
    }

    @Override
    public void onAlbumRenameFailed(Albumable albumable, int errorCode) {
        int errorMessage = R.string.message_rename_failed_generic;
        switch (errorCode) {
            case Albumable.RENAME_FAILED_ALREADY_EXISTS:
                errorMessage = R.string.message_folder_already_exists;
                break;
            case Albumable.RENAME_FAILED_INVALID_NAME:
                errorMessage = R.string.message_rename_failed_invalid_name;
                break;
            case Albumable.RENAME_FAILED_OTHER:
                errorMessage = R.string.message_rename_failed_generic;
                break;
        }
        Toast.makeText(mCtx, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAlbumRename(Albumable albumable, String newName) {
        // TODO
    }

    @Override
    public void onAlbumDelete(Albumable albumable) {
        Log.i(TAG, "Album deleted: " + albumable.getName());
        switch (viewType) {
            case Albumable.TYPE_FOLDER:
                FolderManager.getInstance().removeFolder((Folder) albumable);
                break;
            case Albumable.TYPE_TAG:
                TagManager.getInstance().removeTag((Tag) albumable);
                break;
        }
        notifyDataSetChanged();
        if (getItemCount() == 0) {
            mFrag.notifyEmpty(true);
        }
    }

    @Override
    public void onAlbumImagesChanged(Albumable albumable) {
        Integer idx = adapterPositionMap.get(albumable);
        if (idx != null) {
            notifyItemChanged(idx);
        }
    }

    void addAlbum(Albumable album) {
        switch (viewType) {
            case Albumable.TYPE_FOLDER:
                FolderManager.getInstance().addFolder((Folder) album);
                break;
            case Albumable.TYPE_TAG:
                TagManager.getInstance().addTag((Tag) album);
                break;
        }
        notifyItemInserted(getItemCount() - 1);
        if (getItemCount() == 1) {
            mFrag.notifyEmpty(false);
        }
    }

    void setViewType(int idx) {
        viewType = idx;
        notifyDataSetChanged();
    }

}
