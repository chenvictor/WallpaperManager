package cvic.wallpapermanager;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import cvic.wallpapermanager.ui.AlbumsFragment;
import cvic.wallpapermanager.ui.SettingsFragment;
import cvic.wallpapermanager.ui.WallpaperFragment;

public class WallpaperManager extends AppCompatActivity implements SettingsFragment.RootChangeListener {

    private static final String[] PAGE_TITLES = {"Wallpaper", "Albums", "Settings"};

    private TabLayout mTabs;
    private ViewPager mViewPager;

    private WallpaperFragment mWallpaper = new WallpaperFragment();
    private AlbumsFragment mAlbums = new AlbumsFragment();
    private SettingsFragment mSettings = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        mSettings.setListener(this);

        mTabs = findViewById(R.id.appbar_tabs);
        mViewPager = findViewById(R.id.main_pager);
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
    public void rootChanged(String path) {
        mAlbums.rootChanged(path);
    }
}
