package cvic.wallpapermanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.model.Folder;
import cvic.wallpapermanager.tasks.AddImagesTask;
import cvic.wallpapermanager.ui.DirectoryAdapter;
import cvic.wallpapermanager.utils.DisplayUtils;

public class AddImagesActivity extends AppCompatActivity implements DirectoryAdapter.AdapterListener, AddImagesTask.TaskListener {

    private static final int GRID_SIZE = 300;

    private String destinationPath;
    private Albumable album;

    private Toolbar toolbar;
    private MenuItem addBtn;

    private RecyclerView mRecycler;
    private DirectoryAdapter mAdapter;

    private AddImagesTask.TaskListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_images);
        destinationPath = getIntent().getStringExtra(Folder.EXTRA_DEST_PATH);
        File file = getRootPath();
        if (file == null) {
            Toast.makeText(this, "Root path invalid!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initToolbar();
        mRecycler = findViewById(R.id.recycler);
        int colSpan = DisplayUtils.getDisplayWidth(this) / GRID_SIZE;
        mRecycler.setLayoutManager(new GridLayoutManager(this, colSpan));
        mAdapter = new DirectoryAdapter(this, this, file);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setHasFixedSize(true);
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Add Images");
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mAdapter.navigateBack()) {
            super.onBackPressed();
        }
    }

    @Override
    public void singleSelected(File file) {
        Set<File> set = new HashSet<>();
        set.add(file);
        multiSelected(set);
    }

    public void multiSelected(Set<File> files) {
        AddImagesTask task = new AddImagesTask(this, new File(destinationPath));
        File[] array = new File[files.size()];
        array = files.toArray(array);
        task.execute(array);
    }

    @Override
    public void multiSelectionChanged(int count) {
        if (count == 0) {
            toolbar.setTitle("Add Images");
            addBtn.setVisible(false);
        } else {
            toolbar.setTitle(String.valueOf(count) + " selected");
            addBtn.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        addBtn = menu.add(getString(R.string.confirm));
        addBtn.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        addBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                multiSelected(mAdapter.getSelection());
                return true;
            }
        });
        addBtn.setVisible(false);
        return true;
    }

    @Override
    public void onTaskComplete() {
        setResult(RESULT_OK);
        finish();
    }
}
