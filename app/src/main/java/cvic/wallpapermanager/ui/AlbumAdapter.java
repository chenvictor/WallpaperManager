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

import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.utils.ImageCache;

public class AlbumAdapter extends Adapter implements ImageCache.CacheListener {

    private Toast mToast;
    private final Context mCtx;

    private List<Albumable> mAdapterItems;
    private ImageCache mCache;

    AlbumAdapter(Context ctx) {
        mCtx = ctx;
        mCache = new ImageCache(this);
    }

    void setAdapterItems(List<Albumable> AdapterItems) {
        mAdapterItems = AdapterItems;
        //flush the image cache
        mCache.flush();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_albumable, viewGroup, false);

        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapterItems.get(holder.getAdapterPosition()).onClick();
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mAdapterItems.get(holder.getAdapterPosition()).onLongClick();
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        Albumable item = mAdapterItems.get(i);
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

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
