package cvic.wallpapermanager.dialogs;

import android.content.Intent;
import android.view.View;

import java.io.File;

import cvic.wallpapermanager.SelectImagesActivity;
import cvic.wallpapermanager.model.album.Album;
import cvic.wallpapermanager.model.album.FolderAlbum;
import cvic.wallpapermanager.model.album.ImageAlbum;
import cvic.wallpapermanager.model.album.TagAlbum;
import cvic.wallpapermanager.ui.WallpaperFragment;

public class AlbumEditDialog extends ContextMenuDialog {

    private static final String TITLE = "Edit Album";
    private static final String CURRENT = " (Current)";

    public AlbumEditDialog(final WallpaperFragment fragment, Album album) {
        super(fragment.getContext(), TITLE);
        addButton("Folder" + (album instanceof FolderAlbum ? CURRENT : ""), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FolderSelectDialog(AlbumEditDialog.this.ctx, fragment).show();
            }
        });
        addButton("Tag" + (album instanceof TagAlbum ? CURRENT : ""), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TagSelectDialog(AlbumEditDialog.this.ctx, fragment).show();
            }
        });
        addButton("Image" + (album instanceof ImageAlbum ? CURRENT : ""), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ctx, SelectImagesActivity.class);
                File externalDir = ctx.getExternalFilesDir(null);
                assert (externalDir != null);
                intent.putExtra(SelectImagesActivity.EXTRA_ROOT, externalDir.getAbsolutePath());
                fragment.startActivityForResult(intent, WallpaperFragment.RCODE_SELECT_IMAGE);
            }
        });
    }

}
