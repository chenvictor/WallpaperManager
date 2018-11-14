package cvic.wallpapermanager.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.model.FolderManager;
import cvic.wallpapermanager.model.TagManager;

public class AlbumSelectDialog {

    private ResultListener listener;
    private Dialog dialog;
    private RecyclerView recycler;
    private final int requestCode;

    public AlbumSelectDialog(Context ctx, ResultListener listener, int requestCode, int... exclude) {
        this.requestCode = requestCode;
        this.listener = listener;
        recycler = new RecyclerView(ctx);
        recycler.setLayoutManager(new LinearLayoutManager(ctx));
        SimpleAdapter adapter = new SimpleAdapter(this, requestCode, exclude);
        recycler.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Select an Album").setView(recycler);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }

    private void clicked(int idx) {
        dialog.dismiss();
        listener.onResult(requestCode, idx);
    }

    public interface ResultListener {
        void onResult(int requestCode, int index);
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<ViewHolder> {

        private int requestCode;
        private boolean[] disabled;
        private AlbumSelectDialog parent;

        SimpleAdapter(AlbumSelectDialog dialog,int requestCode, int... exclude) {
            this.requestCode = requestCode;
            parent = dialog;
            switch (requestCode) {
                case Albumable.TYPE_FOLDER:
                    disabled = new boolean[FolderManager.getInstance().size()];
                    break;
                case Albumable.TYPE_TAG:
                    disabled = new boolean[TagManager.getInstance().size()];
                    break;
                default:
                    throw new IllegalArgumentException("Request code is invalid");
            }
            for (int i : exclude) {
                disabled[i] = true;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_directory, viewGroup, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int idx = holder.getAdapterPosition();
                    if (!disabled[idx]) {
                        parent.clicked(idx);
                    }
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            Button name = viewHolder.name;
            name.setText(getItem(i).getName());
            name.setEnabled(!disabled[i]);
        }

        private Albumable getItem(int idx) {
            switch(requestCode) {
                case Albumable.TYPE_FOLDER:
                    return FolderManager.getInstance().getFolder(idx);
                case Albumable.TYPE_TAG:
                    return TagManager.getInstance().getTag(idx);
                default:
                    throw new IllegalStateException("Request code invalid!");
            }
        }

        @Override
        public int getItemCount() {
            return FolderManager.getInstance().size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        Button name;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.directory_name);
        }
    }

}
