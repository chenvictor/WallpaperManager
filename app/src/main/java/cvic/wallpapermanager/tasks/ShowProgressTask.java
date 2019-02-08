package cvic.wallpapermanager.tasks;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import cvic.wallpapermanager.dialogs.LoadingDialog;

public abstract class ShowProgressTask<X, Y, Z> extends AsyncTask<X, Y, Z> {

    private final LoadingDialog dialog;
    private final int maxProgress;
    private final String title;

    ShowProgressTask(@NonNull LoadingDialog dialog, int maxProgress, String title) {
        this.dialog = dialog;
        this.maxProgress = maxProgress;
        this.title = title;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.show(title, maxProgress);
    }

    @Override
    protected void onProgressUpdate(Y[] values) {
        super.onProgressUpdate(values);
        dialog.increment();
    }

    @Override
    protected void onPostExecute(Z o) {
        super.onPostExecute(o);
        dialog.dismiss();
    }
}
