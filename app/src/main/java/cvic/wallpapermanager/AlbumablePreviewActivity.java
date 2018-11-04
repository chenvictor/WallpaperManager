package cvic.wallpapermanager;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.ui.ImageAdapter;
import cvic.wallpapermanager.utils.DisplayUtils;

public class AlbumablePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_ALBUM_PARCEL = "cvic.wpm.eap";

    private static final int GRID_SIZE = 300;

    private Albumable album;

    private RecyclerView mRecycler;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albumable_preview);
        Parcelable parcelable = getIntent().getParcelableExtra(EXTRA_ALBUM_PARCEL);
        if (parcelable == null) {
            errorNoParcelable();
            return;
        }
        album = (Albumable) parcelable;
        initToolbar(album);
        mRecycler = findViewById(R.id.recycler);
        int colSpan = DisplayUtils.getDisplayWidth(this) / GRID_SIZE;
        mRecycler.setLayoutManager(new GridLayoutManager(this, colSpan));
        mAdapter = new ImageAdapter(this, album);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setHasFixedSize(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem addBtn = menu.add("Add");
        addBtn.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        addBtn.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                album.addImage();
                return true;
            }
        });
        return true;
    }

    private void initToolbar(Albumable parcelable) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.folder_title, parcelable.getName(), parcelable.getCount()));
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
    }

    private void errorNoParcelable() {
        Toast.makeText(this, "Invalid Parcelable passed.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
