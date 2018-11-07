package cvic.wallpapermanager.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import cvic.wallpapermanager.R;

public class TextInputDialog {

    private ResultListener mListener;
    private Dialog mDialog;

    public TextInputDialog (Context ctx, ResultListener listener, String title) {
        this(ctx, listener, title, "");
    }

    public TextInputDialog (Context ctx, ResultListener listener, String title, String defaultValue) {
        mListener = listener;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(title);
        final EditText input = new EditText(ctx);
        input.setText(defaultValue);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(ctx.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Validate the string
                String text = input.getText().toString();
                if (text.matches("[a-zA-Z0-9]+")) {
                    mListener.onResult(text);
                } else {
                    mListener.onResult(null);
                }
            }
        });
        builder.setNegativeButton(ctx.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        mDialog = builder.create();
    }

    public void show () {
        mDialog.show();
    }

    public interface ResultListener {

        void onResult(String input);

    }

}
