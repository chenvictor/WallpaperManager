package cvic.wallpapermanager.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

import cvic.wallpapermanager.model.albumable.FolderManager;

public class FolderSelectDialog implements DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnClickListener {

    private final ResultListener listener;
    private String[] folders;
    private boolean[] checked;
    private Dialog dialog;

    public FolderSelectDialog(Context ctx, ResultListener listener) {
        this.listener = listener;
        folders = FolderManager.getInstance().getFolderNames();
        checked = new boolean[folders.length];
        dialog = new AlertDialog.Builder(ctx).
                setTitle("Select Folders").
                setMultiChoiceItems(folders, checked, this).
                setNegativeButton(android.R.string.no, null).
                setPositiveButton(android.R.string.yes, this).
                create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int idx, boolean val) {
        checked[idx] = val;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int viewId) {
        dialogInterface.dismiss();
        List<String> retVal = new ArrayList<>();
        for (int i = 0; i < folders.length; i++) {
            if (checked[i]) {
                retVal.add(folders[i]);
            }
        }
        String[] retArr = new String[retVal.size()];
        listener.onSelected(retVal.toArray(retArr));
    }

    public void show() {
        dialog.show();
    }

    public interface ResultListener {

        void onSelected(String[] folders);

    }
}
