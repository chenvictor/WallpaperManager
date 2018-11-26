package cvic.wallpapermanager.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;

import cvic.wallpapermanager.R;

public class LoadingDialog {

    private static final String TAG = "cvic.wpm.ld";

    private int maxProgress;
    private int current;

    private Dialog dialog;
    private TextView text;
    private ProgressBar bar;

    public LoadingDialog(Context ctx, String title, int maxProgress) {
        this.maxProgress = maxProgress;
        @SuppressLint("InflateParams") View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_loading, null, false);
        bar = view.findViewById(R.id.progressBar);
        text = view.findViewById(R.id.progressMessage);
        if (maxProgress == 0) {
            bar.setIndeterminate(true);
        } else {
            bar.setIndeterminate(false);
            bar.setMax(this.maxProgress);
        }
        dialog = new AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(view)
                .setCancelable(false)
                .create();
    }

    public void show() {
        Log.i(TAG, "loading.show");
        current = 0;
        increment();
        dialog.show();
    }

    public void show(String title, int maxProgress) {
        this.maxProgress = maxProgress;
        dialog.setTitle(title);
        if (maxProgress == 0) {
            bar.setIndeterminate(true);
        } else {
            bar.setIndeterminate(false);
            bar.setMax(maxProgress);
        }
        show();
    }

    public void dismiss() {
        Log.i(TAG, "loading.dismiss");
        dialog.dismiss();
    }

    /**
     * Increments the current progress
     *  Dismisses when the bar is full or if progress is indeterminate
     */
    public void increment () {
        Log.i(TAG, "loading.increment");
        bar.setProgress(current);
        text.setText(String.format(Locale.getDefault(), "%d/%d", current, maxProgress));
        if (current++ >= maxProgress) {
            dialog.dismiss();
        }
    }

}
