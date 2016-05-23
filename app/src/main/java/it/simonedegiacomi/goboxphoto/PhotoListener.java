package it.simonedegiacomi.goboxphoto;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;

import it.simonedegiacomi.goboxapi.GBFile;
import it.simonedegiacomi.goboxapi.client.StandardGBClient;

/**
 * Created on 06/05/16.
 *
 * @author Degiacomi Simone
 */
public class PhotoListener extends Service {

    /**
     * Flag that indicates if the service is running
     */
    private static boolean running = false;

    /**
     * Content observer
     */
    private ContentObserver observer;

    private GoBoxStatusNotify notify;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // This is not a bind service
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("GoBoxPhoto", "on start");

        //Stay in the background
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Check if the sync in enabled
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("syncEnabled", false)) {
            stopSelf();
            return;
        }

        // Create the observer
        observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                onNewPhoto();
            }

            @Override
            public void onChange(boolean selfChange, Uri uri) {
                onNewPhoto();
            }
        };

        // Register the observer
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, observer);
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer);

        // Update the flag
        running = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister observer
        getContentResolver().unregisterContentObserver(observer);

        notify = null;

        // Update the flag
        running = false;
    }

    private void onNewPhoto() {
        Log.i("GoBoxPhoto", "new photo event");

        // Find the last captured picture
        File picture = findLastPicture();

        // Upload the file
        uploadPhoto(picture);
    }
    /**
     * Find the last captured photo
     * @return Last captured photo
     */
    private File findLastPicture() {

        // Find the last picture
        String[] projection = new String[]{
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.MIME_TYPE
        };

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

        if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        File picture = new File(cursor.getString(1));
        cursor.close();
        return picture;
    }


    private void uploadPhoto (File file) {

        // Create a new client
        final StandardGBClient client = new StandardGBClient(GBAuthPreferencesUtility.loadFromSharedPreferences(this));

        new AsyncTask<File, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                if (notify == null) {
                    notify = new GoBoxStatusNotify(PhotoListener.this, (NotificationManager) getSystemService(NOTIFICATION_SERVICE), GoBoxStatusNotify.DEFAULT_ID);
                }
                notify.notifyUpload();
            }

            @Override
            protected Boolean doInBackground(File... params) {
                try {

                    // Connect the client
                    if (!client.init()) {
                        return false;
                    }

                    // Create the gbfile
                    final GBFile photo = new GBFile(params[0].getName(), getDestinationFolderID(), false);
                    photo.setSize(params[0].length());

                    // Upload it
                    client.uploadFile(photo, new FileInputStream(params[0]));

                    // Disconnect the client
                    client.shutdown();
                    return true;
                } catch (Exception ex) {
                    Log.i("GoBoxPhoto", "Upload failed");
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    notify.notifySuccess();
                    return;
                }
                notify.notifyError();
            }
        }.execute(file);

    }

    public static boolean isRunning () {
        return running;
    }

    private long getDestinationFolderID () {
        return PreferenceManager.getDefaultSharedPreferences(this).getLong("uploadFolderID", GBFile.ROOT_ID);
    }
}