package cvic.wallpapermanager.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.utils.ImageCache;

public class AlbumAdapter extends Adapter implements ImageCache.CacheListener, Albumable.AlbumChangeListener{

    private final AlbumsFragment mFrag;
    private final Context mCtx;

    private List<Albumable> mAdapterItems;
    private ImageCache mCache;

    AlbumAdapter(AlbumsFragment fragment) {
        mFrag = fragment;
        mCtx = fragment.getContext();
        mCache = new ImageCache(this);
        mAdapterItems = new ArrayList<>();
    }

    void setAdapterItems(List<Albumable> items) {
        mAdapterItems = items;
        //flush the image cache
        mCache.flush();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_preview_albumable, viewGroup, false);

        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapterItems.get(holder.getAdapterPosition()).onClick(mCtx);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mAdapterItems.get(holder.getAdapterPosition()).onLongClick(view.getContext());
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Albumable item = mAdapterItems.get(i);
        item.setListener(this, i);
        View view = viewHolder.itemView;
        ImageView preview = view.findViewById(R.id.grid_albumable_preview);
        TextView label = view.findViewById(R.id.grid_albumable_label);
        label.setText(mCtx.getString(R.string.albumable_label, item.getName(), item.getCount()));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.outWidth = preview.getWidth();
        options.outHeight = preview.getHeight();
        preview.setImageBitmap(mCache.requestImage(item.getPreview(), i, options));
    }

    @Override
    public int getItemCount() {
        if (mAdapterItems == null) {
            return 0;
        }
        return mAdapterItems.size();
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
        if (idx < mAdapterItems.size()) {
            mAdapterItems.remove(idx);
            notifyItemRemoved(idx);
        }
        if (mAdapterItems.isEmpty()) {
            mFrag.notifyEmpty(true);
        }
    }

    public void addAlbum(Albumable album) {
        mAdapterItems.add(album);
        notifyItemInserted(mAdapterItems.size() - 1);
        if (mAdapterItems.size() == 1) {
            mFrag.notifyEmpty(false);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
