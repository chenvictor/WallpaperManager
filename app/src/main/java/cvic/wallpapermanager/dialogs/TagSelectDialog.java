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
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.albumable.TagManager;
import cvic.wallpapermanager.ui.custom.CustomCheckBox;

public class TagSelectDialog implements DialogInterface.OnClickListener {

    private final ResultListener listener;
    private final Context ctx;
    private String[] tags;
    private int[] states;
    private Dialog dialog;

    public TagSelectDialog(Context ctx, ResultListener listener) {
        this.ctx = ctx;
        this.listener = listener;
        tags = TagManager.getInstance().getTagNames();
        states = new int[tags.length];
        RecyclerView recycler = new RecyclerView(ctx);
        TagAdapter adapter = new TagAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(ctx));
        recycler.setHasFixedSize(true);
        dialog = new AlertDialog.Builder(ctx).
                setTitle("Select Tags").
                setView(recycler).
                setNegativeButton(android.R.string.cancel, null).
                setPositiveButton(android.R.string.ok, this).
                create();

    }

    public void show() {
        dialog.show();
    }

    private void setState(int idx, int state) {
        states[idx] = state;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int viewId) {
        List<String> include = new ArrayList<>(tags.length);
        List<String> exclude = new ArrayList<>(tags.length);
        for (int i = 0 ; i < tags.length; i++) {
            switch (states[i]) {
                case CustomCheckBox.PLUS:
                    include.add(tags[i]);
                    break;
                case CustomCheckBox.MINUS:
                    exclude.add(tags[i]);
                    break;
            }
        }
        if (include.size() > 0) {
            String[] temp1 = new String[include.size()];
            String[] temp2 = new String[exclude.size()];
            listener.onSelected(include.toArray(temp1), exclude.toArray(temp2));
            dialogInterface.dismiss();
        } else {
            Toast.makeText(ctx, "At least one tag must be included!", Toast.LENGTH_SHORT).show();
        }
    }

    class TagAdapter extends RecyclerView.Adapter<TagViewHolder> {

        @NonNull
        @Override
        public TagViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_multicheckable, viewGroup, false);
            final TagViewHolder holder = new TagViewHolder(view);
            holder.wrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.checkBox.toggle();
                    setState(holder.getAdapterPosition(), holder.checkBox.getState());
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TagViewHolder holder, int i) {
            holder.name.setText(tags[i]);
            holder.checkBox.setState(states[i]);
        }

        @Override
        public int getItemCount() {
            return tags.length;
        }
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        private Button wrapper;
        private TextView name;
        private CustomCheckBox checkBox;
        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            wrapper = itemView.findViewById(R.id.wrapper_button);
            name = itemView.findViewById(R.id.name);
            checkBox = itemView.findViewById(R.id.state);
        }
    }

    public interface ResultListener {
        void onSelected(String[] include, String[] exclude);
    }

}
