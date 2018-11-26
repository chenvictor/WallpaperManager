package cvic.wallpapermanager.model.albumable;

import android.content.Context;

import cvic.wallpapermanager.dialogs.ContextMenuDialog;

public class DefaultTag extends Tag {

    private static final String NAME = "untagged";

    DefaultTag() {
        super(NAME);
    }

    @Override
    protected ContextMenuDialog getContextMenu(Context ctx) {
        ContextMenuDialog dialog = new ContextMenuDialog(ctx, toString());
        dialog.addButton("Rename", null);
        dialog.addButton("Delete", null);
        return dialog;
    }
}
