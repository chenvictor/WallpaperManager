package cvic.wallpapermanager.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import cvic.wallpapermanager.DialogFolderPickDialog;
import cvic.wallpapermanager.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, DialogFolderPickDialog.ResultListener, Preference.OnPreferenceChangeListener {

    private RootChangeListener mListener;

    private SharedPreferences mPrefs;
    private Preference mRootFolder;
    private ListPreference mFitType;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public void setListener(RootChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mRootFolder = findPreference(getString(R.string.pref_key_root_folder));
        mFitType = (ListPreference) findPreference("KEY_IMAGE_FIT_TYPE");
        init();
    }

    private void init() {
        if (mPrefs.getString(getString(R.string.pref_key_root_folder), getString(R.string.pref_root_folder_null)).equals(getString(R.string.pref_root_folder_null))) {
            //initialize the value
            mPrefs.edit().putString(getString(R.string.pref_key_root_folder), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()).apply();
        }
        mRootFolder.setSummary(mPrefs.getString(
                getString(R.string.pref_key_root_folder),
                getString(R.string.pref_root_folder_null)));
        mRootFolder.setOnPreferenceChangeListener(this);
        mRootFolder.setOnPreferenceClickListener(this);
        mFitType.setSummary(mFitType.getEntry());
        mFitType.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.equals(mRootFolder)) {
            selectRootFolder();
            return true;
        }
        return false;
    }

    private void selectRootFolder() {
        if (getActivity() != null) {
            DialogFolderPickDialog dialog = new DialogFolderPickDialog(getActivity(), this);
            dialog.setCurrentPath(mPrefs.getString(getString(R.string.pref_key_root_folder), null));
            dialog.show();
        }
    }

    @Override
    public void result(String path) {
        if (!path.contentEquals(mRootFolder.getSummary())) {
            mPrefs.edit().putString(getString(R.string.pref_key_root_folder), path).apply();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String value = (String) o;
        if (preference.equals(mRootFolder)) {
            mRootFolder.setSummary(value);
            if (mListener != null) {
                mListener.rootChanged(value);
            }
            return true;
        }
        if (preference.equals(mFitType)) {
            mFitType.setSummary(mFitType.getEntries()[Integer.parseInt(value) - 1]);    //convert to 0 based
            return true;
        }
        return false;
    }

    public interface RootChangeListener {

        void rootChanged(String path);

    }
}
