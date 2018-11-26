package cvic.wallpapermanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class ReceiveImageActivity extends SingleImageActivity {

    private static final String TAG = "cvic.wpm.ria";
    private Button folderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderBtn = addButton("Folder", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeFolder();
            }
        });
        addButton("Tags", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeTags();
            }
        });
        addButton("Add", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                add();
            }
        });
    }

    @NonNull
    @Override
    protected Uri getImageUri() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                Log.i(TAG,"Received image");
                Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (imageUri != null) {
                    return imageUri;
                }
            } else if (type.startsWith("text/")) {
                Log.i(TAG,"Received text");
                String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (text != null) {
                    Log.i(TAG, text);
                }
            }
        }

        throw new RuntimeException("Did not receive a valid Uri!");
    }

    private void changeFolder() {
        folderBtn.setText("Test");
    }

    private void changeTags() {

    }

    private void add() {
        finish();
    }
}
