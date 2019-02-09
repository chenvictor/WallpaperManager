package cvic.wallpapermanager.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cvic.wallpapermanager.ImageViewHolder;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.FilterUtils;

public abstract class MultiSelectImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {

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

    public MultiSelectImageAdapter(MultiSelectListener listener, Context ctx) {
        this (listener, ctx, true);
    }

    public MultiSelectImageAdapter(MultiSelectListener listener, Context ctx, boolean showName) {
        this.ctx = ctx;
        this.listener = listener;
        this.showName = showName;
        multiselect = false;
        selections = new HashSet<>();
    }

    public final void setSize(int size) {
        this.size = size;
    }

    @NonNull
    @Override
    public final ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_preview_albumable, viewGroup, false);
        final ImageViewHolder holder = new ImageViewHolder(view);
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
    public final void onBindViewHolder(@NonNull ImageViewHolder holder, int i) {
        ImageView image = holder.getImage();
        TextView label = holder.getLabel();
        Group overlay = holder.getOverlay();
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
            if (showName) {
                label.setText(file.getName());
                label.setVisibility(View.VISIBLE);
            } else {
                label.setVisibility(View.GONE);
            }
            Glide.with(ctx).load(file).into(image);
        }
    }

    private void onClick(ImageViewHolder holder) {
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

    private boolean onLongClick(ImageViewHolder holder) {
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

    protected abstract void directoryClicked(File file);
    protected abstract File getFile(int i);
    public abstract boolean onBackPressed();

    private void onSingleImageClick(File file) {
        listener.onSingleImageClick(file);
    }
    private void onSelectionChanged() {
        listener.onSelectionChanged(selections);
    }

    public interface MultiSelectListener {

        void onSingleImageClick(File file);
        void onSelectionChanged(Set<File> selections);

    }

}
