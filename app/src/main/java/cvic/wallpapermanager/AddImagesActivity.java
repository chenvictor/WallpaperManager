package cvic.wallpapermanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cvic.wallpapermanager.model.Folder;
import cvic.wallpapermanager.tasks.AddImagesTask;
import cvic.wallpapermanager.ui.MultiSelectImageAdapter;
import cvic.wallpapermanager.utils.FilterUtils;

public class AddImagesActivity extends MultiSelectImageActivity implements AddImagesTask.TaskListener {

    private String destinationPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        destinationPath = getIntent().getStringExtra(Folder.EXTRA_DEST_PATH);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected MultiSelectImageAdapter getAdapter() {
        File root = getRootPath();
        if (root == null) {
            Toast.makeText(this, "Root path invalid!", Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        return new DirectoryAdapter(this, this, root);
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
        Set<File> set = new HashSet<>();
        set.add(file);
        addImages(set);
    }

    private void addImages(Set<File> files) {
        AddImagesTask task = new AddImagesTask(this, new File(destinationPath));
        File[] array = new File[files.size()];
        array = files.toArray(array);
        task.execute(array);
        setLoading("Adding Images");
    }

    @Override
    protected String getDefaultTitle() {
        return "Add Images";
    }

    @Override
    public void defaultMenuOptions(Menu menu) {

    }

    @Override
    protected void multiselectMenuOptions(Menu menu) {
        menu.add("Add Images").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                addImages(mAdapter.getSelections());
                return true;
            }
        });
    }

    @Override
    public void onTaskComplete() {
        doneLoading();
        setResult(RESULT_OK);
        finish();
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
            flushCache();
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
