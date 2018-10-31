package cvic.wallpapermanager.ui;

import android.app.Activity;
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
import java.util.Arrays;
import java.util.Comparator;

import cvic.wallpapermanager.ImageCache;
import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.FilterUtils;

public class FolderAdapter extends RecyclerView.Adapter implements ImageCache.CacheListener {

    private static final boolean SORT_ALPHABETICAL = false;

    private Activity mActivity;

    private File root;
    private File[] folders = {};

    private ImageCache mCache;

    FolderAdapter(Activity activity, String rootPath) {
        mActivity = activity;
        mCache = new ImageCache(this);
        if (rootPath != null) {
            root = new File(rootPath);
            folders = root.listFiles(FilterUtils.get(FilterUtils.FOLDER));
            if (FilterUtils.containsImages(root)) {
                folders = Arrays.copyOf(folders, folders.length + 1);
                folders[folders.length - 1] = root;
            }
            Arrays.sort(folders, new Comparator<File>() {
                @Override
                public int compare(File file, File t1) {
                    if (file.equals(root)) {
                        return -1;
                    }
                    if (t1.equals(root)) {
                        return 1;
                    }
                    if (SORT_ALPHABETICAL) {
                        return file.getName().compareToIgnoreCase(t1.getName());
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.card_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        View view = viewHolder.itemView;
        TextView folderName = view.findViewById(R.id.folder_name);
        ImageView folderPreview = view.findViewById(R.id.folder_preview);
        File folder = folders[i];
        File[] images = folder.listFiles(FilterUtils.get(FilterUtils.IMAGE));
        if (images.length > 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            options.outWidth = folderPreview.getWidth();
            options.outHeight = folderPreview.getHeight();
            folderPreview.setImageBitmap(mCache.requestImage(images[0], i, options));
        }
        if (folder.equals(root)) {
            folderName.setText(mActivity.getString(R.string.folder_title, "default", images.length));
        } else {
            folderName.setText(mActivity.getString(R.string.folder_title, folder.getName(), images.length));
        }
    }



    @Override
    public int getItemCount() {
        return folders.length;
    }

    @Override
    public void onBitmapAvailable(int requestId, Bitmap bitmap) {
        notifyItemChanged(requestId);   //this will refresh the associated viewholder... I think?
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

}
