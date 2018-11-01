package cvic.wallpapermanager.ui;


import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

import cvic.wallpapermanager.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumsFragment extends Fragment {

    private RecyclerView mRecycler;
    private RecyclerView.Adapter mAdapter;

    public AlbumsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums, container, false);
        mRecycler = view.findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new FolderAdapter(getActivity(), PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.key_root_folder), null));
        mRecycler.setAdapter(mAdapter);
        mRecycler.setHasFixedSize(true);
        return view;
    }

    public void rootChanged(String path) {
        mAdapter = new FolderAdapter(getActivity(), path);
        mRecycler.setAdapter(mAdapter);
    }

}
