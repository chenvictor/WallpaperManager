package cvic.wallpapermanager.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.dialogs.DirectorySelectDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment implements DirectorySelectDialog.ResultListener, View.OnClickListener, RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "cvic.wpm.s_frag";

    private SharedPreferences mPrefs;
    private boolean editEnabled;

    private TextView mRootValue;
    private Button mSetRootBtn;
    private RadioGroup mPositionGroup;
    private CheckBox mRandomOrderCheckBox;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        findViews(root);
        setListeners();
        setPrefs();
        return root;
    }

    private void findViews (View root) {
        mRootValue = root.findViewById(R.id.value_root);
        mSetRootBtn = root.findViewById(R.id.set_root_btn);
        mPositionGroup = root.findViewById(R.id.position_radio_group);
        mRandomOrderCheckBox = root.findViewById(R.id.check_random_order_enabled);
    }

    private void setListeners () {
        mSetRootBtn.setOnClickListener(this);
        mPositionGroup.setOnCheckedChangeListener(this);
        mRandomOrderCheckBox.setOnCheckedChangeListener(this);
    }

    private void setPrefs () {
        editEnabled = false;    // lock editing until initialization is done

        mRootValue.setText(mPrefs.getString(getString(R.string.key_root_folder), getString(R.string.uninitialized)));
        ((RadioButton) mPositionGroup.getChildAt(mPrefs.getInt(getString(R.string.key_position), 0))).setChecked(true);
        mRandomOrderCheckBox.setChecked(mPrefs.getBoolean(getString(R.string.key_random_order_enabled), false));

        editEnabled = true;     // unlock editing
    }

    @Override
    public void onClick(View view) {
        if (mSetRootBtn.equals(view)) {
            selectRootFolder();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        if (mPositionGroup.equals(radioGroup)) {
            positionTypeChecked(radioGroup.indexOfChild(radioGroup.findViewById(id)));
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (mRandomOrderCheckBox.equals(compoundButton)) {
            randomOrderChecked(b);
        }
    }

    /**
     * Handle setting of root folder value,
     *  updating the pref if necessary
     * @param newValue      new value to switch to
     */
    private void rootChanged (String newValue) {
        if (editEnabled) {
            mPrefs.edit().putString(getString(R.string.key_root_folder), newValue).apply();
            //notify listener
        }
        mRootValue.setText(newValue);
    }

    /**
     * Handling setting of position type,
     *  updating the pref if necessary
     * @param idx           new index of checked button
     */
    private void positionTypeChecked (int idx) {
        if (editEnabled) {
            mPrefs.edit().putInt(getString(R.string.key_position), idx).apply();
        }
    }

    /**
     * Handle checking of random order,
     *  updating the pref if necessary
     * @param b     new checked state
     */
    private void randomOrderChecked (boolean b) {
        if (editEnabled) {
            mPrefs.edit().putBoolean(getString(R.string.key_random_order_enabled), b).apply();
        }
    }

    private void selectRootFolder() {
        if (getActivity() != null) {
            DirectorySelectDialog dialog = new DirectorySelectDialog(getActivity(), this);
            dialog.setCurrentPath(mPrefs.getString(getString(R.string.key_root_folder), null));
            dialog.show();
        }
    }

    @Override
    public void result(String path) {
        rootChanged(path);
    }

}
