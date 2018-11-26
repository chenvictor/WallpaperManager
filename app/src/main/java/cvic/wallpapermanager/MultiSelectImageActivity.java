package cvic.wallpapermanager;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Set;

import cvic.wallpapermanager.ui.MultiSelectImageAdapter;
import cvic.wallpapermanager.utils.DisplayUtils;

public abstract class MultiSelectImageActivity extends AppCompatActivity implements MultiSelectImageAdapter.MultiSelectListener {

    private static final int GRID_SIZE = 300;
    private static final String TAG = "cvic.wpm.apa";

    Toolbar toolbar;

    private RecyclerView mRecycler;

    MultiSelectImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiselect_image);
        initToolbar();
        mRecycler = findViewById(R.id.recycler);
        mAdapter = getAdapter();
        mAdapter.setSize(GRID_SIZE);
        mRecycler.setAdapter(mAdapter);
        int colSpan = DisplayUtils.getDisplayWidth(this) / GRID_SIZE;
        mRecycler.setLayoutManager(new GridLayoutManager(this, colSpan));
        mRecycler.setHasFixedSize(true);
    }

    private void initToolbar () {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getDefaultTitle());
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        assert bar != null;
        bar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.flushCache();
    }

    @Override
    public void onSelectionChanged(Set<File> selections) {
        if (selections.isEmpty()) {
            toolbar.setTitle(getDefaultTitle());
        } else {
            toolbar.setTitle(selections.size() + " Selected");
        }
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (mAdapter.isMultiselect()) {
            multiselectBasicMenuOptions(menu);
            multiselectMenuOptions(menu);
        } else {
            defaultBasicMenuOptions(menu);
            defaultMenuOptions(menu);
        }
        return true;
    }

    private void defaultBasicMenuOptions(Menu menu) {
        if (mAdapter.getItemCount() > 0) {
            menu.add("Select All").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    mAdapter.selectAll();
                    return true;
                }
            });
        }
    }

    private void multiselectBasicMenuOptions(Menu menu) {
        menu.add("Select All").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mAdapter.selectAll();
                return true;
            }
        });
        menu.add("Select None").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mAdapter.clearSelections();
                return true;
            }
        });
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
        if (mAdapter.onBackPressed()) {
            // Handled by adapter
            mRecycler.scrollToPosition(0);
        } else {
            super.onBackPressed();
        }
    }

    protected abstract String getDefaultTitle();
    protected abstract MultiSelectImageAdapter getAdapter();
    protected abstract void defaultMenuOptions(Menu menu);
    protected abstract void multiselectMenuOptions(Menu menu);

}
