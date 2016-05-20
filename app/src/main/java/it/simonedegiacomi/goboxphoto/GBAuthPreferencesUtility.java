package it.simonedegiacomi.goboxphoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import it.simonedegiacomi.goboxapi.authentication.GBAuth;

/**
 * Utilities to save and reload gobox auth objects
 * Created on 20/05/16.
 * @author Degiacomi Simone
 */
public class GBAuthPreferencesUtility {

    /**
     * Load the Auth object fromt he default shared preferences of the given context
     * @param context Context to use to get the shared preferences
     * @return Auth object loaded from the hared preferences
     */
    public static GBAuth loadFromSharedPreferences (Context context) {

        // Get the shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Create a new auth model
        GBAuth auth = new GBAuth();
        auth.setUsername(preferences.getString("_authUsername", null));
        auth.setMode(GBAuth.Modality.valueOf(preferences.getString("_authMode", GBAuth.Modality.CLIENT.toString())));
        auth.setToken(preferences.getString("_authToken", null));

        return auth;
    }

    /**
     * Save the specified auth object tot he default shared preferences of the given context
     * @param auth Auth object to save
     * @param context Context to use to get the shared preferences
     */
    public static void saveToSharedPreferences (GBAuth auth, Context context) {

        // Get the shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        preferences.edit()
                .putString("_authUsername", auth.getUsername())
                .putString("_authMode", auth.getMode().toString())
                .putString("_authToken", auth.getToken())
                .apply();
    }

    /**
     * Check if the user is logged or not
     * @param context Context to use to get the shared preferences
     * @return User logged or not
     */
    public static boolean isLogged (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains("_authToken");
    }

    /**
     * Remove user's credentials from the shared preferences
     * @param context Context to use to get the shared preferences
     */
    public static void clear (Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .remove("_authToken")
                .remove("_authUsername")
                .remove("_authMode")
                .apply();
    }
}