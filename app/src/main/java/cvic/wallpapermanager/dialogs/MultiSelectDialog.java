package cvic.wallpapermanager.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cvic.wallpapermanager.R;

public abstract class MultiSelectDialog implements View.OnClickListener {

    private static final int UNCHECKED = 0;
    private static final int UNCHECKED_DRAWABLE = R.drawable.checkbox_blank;

    protected final Context ctx;
    private Dialog dialog;

    String[] names;
    protected int[] states;

    public MultiSelectDialog(Context ctx, String[] names, int[] initialStates) {
        this.ctx = ctx;
        this.names = names;
        states = initialStates;
        RecyclerView recycler = new RecyclerView(ctx);
        TagAdapter adapter = new TagAdapter();
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(ctx));
        recycler.setHasFixedSize(true);
        dialog = new AlertDialog.Builder(ctx).
                setTitle(getTitle()).
                setView(recycler).
                setNegativeButton(android.R.string.cancel, null).
                setPositiveButton(android.R.string.ok, null).
                create();
    }

    public MultiSelectDialog(Context ctx, String[] names) {
        this (ctx, names, new int[names.length]);
    }

    public void show() {
        dialog.show();
        Button ok = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
        ok.setOnClickListener(this);
    }

    private void setState(int idx, int state) {
        states[idx] = state;
    }

    @Override
    public void onClick(View view) {
        if (onOkClicked()) {
            dialog.dismiss();
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
                    setState(holder.getAdapterPosition(), holder.toggle());
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TagViewHolder holder, int i) {
            holder.name.setText(names[i]);
            holder.setState(states[i]);
        }

        @Override
        public int getItemCount() {
            return names.length;
        }
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        private int state;
        private Button wrapper;
        private TextView name;
        private ImageView image;
        TagViewHolder(@NonNull View itemView) {
            super(itemView);
            state = UNCHECKED;
            wrapper = itemView.findViewById(R.id.wrapper_button);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.state);
        }

        int toggle() {
            setState((state + 1) % numStates());
            return state;
        }

        void setState(int state) {
            this.state = state;
            if (state == 0) {
                image.setImageResource(UNCHECKED_DRAWABLE);
            } else {
                image.setImageResource(getStateDrawable(state));
            }
        }
    }

    /**
     * @return  The title for the alert dialog
     */
    protected abstract String getTitle();

    /**
     * Returns the total number of states the checkbox has
     * @return          num states
     */
    protected abstract int numStates();

    /**
     * Returns a drawable resource corresponding to the state
     * @param state     state of the checkbox (not unchecked, ie 0)
     * @return          drawable resource id
     */
    protected abstract int getStateDrawable(int state);

    /**
     * Handle validation of input and result handling
     * @return  true if the input was valid and the dialog should be dismissed,
     *              false otherwise
     */
    protected abstract boolean onOkClicked();

}
