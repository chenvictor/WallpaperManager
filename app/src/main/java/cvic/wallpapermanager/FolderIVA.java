package cvic.wallpapermanager;

import android.os.Bundle;
import android.view.View;

public class FolderIVA extends ImageViewActivity {

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
                //TODO
            }
        });
    }
}
