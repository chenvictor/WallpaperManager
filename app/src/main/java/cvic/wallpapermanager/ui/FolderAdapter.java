package cvic.wallpapermanager.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.utils.FilterUtils;

public class FolderAdapter extends RecyclerView.Adapter {

    private static final boolean SORT_ALPHABETICAL = false;

    private Activity mActivity;
    private File root;
    private File[] folders = {};

    private LruCache<Integer, Bitmap> imageCache;

    FolderAdapter(Activity activity, String rootPath) {
        mActivity = activity;
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
        imageCache = new LruCache<Integer, Bitmap>((int) Runtime.getRuntime().maxMemory() / (1024 * 8)) {
            @Override
            protected int sizeOf(@NonNull Integer key, @NonNull Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    private void loadBitmap(int index, ImageView imageView, File file) {
        final Bitmap bitmap = getBitmapFromMemCache(index);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_launcher_background);
            BitmapWorkerTask task = new BitmapWorkerTask(imageView, file);
            task.execute(index);
        }
    }

    private void addBitmapToCache(int pos, Bitmap bitmap) {
        if (getBitmapFromMemCache(pos) == null) {
            imageCache.put(pos, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(int pos) {
        return imageCache.get(pos);
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
            loadBitmap(i, folderPreview, images[0]);
        }
        if (folder.equals(root)) {
            folderName.setText(mActivity.getString(R.string.folder_title, "Root", images.length));
        } else {
            folderName.setText(mActivity.getString(R.string.folder_title, folder.getName(), images.length));
        }
    }



    @Override
    public int getItemCount() {
        return folders.length;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view = itemView;
        }
    }

    private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        ImageView mView;
        File mFile;

        BitmapWorkerTask(ImageView view, File file) {
            mView = view;
            mFile = file;
        }

        @Override
        protected Bitmap doInBackground(Integer... integers) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            options.outHeight = 50;
            options.outWidth = 200;
            final Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath(), options);

            addBitmapToCache(integers[0], bitmap);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mView.setImageBitmap(bitmap);
            mView = null;
        }

    }

}
