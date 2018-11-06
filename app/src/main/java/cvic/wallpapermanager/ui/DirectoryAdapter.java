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
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.FilterUtils;
import cvic.wallpapermanager.utils.ImageCache;

public class DirectoryAdapter extends RecyclerView.Adapter implements ImageCache.CacheListener {

    private AdapterListener listener;
    private final Context mCtx;
    private final File root;
    private File current;

    private boolean multiselect;
    private Set<File> selections;

    private ImageCache cache;

    private File[] files;

    public DirectoryAdapter(Context ctx, AdapterListener listener, File root) {
        this.listener = listener;
        mCtx = ctx;
        this.root = root;
        multiselect = false;
        selections = new HashSet<>();
        cache = new ImageCache(this);
        setPath(root);
    }

    /**
     * Navigate to the parent of the current file,
     *  moving at most to the root
     * @return      true if navigation successful, false otherwise
     */
    public boolean navigateBack() {
        if (current.equals(root)) {
            return false;
        }
        setPath(current.getParentFile());
        return true;
    }

    public Set<File> getSelection() {
        return selections;
    }

    private void setPath(File file) {
        current = file;
        files = current.listFiles(FilterUtils.get(FilterUtils.EITHER));
        cache.flush();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_preview_albumable, null, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileClicked(holder);
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return fileLongClicked(holder);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        View view = viewHolder.itemView;
        ImageView image = view.findViewById(R.id.grid_albumable_preview);
        TextView text = view.findViewById(R.id.grid_albumable_label);
        File file = files[i];
        if (file.isDirectory()) {
            image.setImageResource(R.drawable.folder_icon);
            int count = file.listFiles(FilterUtils.get(FilterUtils.IMAGE)).length;
            text.setText(mCtx.getString(R.string.albumable_label, file.getName(), count));
            image.setImageAlpha(255);
        } else {
            if (multiselect) {
                if (selections.contains(file)) {
                    image.setImageAlpha(255);
                } else {
                    image.setImageAlpha(100);
                }
            } else {
                image.setImageAlpha(255);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.outWidth = view.getWidth();
            options.outHeight = view.getHeight();
            image.setImageBitmap(cache.requestImage(file, i, options));
            text.setText(file.getName());
        }
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    private void fileClicked(ViewHolder holder) {
        int idx = holder.getAdapterPosition();
        File file = files[idx];
        if (file.isDirectory()) {
            //Dir, navigate to
            setPath(file);
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
                listener.multiSelectionChanged(selections.size());
                notifyItemChanged(idx);
            } else {
                listener.singleSelected(file);
            }
        }
    }

    private boolean fileLongClicked(ViewHolder holder) {
        int idx = holder.getAdapterPosition();
        File file = files[idx];
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
                listener.multiSelectionChanged(selections.size());
                notifyDataSetChanged();
                return true;
            }
        }
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

    public interface AdapterListener {

        void singleSelected(File file);
        void multiSelectionChanged(int count);

    }

}
