package cvic.wallpapermanager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import cvic.wallpapermanager.model.Albumable;
import cvic.wallpapermanager.model.Folder;
import cvic.wallpapermanager.model.FolderManager;
import cvic.wallpapermanager.model.TagManager;
import cvic.wallpapermanager.ui.MultiSelectImageAdapter;

public class AlbumablePreviewActivity extends MultiSelectImageActivity {

    private Albumable album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int type = intent.getIntExtra(Albumable.EXTRA_TYPE, -1);
        int id = intent.getIntExtra(Albumable.EXTRA_ID, -1);
        switch (type) {
            case Albumable.TYPE_FOLDER:
                album = FolderManager.getInstance().getFolder(id);
                break;
            case Albumable.TYPE_TAG:
                album = TagManager.getInstance().getTag(id);
                break;
            default:
                Toast.makeText(this, "Invalid ID Provided", Toast.LENGTH_SHORT).show();
                finish();
        }
        super.onCreate(savedInstanceState);
    }

    protected String getDefaultTitle() {
        return getString(R.string.folder_title, album.getName(), album.getCount());
    }

    @Override
    protected MultiSelectImageAdapter getAdapter() {
        return new ImageAdapter(this, album);
    }

    @Override
    protected void defaultMenuOptions(Menu menu) {
        menu.add("Add Images").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                album.addImage(AlbumablePreviewActivity.this);
                return true;
            }
        });
    }

    @Override
    protected void multiselectMenuOptions(Menu menu) {
        menu.add("Remove").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @SuppressWarnings("ResultOfMethodCallIgnored")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                for (File file : mAdapter.getSelections()) {
                    file.delete();
                }
                album.refresh();
                mAdapter.flushCache();
                mAdapter.clearSelections();
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Folder.PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                album.refresh();
                mAdapter.notifyDataSetChanged();
                toolbar.setTitle(getString(R.string.folder_title, album.getName(), album.getCount()));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSingleImageClick(File file) {
        Intent intent = new Intent(this, FolderIVA.class);
        intent.putExtra(ImageViewActivity.IMAGE_PATH, file.getAbsolutePath());
        startActivity(intent);
    }

    static class ImageAdapter extends MultiSelectImageAdapter {

        private Albumable mAlbum;

        ImageAdapter(AlbumablePreviewActivity ctx, Albumable album) {
            super(ctx, ctx, false);
            mAlbum = album;
        }

        @Override
        public void onBitmapAvailable(int requestId, Bitmap bitmap) {
            notifyItemChanged(requestId);
        }

        @Override
        public void directoryClicked(File file) {
            throw new UnsupportedOperationException();
        }

        @Override
        public File getFile(int i) {
            return mAlbum.getImage(i);
        }

        @Override
        public boolean onBackPressed() {
            return false;
        }

        @Override
        public int getItemCount() {
            if (mAlbum == null) {
                return 0;
            }
            return mAlbum.getCount();
        }

    }

}
