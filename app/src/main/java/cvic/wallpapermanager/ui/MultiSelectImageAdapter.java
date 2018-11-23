package cvic.wallpapermanager.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.FilterUtils;
import cvic.wallpapermanager.utils.ImageCache;

public abstract class MultiSelectImageAdapter extends RecyclerView.Adapter<MultiSelectImageAdapter.ViewHolder> implements ImageCache.CacheListener {

    private final String TAG = "cvic.wpm.msia";

    private int size = 300;

    private final MultiSelectListener listener;
    private final Context ctx;

    /**
     * Whether or not image names should be shown. Directory names are always shown.
     */
    private final boolean showName;

    private boolean multiselect;
    private Set<File> selections;

    private ImageCache cache;

    public MultiSelectImageAdapter(MultiSelectListener listener, Context ctx) {
        this (listener, ctx, true);
    }

    public MultiSelectImageAdapter(MultiSelectListener listener, Context ctx, boolean showName) {
        this.ctx = ctx;
        this.listener = listener;
        this.showName = showName;
        multiselect = false;
        selections = new HashSet<>();
        cache = new ImageCache(this);
    }

    public final void setSize(int size) {
        this.size = size;
    }

    @NonNull
    @Override
    public final ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_preview_albumable, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiSelectImageAdapter.this.onClick(holder);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return MultiSelectImageAdapter.this.onLongClick(holder);
            }
        });
        return holder;
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.i(TAG, "Recycling: " + holder.getAdapterPosition());
        cache.cancelRequest(holder.getAdapterPosition());
//        if (!cache.isCached(holder.getAdapterPosition())) {
//            try {
//                Bitmap bitmap = ((BitmapDrawable) holder.image.getDrawable()).getBitmap();
//                bitmap.recycle(); // TODO recycle bitmaps not in use
//            } catch (ClassCastException ignored) {}
//        }
    }

    @Override
    public final void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        ImageView image = viewHolder.image;
        TextView label = viewHolder.label;
        Group overlay = viewHolder.overlay;
        File file = getFile(i);
        if (file.isDirectory()) {
            image.setImageResource(R.drawable.folder_outline);
            int count = file.listFiles(FilterUtils.get(FilterUtils.IMAGE)).length;
            label.setText(ctx.getResources().getQuantityString(R.plurals.folder_title_plural, count, file.getName(), count));
            overlay.setVisibility(View.INVISIBLE);
        } else {
            if (multiselect && selections.contains(file)) {
                overlay.setVisibility(View.VISIBLE);
            } else {
                overlay.setVisibility(View.INVISIBLE);
            }
            image.setImageBitmap(cache.requestImage(file, i, size, size));
            if (showName) {
                label.setText(file.getName());
                label.setVisibility(View.VISIBLE);
            } else {
                label.setVisibility(View.GONE);
            }
        }
    }

    private void onClick(ViewHolder holder) {
        int idx = holder.getAdapterPosition();
        File file = getFile(idx);
        if (file.isDirectory()) {
            //Dir, navigate to
            directoryClicked(file);
        } else {
            //Image, select or add
            if (multiselect) {
                if (!selections.add(file)) {
                    selections.remove(file);
                    if (selections.isEmpty()) {
                        multiselect = false;
                        notifyDataSetChanged();
                    }
                }
                onSelectionChanged();
                notifyItemChanged(idx);
            } else {
                onSingleImageClick(file);
            }
        }
    }

    private boolean onLongClick(ViewHolder holder) {
        int idx = holder.getAdapterPosition();
        File file = getFile(idx);
        if (file.isDirectory()) {
            //Dir doesn't long click
            return false;
        } else {
            //Image, select or add
            if (multiselect) {
                return false;
            } else {
                multiselect = true;
                selections.clear();
                selections.add(file);
                onSelectionChanged();
                notifyItemChanged(idx);
                return true;
            }
        }
    }

    public boolean isMultiselect() {
        return multiselect;
    }

    public final Set<File> getSelections() {
        return selections;
    }

    public final void selectAll() {
        if (getItemCount() == 0) {
            return;
        }
        multiselect = true;
        boolean changed = false;
        for (int i = 0; i < getItemCount(); i++) {
            File file;
            if (!(file = getFile(i)).isDirectory()) {
                if (selections.add(file)) {
                    changed = true;
                    notifyItemChanged(i);
                }
            }
        }
        if (changed) {
            onSelectionChanged();
        }
    }

    public final void clearSelections() {
        multiselect = false;
        selections.clear();
        onSelectionChanged();
        notifyDataSetChanged();
    }

    public final void flushCache() {
        cache.flush();
    }

    @Override
    public final void onBitmapAvailable(int requestId, Bitmap bitmap) {
        notifyItemChanged(requestId);
    }

    protected abstract void directoryClicked(File file);
    protected abstract File getFile(int i);
    public abstract boolean onBackPressed();

    private void onSingleImageClick(File file) {
        listener.onSingleImageClick(file);
    }
    private void onSelectionChanged() {
        listener.onSelectionChanged(selections);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView label;
        Group overlay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.grid_albumable_image);
            label = itemView.findViewById(R.id.grid_albumable_label);
            overlay = itemView.findViewById(R.id.checked_overlay);
        }
    }

    public interface MultiSelectListener {

        void onSingleImageClick(File file);
        void onSelectionChanged(Set<File> selections);

    }

}
