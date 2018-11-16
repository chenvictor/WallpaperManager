package cvic.wallpapermanager.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

public class DisplayUtils {

    private static final String TAG = "cvic.wpm.util.display";

    public static int getDisplayWidth(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static Bitmap decodeBitmap (String path, int width, int height) {
        Log.i(TAG, "Decoding bitmap, requested size: " + width + "x" + height);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);  //decode just the size of the image
        Log.i(TAG, "Actual size: " + options.outWidth + "x" + options.outHeight);
        options.inSampleSize = DisplayUtils.calculateInSampleSize(options, width, height);
        options.outWidth = width;
        options.outHeight = height;
        options.inJustDecodeBounds = false;
        Bitmap temp = BitmapFactory.decodeFile(path, options);
        Log.i(TAG, "Decoded size: " + options.outWidth + "x" + options.outHeight);
        return temp;
    }

    // Sample Android code to calculate sample size
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
