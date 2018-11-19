package cvic.wallpapermanager.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.RemoteViews;

import cvic.wallpapermanager.R;

class LockNotification extends Notification {

    static final String ACTION_CUSTOM = "cvic.wpm.notification.ACTION";
    static final String EXTRA = "cvic.wpn.notification.EXTRA";

    private static final String CHANNEL_ID = "cvic.wpm.notification";
    private final int ID = 123;

    private NotificationManagerCompat notificationManager;
    private Notification notification;

    LockNotification(Context ctx) {
        createChannel(ctx);
        notificationManager = NotificationManagerCompat.from(ctx);

        createNotification(ctx);
    }

    private void createChannel(Context ctx) {
        //Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = ctx.getString(R.string.app_name);
            String description = "temp description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }
    }

//    private void createNotification(Context ctx) {
////        Intent intent = new Intent();
////        intent.putExtra(EXTRA, true);
////        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
////        NotificationCompat.Action cycleAction = new NotificationCompat.Action(0, "Cycle", pendingIntent);
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID).
////        setSmallIcon(R.drawable.folder_icon).
////        setContentTitle("Wallpaper Manager").
////        setContentText("Change lock screen wallpaper").
////        setPriority(NotificationCompat.PRIORITY_MAX).
////        addAction(cycleAction).
////        setShowWhen(false).
////        setOngoing(true);
////        notification = builder.build();
////    }

    private void createNotification(Context ctx) {
        RemoteViews view = new RemoteViews(ctx.getPackageName(), R.layout.notification);
        Intent intent = new Intent();
        intent.setAction(ACTION_CUSTOM);
        intent.putExtra(EXTRA, true);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 55, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.notification_button, pendingIntent);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID).
                setSmallIcon(android.R.color.transparent).
                setCustomContentView(view).
                setPriority(NotificationCompat.PRIORITY_MAX).
                setShowWhen(false).
                setOngoing(true);
        notification = builder.build();
    }

    void show() {
        notificationManager.notify(ID, notification);
    }

    void hide() {
        notificationManager.cancel(ID);
    }

    void destroy(Context ctx) {
        hide();
        destroyChannel(ctx);
    }

    private void destroyChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = ctx.getSystemService(NotificationManager.class);
            assert manager != null;
            manager.deleteNotificationChannel(CHANNEL_ID);
        }
    }

}
