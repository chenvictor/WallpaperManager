package cvic.wallpapermanager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import java.io.File;

import cvic.wallpapermanager.dialogs.MultiSelectDialog;
import cvic.wallpapermanager.model.ImageFile;
import cvic.wallpapermanager.model.ImageFileManager;
import cvic.wallpapermanager.model.albumable.TagManager;

public class FolderIVA extends SingleImageActivity {

    ImageFile imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addButton("Delete", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        addButton("Move", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
        addButton("Tags", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TagManager tm = TagManager.getInstance();
                String[] names = new String[tm.size() - 1];
                for (int i = 1; i < tm.size(); i++) {
                    names[i-1] = tm.getTag(i).getName();
                }
                int[] states = new int[tm.size() - 1];
                for (int i = 0; i < states.length; i++) {
                    if (imageFile.hasTag(tm.getTag(i + 1))) {
                        states[i] = 1;
                    }
                }
                new TagDialog(view.getContext(), names, states).show();
            }
        });
    }

    @NonNull
    @Override
    protected Uri getImageUri() {
        File file = new File(getIntent().getStringExtra(IMAGE_PATH));
        imageFile = ImageFileManager.getInstance().getImage(file);
        return Uri.fromFile(file);
    }

    private class TagDialog extends MultiSelectDialog {

        TagDialog(Context ctx, String[] names, int[] initialStates) {
            super(ctx, names, initialStates);
        }

        @Override
        protected String getTitle() {
            return "Tags";
        }

        @Override
        protected int numStates() {
            return 2;
        }

        @Override
        protected int getStateDrawable(int state) {
            return R.drawable.checkbox_checked;
        }

        @Override
        protected boolean onOkClicked() {
            TagManager tm = TagManager.getInstance();
            for (int i = 0; i < states.length; i++) {
                if (states[i] == 1) {
                    imageFile.addTag(tm.getTag(i + 1));
                } else {
                    imageFile.removeTag(tm.getTag(i + 1));
                }
            }
            return true;
        }
    }
}
