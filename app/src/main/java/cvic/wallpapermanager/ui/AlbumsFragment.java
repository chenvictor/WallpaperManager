package cvic.wallpapermanager.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.Collections;
import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.tasks.FetchFolderTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment implements AdapterView.OnItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener, FetchFolderTask.TaskListener {

    private static final String TAG = "cvic.wpm.albums";

    private static final int GRID_SIZE = 300;

    private SharedPreferences mPrefs;
    private boolean editEnabled;

    private Spinner mViewTypeSpinner;

    private RecyclerView mRecycler;
    private AlbumAdapter mAdapter;


    public AlbumsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_albums, container, false);
        initRecycler(container, root);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        findViews(root);
        setListeners();
        setPrefs();
        return root;
    }

    private void findViews (View root) {
        mViewTypeSpinner = root.findViewById(R.id.view_type_spinner);
    }

    private void setListeners () {
        mViewTypeSpinner.setOnItemSelectedListener(this);
    }

    private void setPrefs () {
        editEnabled = false;    // lock editing until initialization is done
        mViewTypeSpinner.setSelection(mPrefs.getInt(getString(R.string.key_album_view_type), 0));
        editEnabled = true;     // unlock editing
    }

    private void initRecycler(ViewGroup container, View view) {
        mRecycler = view.findViewById(R.id.recycler);
        int colSpan = container.getMeasuredWidth() / GRID_SIZE;
        mRecycler.setLayoutManager(new GridLayoutManager(getContext(), colSpan));
        mAdapter = new AlbumAdapter(getContext());
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
        mAdapter.setAdapterItems(Collections.<Albumable>emptyList());
        switch (idx) {
            case 0:
                loadFolders();
                break;
            case 1:
                loadTags();
        }
    }

    private void loadFolders() {
        final String UN_INIT = getString(R.string.uninitialized);
        String rootPath = mPrefs.getString(getString(R.string.key_root_folder), UN_INIT);
        if (!rootPath.equals(UN_INIT)) {
            new FetchFolderTask(this).execute(rootPath);
        }
    }

    private void loadTags() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mViewTypeSpinner.getSelectedItemPosition() == 0) {
            //If viewing folders,
            if (key.equals(getString(R.string.key_root_folder))) {
                // and root folder was changed, reload folders
                loadFolders();
            }
        }
    }

    @Override
    public void onFoldersFetched(List<Albumable> folders) {
        mAdapter.setAdapterItems(folders);
    }
}
