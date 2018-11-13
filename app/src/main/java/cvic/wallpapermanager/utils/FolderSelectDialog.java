package cvic.wallpapermanager.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.FolderManager;

public class FolderSelectDialog {

    private ResultListener listener;
    private Dialog dialog;
    private RecyclerView recycler;

    public FolderSelectDialog (Context ctx, ResultListener listener, int... exclude) {
        this.listener = listener;
        recycler = new RecyclerView(ctx);
        recycler.setLayoutManager(new LinearLayoutManager(ctx));
        SimpleAdapter adapter = new SimpleAdapter(this, exclude);
        recycler.setAdapter(adapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Select a Folder").setView(recycler);
        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }

    private void clicked(int idx) {
        dialog.dismiss();
        listener.onResult(idx);
    }

    public interface ResultListener {
        void onResult(int index);
    }

    private static class SimpleAdapter extends RecyclerView.Adapter<ViewHolder> {

        private boolean[] disabled;
        private FolderSelectDialog parent;

        SimpleAdapter(FolderSelectDialog dialog, int... exclude) {
            parent = dialog;
            disabled = new boolean[FolderManager.getInstance().size()];
            for (int i : exclude) {
                disabled[i] = true;
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_directory, viewGroup, false);
            final ViewHolder holder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
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
            TextView name = viewHolder.name;
            name.setText(FolderManager.getInstance().getFolder(i).getName());
            if (disabled[i]) {
                name.setAlpha(0.5f);
            } else {
                name.setAlpha(1f);
            }
        }

        @Override
        public int getItemCount() {
            return FolderManager.getInstance().size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.directory_name);
        }
    }

}
