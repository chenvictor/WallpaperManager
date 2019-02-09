package cvic.wallpapermanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cvic.wallpapermanager.ui.MultiSelectImageAdapter;
import cvic.wallpapermanager.utils.FilterUtils;

import static cvic.wallpapermanager.SingleImageActivity.IMAGE_PATH;

public class SelectImagesActivity extends MultiSelectImageActivity {

    public static final String EXTRA_IMAGES = "cvic.wpm.selected_images_extra";
    public static final String EXTRA_ROOT = "cvic.wpm.section_root_extra";

    public static final int RCODE_ADD = 29;
    private File rootPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().hasExtra(EXTRA_ROOT)) {
            rootPath = new File(getIntent().getStringExtra(EXTRA_ROOT));
        } else {
            //defaults to shared preference root path
            rootPath = getRootPath();
            if (rootPath == null) {
                Toast.makeText(this, "Root path invalid!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected MultiSelectImageAdapter getAdapter() {
        return new DirectoryAdapter(this, this, rootPath);
    }

    private File getRootPath() {
        final String UN_INIT = getString(R.string.uninitialized);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String path = prefs.getString(getString(R.string.key_root_folder), UN_INIT);
        if (path.equals(UN_INIT)) {
            return null;
        }
        return new File(path);
    }

    @Override
    public void onSingleImageClick(File file) {
        Intent intent = new Intent(this, SelectImagesIVA.class);
        intent.putExtra(IMAGE_PATH, file.getAbsolutePath());
        startActivityForResult(intent, RCODE_ADD);
    }

    private void addImages(Set<File> files) {
        String[] result = new String[files.size()];
        int idx = 0;
        for (File file : files) {
            result[idx++] = file.getAbsolutePath();
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_IMAGES, result);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected String getDefaultTitle() {
        return "Select Images";
    }

    @Override
    public void defaultMenuOptions(Menu menu) {

    }

    @Override
    protected void multiselectMenuOptions(Menu menu) {
        menu.add("Select Images").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                addImages(mAdapter.getSelections());
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RCODE_ADD) {
            if (resultCode == RESULT_OK && data != null) {
                String path = data.getStringExtra(IMAGE_PATH);
                Set<File> set = new HashSet<>();
                set.add(new File(path));
                addImages(set);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    static class DirectoryAdapter extends MultiSelectImageAdapter {
        private final File root;
        private File current;
        private File[] files = {};

        DirectoryAdapter(MultiSelectListener listener, Context ctx, File root) {
            super(listener, ctx, true);
            this.root = root;
            setPath(root);
        }

        private void setPath(File file) {
            current = file;
            files = current.listFiles(FilterUtils.get(FilterUtils.EITHER));
            notifyDataSetChanged();
        }

        @Override
        public void directoryClicked(File file) {
            setPath(file);
        }

        @Override
        public File getFile(int i) {
            return files[i];
        }

        @Override
        public boolean onBackPressed() {
            if (current.equals(root)) {
                return false;
            }
            setPath(current.getParentFile());
            return true;
        }

        @Override
        public int getItemCount() {
            return files.length;
        }
    }
}
