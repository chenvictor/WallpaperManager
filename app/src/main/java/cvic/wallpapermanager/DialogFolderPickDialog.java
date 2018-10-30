package cvic.wallpapermanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;

public class DialogFolderPickDialog {

    private ResultListener mListener;

    private Activity mActivity;
    private AlertDialog mDialog;

    private RecyclerView mRecycler;
    private SimpleAdapter mAdapter;
    private TextView mPathTextView;
    private File currentDir;
    private String[] currentFolders = {};

    public DialogFolderPickDialog(@NonNull Activity activity, final ResultListener listener) {
        mListener = listener;
        mActivity = activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_folderpick_dialog, null);
        mPathTextView = view.findViewById(R.id.textview_path);
        mRecycler = view.findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(activity));
        mAdapter = new SimpleAdapter();
        mRecycler.setAdapter(mAdapter);
        Button backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upDirectory();
            }
        });
        Button newBtn = view.findViewById(R.id.button_newfolder);
        newBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newFolder();
            }
        });
        builder.setView(view).setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mListener != null) {
                    mListener.result(currentDir.getAbsolutePath());
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        mDialog = builder.create();
        changeDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
    }

    public void setCurrentPath(String path) {
        if (path != null) {
            changeDirectory(new File(path));
        }
    }

    public void show() {
        mDialog.show();
    }

    private void changeDirectory(File file) {
        if (file != null) {
            currentDir = file;
            mPathTextView.setText(file.getPath());
            currentFolders = currentDir.list(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return new File(file, s).isDirectory();
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    }

    private void upDirectory() {
        if (!currentDir.equals(Environment.getExternalStorageDirectory())) {
            File parent = currentDir.getParentFile();
            if (parent != null) {
                changeDirectory(parent);
            }
        }
    }

    private void newFolder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("New Folder");
        final EditText input = new EditText(mActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(mActivity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createFolder(input.getText().toString());
            }
        });
        builder.setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    private void createFolder(String name) {
        File newFolder = new File(currentDir, name);
        if (newFolder.mkdirs()) {
            //refresh recyclerview
            changeDirectory(currentDir);
        }
    }

    private class SimpleAdapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.card_directory, viewGroup, false);
            final ViewHolder holder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    changeDirectory(new File(currentDir, currentFolders[holder.getAdapterPosition()]));
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            View view = viewHolder.itemView;
            TextView name = view.findViewById(R.id.textview_dir);
            name.setText(currentFolders[i]);
        }

        @Override
        public int getItemCount() {
            return currentFolders.length;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface ResultListener {

        void result(String path);

    }
}
