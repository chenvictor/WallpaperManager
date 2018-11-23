package cvic.wallpapermanager.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cvic.wallpapermanager.R;

/**
 * A simple alert dialog to handle context menus
 * Dismisses automatically when an option is selected
 */
public class ContextMenuDialog {

    protected Context ctx;
    private Dialog dialog;
    private LinearLayout buttonArea;

    @SuppressLint("InflateParams")
    public ContextMenuDialog(Context ctx, String title) {
        this.ctx = ctx;
        buttonArea = (LinearLayout) LayoutInflater.from(ctx).inflate(R.layout.dialog_ctx_menu, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title).setView(buttonArea);
        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }

    public void addButton (String text, final View.OnClickListener listener) {
        Button newButton = (Button) LayoutInflater.from(ctx).inflate(R.layout.button_flush, buttonArea, false);
        newButton.setText(text);
        //newButton.setBackgroundColor(highlight ? HIGHLIGHT_COLOR : DEFAULT_COLOR);
        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                listener.onClick(view);
            }
        });
        buttonArea.addView(newButton);
    }

}
