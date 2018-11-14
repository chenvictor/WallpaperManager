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
import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.dialogs.TextInputDialog;
import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.model.Folder;
import cvic.wallpapermanager.model.FolderManager;
import cvic.wallpapermanager.model.TagManager;
import cvic.wallpapermanager.tasks.FetchFolderTask;
import cvic.wallpapermanager.utils.DisplayUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment implements AdapterView.OnItemSelectedListener, FetchFolderTask.TaskListener, TextInputDialog.ResultListener, View.OnClickListener {

    private static final String TAG = "cvic.wpm.albums";

    private static final int GRID_SIZE = 300;

    private SharedPreferences mPrefs;
    private boolean editEnabled;

    private Button mAddAlbumBtn;
    private Spinner mViewTypeSpinner;
    private TextView mNoFoldersMessage;

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
        loadFolders();
        loadTags();
        return root;
    }

    private void findViews (View root) {
        mViewTypeSpinner = root.findViewById(R.id.view_type_spinner);
        mAddAlbumBtn = root.findViewById(R.id.btn_new_albumable);
        mNoFoldersMessage = root.findViewById(R.id.no_folders_message);
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
        notifyEmpty(mAdapter.getItemCount() == 0);
    }

    private void loadFolders() {
        final String UN_INIT = getString(R.string.uninitialized);
        assert (getContext() != null);
        File file = getContext().getExternalFilesDir(null);
        assert (file != null);
        String rootPath = file.getAbsolutePath();
        if (!rootPath.equals(UN_INIT)) {
            new FetchFolderTask(this).execute(rootPath);
        }
    }

    private void loadTags() {
        // TODO
    }

    @Override
    public void onFoldersFetched(List<Albumable> folders) {
        FolderManager.getInstance().setFolders(folders);
        TagManager.getInstance().initialize();
    }

    public void notifyEmpty(boolean empty) {
        if (empty) {
            //Hide recycler, show message, set folder view, disable spinner
            mRecycler.setVisibility(View.GONE);
            mNoFoldersMessage.setVisibility(View.VISIBLE);
            mViewTypeSpinner.setSelection(0, true);
            mViewTypeSpinner.setEnabled(false);
        } else {
            //Show recycler, hide message, enable spinner
            mRecycler.setVisibility(View.VISIBLE);
            mNoFoldersMessage.setVisibility(View.GONE);
            mViewTypeSpinner.setEnabled(true);
        }
    }

    @Override
    public void onResult(String input) {
        if (input == null) {
            Toast.makeText(getContext(), R.string.message_rename_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        //Create New
        assert (getContext() != null);
        switch(mViewTypeSpinner.getSelectedItemPosition()) {
            case Albumable.TYPE_FOLDER:
                File file = new File(getContext().getExternalFilesDir(null), input);
                Log.i(TAG, "Creating Folder: " + file.getAbsolutePath());
                if (!file.mkdir()) {
                    Toast.makeText(getContext(), R.string.message_folder_exists, Toast.LENGTH_SHORT).show();
                } else {
                    mAdapter.addAlbum(new Folder(file));
                }
                break;
            case Albumable.TYPE_TAG:
                //TODO
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

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        mAdapter.flushCache();
    }
}
