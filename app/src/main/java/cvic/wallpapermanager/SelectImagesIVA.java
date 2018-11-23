package cvic.wallpapermanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class SelectImagesIVA extends SingleImageActivity {

    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        path = getIntent().getStringExtra(IMAGE_PATH);
        addButton("Select", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra(IMAGE_PATH, path);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}
