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

import com.bumptech.glide.Glide;

import org.json.JSONException;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.SelectImagesActivity;
import cvic.wallpapermanager.dialogs.AlbumEditDialog;
import cvic.wallpapermanager.dialogs.FolderSelectDialog;
import cvic.wallpapermanager.dialogs.TagSelectDialog;
import cvic.wallpapermanager.model.album.Album;
import cvic.wallpapermanager.model.album.AlbumFactory;
import cvic.wallpapermanager.model.album.FolderAlbum;
import cvic.wallpapermanager.model.album.ImageAlbum;
import cvic.wallpapermanager.model.album.TagAlbum;
import cvic.wallpapermanager.service.WPMService;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallpaperFragment extends Fragment implements CheckBox.OnCheckedChangeListener, TextView.OnEditorActionListener, AdapterView.OnItemSelectedListener, FolderSelectDialog.ResultListener, TagSelectDialog.ResultListener, View.OnClickListener {

    private static final String TAG = "cvic.wpm.wp_frag";

    public static final int RCODE_ENABLE_WALLPAPER = 1234;

    public static final int RCODE_SELECT_IMAGE = 102;

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

    public WallpaperFragment() {
        // Required empty public constructor
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
        container.post(new Runnable() {
            @Override
            public void run() {
                syncAlbums();
            }
        });

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
        if (homeAlbum.getPreview() != null) {
            Glide.with(this).load(homeAlbum.getPreview()).into(mHomeAlbumPreview);
        } else {
            mHomeAlbumPreview.setImageBitmap(null);
        }
        if (lockAlbum.getPreview() != null) {
            Glide.with(this).load(lockAlbum.getPreview()).into(mLockAlbumPreview);
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
        Album album;
        switch(id) {
            case 0:
                album = homeAlbum;
                break;
            case 1:
                album = lockAlbum;
                break;
            default:
                throw new IllegalArgumentException("Id should be one of 0 or 1");
        }
        new AlbumEditDialog(this, album).show();
        currentlySelecting = id;
    }

    private int currentlySelecting = -1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RCODE_SELECT_IMAGE) {
            if (resultCode == RESULT_OK) {
                String[] images = data.getStringArrayExtra(SelectImagesActivity.EXTRA_IMAGES);
                onImagesSelected(images);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
    public void onFoldersSelected(String[] folders) {
        Log.i(TAG, folders.length + " folders selected");
        setAndStoreAlbum(currentlySelecting, new FolderAlbum(folders));
        currentlySelecting = -1;
    }

    @Override
    public void onTagsSelected(String[] include, String[] exclude) {
        Log.i(TAG, include.length + " tags included, " + exclude.length + " tags excluded");
        setAndStoreAlbum(currentlySelecting, new TagAlbum(include, exclude));
        currentlySelecting = -1;
    }

    private void onImagesSelected(String[] images) {
        Log.i(TAG, images.length + " images picked");
        setAndStoreAlbum(currentlySelecting, new ImageAlbum(images));
        currentlySelecting = -1;
    }

    private void setAndStoreAlbum(int id, Album value) {
        String prefKey;
        Album album;
        switch (id) {
            case 0:
                prefKey = getString(R.string.key_wallpaper_home_album);
                homeAlbum = value;
                album = homeAlbum;
                break;
            case 1:
                prefKey = getString(R.string.key_wallpaper_lock_album);
                lockAlbum = value;
                album = lockAlbum;
                break;
            default:
                throw new IllegalArgumentException("Id must be one of 0 or 1");
        }
        // Set views to match
        syncAlbums();
        // Store album preference
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


    public void saveAlbums() {

    }
}
