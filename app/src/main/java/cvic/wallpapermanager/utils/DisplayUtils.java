package cvic.wallpapermanager.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

public class DisplayUtils {

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

}
