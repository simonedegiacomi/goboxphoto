package it.simonedegiacomi.goboxphoto;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Created on 20/05/16.
 * @author Degiacomi Simone
 */
public class GoBoxStatusNotify {

    public static final int DEFAULT_ID = 0;

    /**
     * ID of the notification
     */
    private final int id;

    private final NotificationManager manager;

    /**
     * Notification builder
     */
    private final NotificationCompat.Builder notificationBuilder;

    private int success = 0;
    private int errors = 0;

    public GoBoxStatusNotify (Context context, NotificationManager manager, int id) {
        this.id = id;
        this.manager = manager;

        // Create the task and the intent to open the app settings on the click
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        Intent resultIntent = new Intent(context, GoBoxPhoto.class);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(GoBoxPhoto.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Fill the builder
        notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("GoBoxPhoto")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(resultPendingIntent);
    }

    public void notifyUpload () {
        notificationBuilder.setContentText("Uploading new photo...");
        showChanges();
    }

    public void notifySuccess () {
        success++;
        notificationBuilder.setContentText(getCountText());
        showChanges();
    }

    public void notifyError () {
        errors++;
        notificationBuilder.setContentText(getCountText());
        showChanges();
    }

    private String getCountText () {
        return new StringBuilder("Success: ")
                .append(success)
                .append(" Errors: ")
                .append(errors)
                .toString();
    }

    private void showChanges () {
        manager.notify(id, notificationBuilder.build());
    }
}