package cvic.wallpapermanager;

import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageViewHolder extends RecyclerView.ViewHolder {

    private ImageView image;
    private TextView label;
    private Group overlay;

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.grid_albumable_image);
        label = itemView.findViewById(R.id.grid_albumable_label);
        overlay = itemView.findViewById(R.id.checked_overlay);
    }

    public ImageView getImage() {
        return image;
    }

    public TextView getLabel() {
        return label;
    }

    public Group getOverlay() {
        return overlay;
    }
}
