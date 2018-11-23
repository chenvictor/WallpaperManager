package cvic.wallpapermanager.dialogs;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.albumable.FolderManager;

public class FolderSelectDialog extends MultiSelectDialog {

    private static final int CHECKED = 1;
    private static final int CHECKED_DRAWABLE = R.drawable.checkbox_checked;
    private final ResultListener listener;

    FolderSelectDialog(Context ctx, ResultListener listener) {
        super(ctx, FolderManager.getInstance().getFolderNames());
        this.listener = listener;
    }

    @Override
    protected String getTitle() {
        return "Select Folders";
    }

    @Override
    protected int numStates() {
        return 2;
    }

    @Override
    protected int getStateDrawable(int state) {
        return CHECKED_DRAWABLE;
    }

    @Override
    protected boolean onOkClicked() {
        List<String> retVal = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if (states[i] == CHECKED) {
                retVal.add(names[i]);
            }
        }
        if (retVal.size() > 0) {
            String[] retArr = new String[retVal.size()];
            listener.onFoldersSelected(retVal.toArray(retArr));
            return true;
        } else {
            Toast.makeText(ctx, "At least one Folder must be selected!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public interface ResultListener {

        void onFoldersSelected(String[] folders);

    }
}
