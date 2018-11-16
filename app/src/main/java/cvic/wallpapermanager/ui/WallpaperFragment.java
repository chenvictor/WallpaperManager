package cvic.wallpapermanager.ui;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Locale;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.SelectImagesActivity;
import cvic.wallpapermanager.dialogs.ContextMenuDialog;
import cvic.wallpapermanager.utils.JSONUtils;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class WallpaperFragment extends Fragment implements CheckBox.OnCheckedChangeListener, TextView.OnEditorActionListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final String TAG = "cvic.wpm.wp_frag";
    private static final int RCODE_SELECT_FOLDER = 100;
    private static final int RCODE_SELECT_TAG = 101;
    private static final int RCODE_SELECT_IMAGE = 102;

    private SharedPreferences mPrefs;
    private boolean editEnabled;

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
        return root;
    }

    private void findViews(View root) {
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
    }

    private void setListeners() {
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
    }

    private void setPrefs() {
        editEnabled = false;    // lock editing until initialization is done

        // General
        mIntervalEnabledCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_interval_enabled), true));
        mIntervalValue.setText(String.valueOf(mPrefs.getInt(getString(R.string.key_wallpaper_interval_value), 1)));
        mIntervalSpinner.setSelection(mPrefs.getInt(getString(R.string.key_wallpaper_interval_type), 0));

        // Home
        //TODO image must also be changed
        mHomeAlbumDesc.setText(getDesc(mPrefs.getString(getString(R.string.key_wallpaper_home_album), getString(R.string.uninitialized))));
        mHomeDoubleTapCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_home_double_tap_enabled), false));

        // Lock
        mLockUseHomeSwitch.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_lock_use_home), true));
        //TODO image must also be changed
        mLockAlbumDesc.setText(getDesc(mPrefs.getString(getString(R.string.key_wallpaper_lock_album), getString(R.string.uninitialized))));
        mLockBlurCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_wallpaper_lock_blur), false));

        editEnabled = true;     // unlock editing
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
     * Initiates an album change
     * @param id    0 for Home screen
     *              1 for Lock screen
     */
    private void changeAlbum(int id) {
        //TODO, open select album activity
        ContextMenuDialog dialog = new ContextMenuDialog(getContext(), "Album Type");
        dialog.addButton("Folder", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Folder selected");
            }
        });
        dialog.addButton("Tag", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Tag selected");
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

    private String getDesc(String preferenceString) {
        if (preferenceString.equals(getString(R.string.uninitialized))) {
            return preferenceString;
        }
        try {
            JSONObject data = new JSONObject(preferenceString);
            String appendage = "";
            String type = data.getString("type");
            switch (type) {
                case "FOLDER":
                    break;
                case "TAG":
                    break;
                case "IMAGE":
                    appendage = String.format(Locale.getDefault(), "(%d)", data.getJSONArray("images").length());
                    break;
            }
            return type + " " + appendage;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getString(R.string.uninitialized);
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

    private void setAlbumImages(int id, String[] images) {
        TextView desc;
        String prefKey;
        String countKey;
        switch (id) {
            case 0:
                desc = mHomeAlbumDesc;
                prefKey = getString(R.string.key_wallpaper_home_album);
                countKey = getString(R.string.key_wallpaper_home_album_count);
                break;
            case 1:
                desc = mLockAlbumDesc;
                prefKey = getString(R.string.key_wallpaper_lock_album);
                countKey = getString(R.string.key_wallpaper_lock_album_count);
                break;
            default:
                return;
        }
        String output = JSONUtils.jsonifyImages(images);
        if (output == null) {
            output = getString(R.string.uninitialized);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(prefKey, output);
        editor.putInt(countKey, images.length);
        editor.apply();
        desc.setText(getDesc(output));
    }

}
