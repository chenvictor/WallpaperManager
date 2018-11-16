package cvic.wallpapermanager;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.DialogInterface;
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
import android.widget.Toast;

import cvic.wallpapermanager.service.WPMService;
import cvic.wallpapermanager.ui.AlbumsFragment;
import cvic.wallpapermanager.ui.SettingsFragment;
import cvic.wallpapermanager.ui.WallpaperFragment;

public class WallpaperManager extends AppCompatActivity {

    private static final int RCODE_ENABLE_WALLPAPER = 1234;
    private static final String[] PAGE_TITLES = {"Wallpaper", "Albums", "Settings"};

    private TabLayout mTabs;
    private ViewPager mViewPager;

    private WallpaperFragment mWallpaper = new WallpaperFragment();
    private AlbumsFragment mAlbums = new AlbumsFragment();
    private SettingsFragment mSettings = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPrefs();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        mTabs = findViewById(R.id.appbar_tabs);
        mViewPager = findViewById(R.id.main_pager);
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
    protected void onPostCreate(Bundle savedInstanceState) {
        if (!wallpaperSet()) {
            requestWallpaperSet();
        }
        super.onPostCreate(savedInstanceState);
    }

    private boolean wallpaperSet() {
        android.app.WallpaperManager wpm = android.app.WallpaperManager.getInstance(this);
        WallpaperInfo info = wpm.getWallpaperInfo();
        if (info == null) {
            return false;
        }
        return (info.getComponent().getClassName().equals(WPMService.class.getName()));
    }

    private void requestWallpaperSet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
                .setMessage(R.string.request_enabled)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        sendIntent();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    private void sendIntent() {
        Intent intent = new Intent();
        intent.setAction(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(getBaseContext(), WPMService.class));
        startActivityForResult(intent, RCODE_ENABLE_WALLPAPER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RCODE_ENABLE_WALLPAPER) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.confirm_enabled, Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
