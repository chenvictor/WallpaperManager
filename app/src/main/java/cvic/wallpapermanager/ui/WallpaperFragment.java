package cvic.wallpapermanager.ui;


import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.SelectImagesActivity;
import cvic.wallpapermanager.dialogs.ContextMenuDialog;
import cvic.wallpapermanager.dialogs.FolderSelectDialog;
import cvic.wallpapermanager.dialogs.TagSelectDialog;
import cvic.wallpapermanager.model.album.Album;
import cvic.wallpapermanager.model.album.AlbumFactory;
import cvic.wallpapermanager.model.album.ImageAlbum;
import cvic.wallpapermanager.service.WPMService;
import cvic.wallpapermanager.utils.ImageCache;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallpaperFragment extends Fragment implements CheckBox.OnCheckedChangeListener, TextView.OnEditorActionListener, AdapterView.OnItemSelectedListener, View.OnClickListener, ImageCache.CacheListener {

    private static final String TAG = "cvic.wpm.wp_frag";

    public static final int RCODE_ENABLE_WALLPAPER = 1234;

    private static final int RCODE_SELECT_FOLDER = 100;
    private static final int RCODE_SELECT_TAG = 101;
    private static final int RCODE_SELECT_IMAGE = 102;

    private SharedPreferences mPrefs;
    private boolean editEnabled;

    // Enable alert
    private CardView mEnableCard;
    private Button mEnableButton;

    // General screen views
    private CheckBox mIntervalEnabledCheckBox;
    private EditText mIntervalValue;
    private Spinner mIntervalSpinner;

    // Home screen views
    private CardView mHomeAlbum;
    private ImageView mHomeAlbumPreview;
    private TextView mHomeAlbumDesc;
    private CheckBox mHomeDoubleTapCheckBox;

    // Lock screen views
    private Switch mLockUseHomeSwitch;
    private CardView mLockAlbum;
    private ImageView mLockAlbumPreview;
    private TextView mLockAlbumDesc;
    private CheckBox mLockBlurCheckBox;
    private CheckBox mLockNotificationCheckBox;

    // Albums
    private Album homeAlbum;
    private Album lockAlbum;

    private ImageCache cache;

    public WallpaperFragment() {
        // Required empty public constructor
        cache = new ImageCache(this, 2);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_wallpaper, container, false);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        findViews(root);
        setListeners();
        setPrefs();

        fetchAlbums();
        syncAlbums();

        return root;
    }

    private void findViews(View root) {

        mEnableCard = root.findViewById(R.id.card_enable);
        mEnableButton = mEnableCard.findViewById(R.id.button_enable);

        View general = root.findViewById(R.id.card_general);
        mIntervalEnabledCheckBox = general.findViewById(R.id.interval_checkbox);
        mIntervalValue = general.findViewById(R.id.interval_value);
        mIntervalSpinner = general.findViewById(R.id.interval_spinner);

        View home = root.findViewById(R.id.card_home);
        mHomeAlbum = home.findViewById(R.id.card_home_album);
        mHomeAlbumPreview = mHomeAlbum.findViewById(R.id.preview_image);
        mHomeAlbumDesc = mHomeAlbum.findViewById(R.id.preview_desc);
        mHomeDoubleTapCheckBox = home.findViewById(R.id.checkbox_home_double_tap_enabled);

        View lock = root.findViewById(R.id.card_lock);
        mLockUseHomeSwitch = lock.findViewById(R.id.lock_use_home_switch);
        mLockAlbum = lock.findViewById(R.id.card_lock_album);
        mLockAlbumPreview = mLockAlbum.findViewById(R.id.preview_image);
        mLockAlbumDesc = mLockAlbum.findViewById(R.id.preview_desc);
        mLockBlurCheckBox = lock.findViewById(R.id.lock_blur_checkbox);
        mLockNotificationCheckBox = lock.findViewById(R.id.lock_notification_checkbox);
    }

    private void setListeners() {

        // Alert listener
        mEnableButton.setOnClickListener(this);

        // General listeners
        mIntervalEnabledCheckBox.setOnCheckedChangeListener(this);
        mIntervalValue.setOnEditorActionListener(this);
        mIntervalSpinner.setOnItemSelectedListener(this);

        // Home listeners
        mHomeAlbum.setOnClickListener(this);
        mHomeDoubleTapCheckBox.setOnCheckedChangeListener(this);

        // Lock listeners
        mLockUseHomeSwitch.setOnCheckedChangeListener(this);
        mLockAlbum.setOnClickListener(this);
        mLockBlurCheckBox.setOnCheckedChangeListener(this);
        mLockNotificationCheckBox.setOnCheckedChangeListener(this);
    }

    private void setPrefs() {
        editEnabled = false;    // lock editing until initialization is done

        // General
        mIntervalEnabledCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_interval_enabled), true));
        mIntervalValue.setText(String.valueOf(mPrefs.getInt(getString(R.string.key_wallpaper_interval_value), 1)));
        mIntervalSpinner.setSelection(mPrefs.getInt(getString(R.string.key_wallpaper_interval_type), 0));

        // Home
        mHomeDoubleTapCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_home_double_tap_enabled), false));

        // Lock
        mLockUseHomeSwitch.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_lock_use_home), true));
        mLockNotificationCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_lock_notification), false));
        mLockBlurCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_lock_blur), false));

        editEnabled = true;     // unlock editing
    }

    // Fetch the albums, and update the preview image and descriptions
    private void fetchAlbums() {
        homeAlbum = AlbumFactory.create(mPrefs.getString(getString(R.string.key_wallpaper_home_album), null));
        lockAlbum = AlbumFactory.create(mPrefs.getString(getString(R.string.key_wallpaper_lock_album), null));
    }

    private void syncAlbums() {
        mHomeAlbumDesc.setText(homeAlbum.getName());
        mLockAlbumDesc.setText(lockAlbum.getName());
        cache.flush();  //
        if (homeAlbum.getPreview() != null) {
            mHomeAlbumPreview.setImageBitmap(cache.requestImage(homeAlbum.getPreview(), 0, mHomeAlbumPreview.getMeasuredWidth(), mHomeAlbumPreview.getMeasuredHeight()));
        } else {
            mHomeAlbumPreview.setImageBitmap(null);
        }
        if (lockAlbum.getPreview() != null) {
            mLockAlbumPreview.setImageBitmap(cache.requestImage(homeAlbum.getPreview(), 0, mLockAlbumPreview.getMeasuredWidth(), mLockAlbumPreview.getMeasuredHeight()));
        } else {
            mLockAlbumPreview.setImageBitmap(null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEnableCard != null) {
            if (wallpaperSet()) {
                mEnableCard.setVisibility(View.GONE);
            } else {
                mEnableCard.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mIntervalEnabledCheckBox.equals(compoundButton)) {
            intervalChecked(b);
        } else if (mLockUseHomeSwitch.equals(compoundButton)) {
            lockUseHomeSwitched(b);
        } else if (mHomeDoubleTapCheckBox.equals(compoundButton)) {
            homeDoubleTapChecked(b);
        } else if (mLockBlurCheckBox.equals(compoundButton)) {
            lockBlurChecked(b);
        } else if (mLockNotificationCheckBox.equals(compoundButton)) {
            lockNotificationChecked(b);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (mIntervalValue.equals(textView)) {
            if (actionId == EditorInfo.IME_NULL ||
                    actionId == EditorInfo.IME_ACTION_SEND ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                String value = textView.getText().toString();
                if (value.length() > 0) {
                    intervalSet(Integer.parseInt(textView.getText().toString()));
                } else {
                    final int DEFAULT_INTERVAL = 1;
                    textView.setText(String.valueOf(DEFAULT_INTERVAL));
                    intervalSet(DEFAULT_INTERVAL);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (mIntervalSpinner.equals(adapterView)) {
            intervalTypeSet(i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Do nothing
    }

    @Override
    public void onClick(View view) {
        if (mHomeAlbum.equals(view)) {
            changeAlbum(0);
        } else if (mLockAlbum.equals(view)) {
            changeAlbum(1);
        } else if (mEnableButton.equals(view)) {
            sendIntent();
        }
    }

    private void sendIntent() {
        if (getContext() != null) {
            Intent intent = new Intent();
            intent.setAction(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, new ComponentName(getContext(), WPMService.class));
            startActivityForResult(intent, RCODE_ENABLE_WALLPAPER);
        }
    }

    /**
     * Handle checking of wallpaper interval checkbox,
     *  enabling/disabling associated views and
     *  updating the pref if necessary
     * @param b         new checked state
     */
    private void intervalChecked(boolean b) {
        if (editEnabled) {
            mPrefs.edit().putBoolean(getString(R.string.key_wallpaper_interval_enabled), b).apply();
        }
        mIntervalValue.setEnabled(b);
        mIntervalSpinner.setEnabled(b);
    }

    /**
     * Handle switching of lock use home switch,
     *  enabling/disabling associated views and
     *  updating the pref if necessary
     * @param b         new checked state
     */
    private void lockUseHomeSwitched(boolean b) {
        if (editEnabled) {
            mPrefs.edit().putBoolean(getString(R.string.key_wallpaper_lock_use_home), b).apply();
        }
        mLockAlbum.setClickable(!b);
        mLockAlbum.setAlpha(b ? 0.4f : 1f);
    }

    /**
     * Handling setting of interval value,
     *  closing the keyboard and updating the pref if necessary
     * @param value     new interval value
     */
    private void intervalSet(int value) {
        if (editEnabled) {
            mPrefs.edit().putInt(getString(R.string.key_wallpaper_interval_value), value).apply();
        }
        // un focus
        mIntervalValue.clearFocus();
        //close the keyboard
        try {
            Context ctx = getContext();
            if (ctx != null) {
                InputMethodManager manager = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.hideSoftInputFromWindow(mIntervalValue.getWindowToken(), 0);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Handle settings of interval type spinner,
     *  updating the pref is necessary
     * @param idx       new spinner idx
     */
    private void intervalTypeSet(int idx) {
        if (editEnabled) {
            mPrefs.edit().putInt(getString(R.string.key_wallpaper_interval_type), idx).apply();
        }
    }

    /**
     * Handle checking of home double tap checkbox,
     *  updating the pref if necessary
     * @param b         new checked state
     */
    private void homeDoubleTapChecked(boolean b) {
        if (editEnabled) {
            mPrefs.edit().putBoolean(getString(R.string.key_wallpaper_home_double_tap_enabled), b).apply();
        }
    }

    /**
     * Handle checking of lock blur checkbox,
     *  updating the pref if necessary
     * @param b         new checked state
     */
    private void lockBlurChecked(boolean b) {
        if (editEnabled) {
            mPrefs.edit().putBoolean(getString(R.string.key_wallpaper_lock_blur), b).apply();
        }
    }

    /**
     * Handle checking of lock notification checkbox,
     *  updating the pref if necessary
     * @param b         new checked state
     */
    private void lockNotificationChecked(boolean b) {
        if (editEnabled) {
            mPrefs.edit().putBoolean(getString(R.string.key_wallpaper_lock_notification), b).apply();
        }
    }

    /**
     * Initiates an album change
     * @param id    0 for Home screen
     *              1 for Lock screen
     */
    private void changeAlbum(int id) {
        ContextMenuDialog dialog = new ContextMenuDialog(getContext(), "Album Type");
        dialog.addButton("Folder", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Folder selected");
                new FolderSelectDialog(getContext(), new FolderSelectDialog.ResultListener() {
                    @Override
                    public void onSelected(String[] folders) {
                        Log.i(TAG, "Folders selected! TODO");
                        for (String f : folders) {
                            Log.i(TAG, "folder: " + f);
                        }
                    }
                }).show();
            }
        });
        dialog.addButton("Tag", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Tag selected");
                new TagSelectDialog(getContext(), new TagSelectDialog.ResultListener() {
                    @Override
                    public void onSelected(String[] include, String[] exclude) {
                        Log.i(TAG, "Tags selected! TODO");
                        for (String s : include) {
                            Log.i(TAG, "+" + s);
                        }
                        for (String s : exclude) {
                            Log.i(TAG, "-" + 1);
                        }
                    }
                }).show();
            }
        });
        dialog.addButton("Image", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Image selected");
                Intent intent = new Intent(getContext(), SelectImagesActivity.class);
                Context ctx = getContext();
                assert (ctx != null);
                File externalDir = ctx.getExternalFilesDir(null);
                assert (externalDir != null);
                intent.putExtra(SelectImagesActivity.EXTRA_ROOT, externalDir.getAbsolutePath());
                startActivityForResult(intent, RCODE_SELECT_IMAGE);
            }
        });
        dialog.show();
        currentlySelecting = id;
    }

    private int currentlySelecting = -1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RCODE_SELECT_IMAGE) {
                String[] images = data.getStringArrayExtra(SelectImagesActivity.EXTRA_IMAGES);
                Log.i(TAG, images.length + " images picked");
                setAlbumImages(currentlySelecting, images);
                currentlySelecting = -1;
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setAlbumFolders(int id, String[] folders) {

    }

    private void setAlbumImages(int id, String[] images) {
        String prefKey;
        Album album;
        switch (id) {
            case 0:
                prefKey = getString(R.string.key_wallpaper_home_album);
                homeAlbum = new ImageAlbum(images);
                album = homeAlbum;
                break;
            case 1:
                prefKey = getString(R.string.key_wallpaper_lock_album);
                lockAlbum = new ImageAlbum(images);
                album = lockAlbum;
                break;
            default:
                return;
        }
        syncAlbums();
        SharedPreferences.Editor editor = mPrefs.edit();
        try {
            editor.putString(prefKey, album.jsonify().toString(0));
        } catch (JSONException e) {
            e.printStackTrace();
            editor.putString(prefKey, "null");
        } finally {
            editor.apply();
        }
    }

    private boolean wallpaperSet() {
        android.app.WallpaperManager wpm = android.app.WallpaperManager.getInstance(getContext());
        WallpaperInfo info = wpm.getWallpaperInfo();
        if (info == null) {
            return false;
        }
        return (info.getComponent().getClassName().equals(WPMService.class.getName()));
    }

    @Override
    public void onBitmapAvailable(int requestId, Bitmap bitmap) {
        switch (requestId) {
            case 0:
                //home
                mHomeAlbumPreview.setImageBitmap(bitmap);
                break;
            case 1:
                //lock
                mLockAlbumPreview.setImageBitmap(bitmap);
                break;
        }
    }
}
