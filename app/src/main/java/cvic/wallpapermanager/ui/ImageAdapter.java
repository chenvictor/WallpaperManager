package cvic.wallpapermanager.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.utils.ImageCache;

public class ImageAdapter extends RecyclerView.Adapter implements ImageCache.CacheListener {

    private Context mCtx;
    private Albumable mAlbum;

    private ImageCache cache;

    public ImageAdapter(Context ctx, Albumable album) {
        mCtx = ctx;
        mAlbum = album;
        cache = new ImageCache(this);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.layout_preview_image, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int idx) {
        ImageView view = viewHolder.itemView.findViewById(R.id.image_preview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.outWidth = view.getWidth();
        options.outHeight = view.getHeight();
        view.setImageBitmap(cache.requestImage(mAlbum.getImage(idx), idx, options));
    }

    @Override
    public int getItemCount() {
        if (mAlbum == null) {
            return 0;
        }
        return mAlbum.getCount();
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
