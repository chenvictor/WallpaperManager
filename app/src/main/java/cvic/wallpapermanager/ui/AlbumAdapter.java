package cvic.wallpapermanager.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cvic.wallpapermanager.AlbumablePreviewActivity;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.album.Albumable;
import cvic.wallpapermanager.model.album.Folder;
import cvic.wallpapermanager.model.album.FolderManager;
import cvic.wallpapermanager.model.album.Tag;
import cvic.wallpapermanager.model.album.TagManager;
import cvic.wallpapermanager.utils.ImageCache;

public class AlbumAdapter extends Adapter implements ImageCache.CacheListener, Albumable.AlbumChangeListener {

    private static final String TAG = "cvic.wpm.a_a";

    private int size;

    private int viewType = 0;

    private final AlbumsFragment mFrag;
    private final Context mCtx;

    private ImageCache mCache;

    AlbumAdapter(AlbumsFragment fragment, int gridSize) {
        mFrag = fragment;
        mCtx = fragment.getContext();
        mCache = new ImageCache(this);
        size = gridSize;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_preview_albumable, viewGroup, false);

        final ViewHolder holder = new ViewHolder(view);
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
        Intent intent = new Intent(mCtx, AlbumablePreviewActivity.class);
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

    void flushCache() {
        mCache.flush();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Albumable item = getItem(i);
        assert item != null;
        item.setListener(this, i);
        View view = viewHolder.itemView;
        ImageView preview = view.findViewById(R.id.grid_albumable_image);
        TextView label = view.findViewById(R.id.grid_albumable_label);
        label.setText(mCtx.getResources().getQuantityString(R.plurals.folder_title_plural, item.getCount(), item.getName(), item.getCount()));
        preview.setImageBitmap(mCache.requestImage(item.getPreview(), i, size, size));
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
    public void onBitmapAvailable(int requestId, Bitmap bitmap) {
        notifyItemChanged(requestId);
    }

    @Override
    public void onAlbumRenameFailed() {
        Toast.makeText(mCtx, R.string.message_rename_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAlbumRename(int idx, String newName) {
        notifyItemChanged(idx);
    }

    @Override
    public void onAlbumDelete(int idx) {
        Log.i(TAG, "Album deleted: " + idx);
        switch (viewType) {
            case Albumable.TYPE_FOLDER:
                FolderManager.getInstance().removeFolder(idx);
                break;
            case Albumable.TYPE_TAG:
                TagManager.getInstance().removeTag(idx);
                break;
        }
        flushCache();
        notifyDataSetChanged();
        if (getItemCount() == 0) {
            mFrag.notifyEmpty(true);
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
        mCache.flush();
        notifyDataSetChanged();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
