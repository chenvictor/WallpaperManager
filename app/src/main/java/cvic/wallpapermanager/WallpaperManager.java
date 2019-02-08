package cvic.wallpapermanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import cvic.wallpapermanager.model.ImageFileManager;
import cvic.wallpapermanager.model.albumable.FolderManager;
import cvic.wallpapermanager.model.albumable.TagManager;
import cvic.wallpapermanager.tasks.FetchFolderTask;
import cvic.wallpapermanager.tasks.FetchTagsTask;
import cvic.wallpapermanager.ui.AlbumsFragment;
import cvic.wallpapermanager.ui.SettingsFragment;
import cvic.wallpapermanager.ui.WallpaperFragment;

public class WallpaperManager extends AppCompatActivity implements FetchFolderTask.TaskListener, FetchTagsTask.TaskListener {

    private static final String TAG = "cvic.wpm.main";
    private static final String MANAGERS_LOADED_KEY = "cvic.wpm.key_managers_loaded";
    private boolean loaded = false;

    private static final String[] PAGE_TITLES = {"Wallpaper", "Albums", "Settings"};

    private File externalFilesRoot;
    private TabLayout mTabs;
    private ViewPager mViewPager;

    private WallpaperFragment mWallpaper = new WallpaperFragment();
    private AlbumsFragment mAlbums = new AlbumsFragment();
    private SettingsFragment mSettings = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        externalFilesRoot = getExternalFilesDir(null);
        initPrefs();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        mTabs = findViewById(R.id.appbar_tabs);
        mViewPager = findViewById(R.id.main_pager);
        if (savedInstanceState != null && savedInstanceState.getBoolean(MANAGERS_LOADED_KEY, false)) {
            Log.i(TAG, "Activity rotated, skipping manager loading");
            loaded = true;
            initTabs();
        } else {
            Log.i(TAG, "First creation, loading managers");
            loadFoldersAndTags();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(MANAGERS_LOADED_KEY, loaded);
    }

    private void loadFoldersAndTags() {
        FolderManager.getInstance().clear();
        FolderManager.setRoot(externalFilesRoot);
        TagManager.getInstance().clear();
        ImageFileManager.getInstance().clear();
        new FetchTagsTask(this, externalFilesRoot).execute();
    }

    @Override
    public void onTagsFetched() {
        new FetchFolderTask(this, externalFilesRoot).execute();
    }

    @Override
    public void onFoldersFetched() {
        loaded = true;
        initTabs();
    }

    @SuppressLint("ApplySharedPref")
    private void initPrefs() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //set default root folder
        final String UNINITIALIZED = getString(R.string.uninitialized);
        final String ROOT_KEY = getString(R.string.key_root_folder);
        if (prefs.getString(ROOT_KEY, UNINITIALIZED).equals(UNINITIALIZED)) {
            //initialize as default picture directory
            //using commit because we need to use this value immediately in the fragments
            prefs.edit().putString(ROOT_KEY, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()).commit();
        }
    }

    private void initTabs() {
        mTabs.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                switch (i) {
                    case 0:
                        return mWallpaper;
                    case 1:
                        return mAlbums;
                    case 2:
                        return mSettings;
                }
                throw new UnsupportedOperationException("Invalid fragment index! " + i);
            }

            @Override
            public int getCount() {
                return 3;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return PAGE_TITLES[position];
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == WallpaperFragment.RCODE_ENABLE_WALLPAPER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.confirm_enabled, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Save tag json
        TagManager.getInstance().saveJson(new File(externalFilesRoot, JSON.FILE_TAGS));
        FolderManager.getInstance().saveJson();
        mWallpaper.saveAlbums();
    }
}
