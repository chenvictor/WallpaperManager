package cvic.wallpapermanager.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import cvic.wallpapermanager.DialogFolderPickDialog;
import cvic.wallpapermanager.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener, DialogFolderPickDialog.ResultListener {

    private RootChangeListener mListener;

    private SharedPreferences mPrefs;
    private Preference mRootFolder;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public void setListener(RootChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        PreferenceManager.setDefaultValues(getContext(), R.xml.preferences, false);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mRootFolder = findPreference(getString(R.string.pref_key_root_folder));
        init();
    }

    private void init() {
        mRootFolder.setSummary(mPrefs.getString(
                getString(R.string.pref_key_root_folder),
                getString(R.string.pref_root_folder_null)));
        mRootFolder.setOnPreferenceClickListener(this);
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
            mRootFolder.setSummary(path);
            if (mListener != null) {
                mListener.rootChanged(path);
            }
        }
    }

    public interface RootChangeListener {

        void rootChanged(String path);

    }
}
