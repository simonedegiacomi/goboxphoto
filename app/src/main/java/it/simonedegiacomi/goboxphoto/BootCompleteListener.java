package it.simonedegiacomi.goboxphoto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

/**
 * Created on 17/05/16.
 * @author Degiacomi Simone
 */
public class BootCompleteListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Check if the sync is enabled
        if(!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("syncEnabled", false)) {
            return;
        }

        // start the service
        context.startService(new Intent(context, PhotoListener.class));
    }
}