package cvic.wallpapermanager.dialogs;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cvic.wallpapermanager.R;
import cvic.wallpapermanager.model.albumable.TagManager;

public class TagSelectDialog extends MultiSelectDialog {

    private static final int PLUS      = 1;
    private static final int MINUS     = 2;
    private static final int[] DRAWABLES = {0, R.drawable.checkbox_plus, R.drawable.checkbox_minus};

    private final ResultListener listener;

    TagSelectDialog(Context ctx, ResultListener listener) {
        super(ctx, TagManager.getInstance().getTagNames());
        this.listener = listener;
    }

    @Override
    protected String getTitle() {
        return "Select Tags";
    }

    @Override
    protected int numStates() {
        return 3;
    }

    @Override
    protected int getStateDrawable(int state) {
        return DRAWABLES[state];
    }

    @Override
    protected boolean onOkClicked() {
        List<String> include = new ArrayList<>(names.length);
        List<String> exclude = new ArrayList<>(names.length);
        for (int i = 0 ; i < names.length; i++) {
            switch (states[i]) {
                case PLUS:
                    include.add(names[i]);
                    break;
                case MINUS:
                    exclude.add(names[i]);
                    break;
            }
        }
        if (include.size() > 0) {
            String[] temp1 = new String[include.size()];
            String[] temp2 = new String[exclude.size()];
            listener.onTagsSelected(include.toArray(temp1), exclude.toArray(temp2));
            return true;
        } else {
            Toast.makeText(ctx, "At least one tag must be included!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public interface ResultListener {
        void onTagsSelected(String[] include, String[] exclude);
    }

}
