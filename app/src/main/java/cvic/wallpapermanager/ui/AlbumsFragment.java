package cvic.wallpapermanager.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.dialogs.TextInputDialog;
import cvic.wallpapermanager.model.albumable.Albumable;
import cvic.wallpapermanager.model.albumable.Folder;
import cvic.wallpapermanager.model.albumable.FolderManager;
import cvic.wallpapermanager.model.albumable.Tag;
import cvic.wallpapermanager.model.albumable.TagManager;
import cvic.wallpapermanager.utils.DisplayUtils;

public class AlbumsFragment extends Fragment implements AdapterView.OnItemSelectedListener,  TextInputDialog.ResultListener, View.OnClickListener {

    private static final String TAG = "cvic.wpm.albums";

    private static final int GRID_SIZE = 300;

    private SharedPreferences mPrefs;
    private boolean editEnabled;

    private Button mAddAlbumBtn;
    private Spinner mViewTypeSpinner;
    private TextView noAlbumableMessage;

    private RecyclerView mRecycler;
    private AlbumAdapter mAdapter;


    public AlbumsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        findViews(root);
        setListeners();
        setPrefs();
        initRecycler();
        return root;
    }

    private void findViews (View root) {
        mViewTypeSpinner = root.findViewById(R.id.view_type_spinner);
        mAddAlbumBtn = root.findViewById(R.id.btn_new_albumable);
        noAlbumableMessage = root.findViewById(R.id.no_folders_message);
        mRecycler = root.findViewById(R.id.recycler);
    }

    private void setListeners () {
        mAddAlbumBtn.setOnClickListener(this);
        mViewTypeSpinner.setOnItemSelectedListener(this);
    }

    private void setPrefs () {
        editEnabled = false;    // lock editing until initialization is done
        mViewTypeSpinner.setSelection(mPrefs.getInt(getString(R.string.key_album_view_type), 0));
        editEnabled = true;     // unlock editing
    }

    private void initRecycler() {
        assert (getActivity() != null);
        int colSpan = DisplayUtils.getDisplayWidth(getActivity()) / GRID_SIZE;
        mRecycler.setLayoutManager(new GridLayoutManager(getContext(), colSpan));
        mAdapter = new AlbumAdapter(this, GRID_SIZE);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setHasFixedSize(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (mViewTypeSpinner.equals(adapterView)) {
            viewTypeSet(i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void viewTypeSet (int idx) {
        if (editEnabled) {
            mPrefs.edit().putInt(getString(R.string.key_album_view_type), idx).apply();
        }
        mAdapter.setViewType(idx);
        noAlbumableMessage.setText(getString(R.string.message_no_albumables, mViewTypeSpinner.getSelectedItem().toString()));
        notifyEmpty(mAdapter.getItemCount() == 0);
    }

    public void notifyEmpty(boolean empty) {
        if (empty) {
            //Hide recycler, show message, disable spinner
            mRecycler.setVisibility(View.GONE);
            noAlbumableMessage.setVisibility(View.VISIBLE);
        } else {
            //Show recycler, hide message, enable spinner
            mRecycler.setVisibility(View.VISIBLE);
            noAlbumableMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResult(String input) {
        if (input == null) {
            Toast.makeText(getContext(), R.string.message_rename_failed_generic, Toast.LENGTH_SHORT).show();
            return;
        }
        //Create New
        assert (getContext() != null);
        switch(mViewTypeSpinner.getSelectedItemPosition()) {
            case Albumable.TYPE_FOLDER:
                FolderManager fm = FolderManager.getInstance();
                File file = new File(getContext().getExternalFilesDir(null), input);
                Log.i(TAG, "Creating Folder: " + file.getAbsolutePath());
                if (!file.mkdir()) {
                    Toast.makeText(getContext(), R.string.message_folder_already_exists, Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.addAlbum(new Folder(file));
                }
                break;
            case Albumable.TYPE_TAG:
                TagManager tm = TagManager.getInstance();
                if (tm.hasTag(input)) {
                    Toast.makeText(getContext(), R.string.message_tag_already_exists, Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.addAlbum(new Tag(input));
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (mAddAlbumBtn.equals(view)) {
            String title = mViewTypeSpinner.getSelectedItem().toString();
            TextInputDialog dialog = new TextInputDialog(getContext(), AlbumsFragment.this, "New " + title);
            dialog.show();
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        mAdapter.notifyDataSetChanged();
//        mAdapter.flushCache();
//    }
}
