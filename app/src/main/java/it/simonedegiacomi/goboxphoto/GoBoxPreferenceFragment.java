package it.simonedegiacomi.goboxphoto;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;



public class GoBoxPreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences (Bundle bundle, String s) {

        // Load the preferences file
        addPreferencesFromResource(R.xml.preferences);

        Preference destinationFolder = findPreference("destinationFolder");
        Preference logout = findPreference("logout");

        destinationFolder.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                GoBoxFolderPickerDialog dialog = new GoBoxFolderPickerDialog();
                dialog.show(getFragmentManager(), "fragment_select_folder");
                return false;
            }
        });

        // Logout button
        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Stop service
                getContext().stopService(new Intent(getContext(), PhotoListener.class));

                // Delete shared preferences
                GBAuthPreferencesUtility.clear(getContext());

                // Close app
                getActivity().finish();
                return false;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("syncEnabled")) {
            boolean sync = sharedPreferences.getBoolean("syncEnabled", false);
            if (sync == PhotoListener.isRunning()) {
                return;
            }
            if (sync) {
                getContext().startService(new Intent(getContext(), PhotoListener.class));
            } else {
                getContext().stopService(new Intent(getContext(), PhotoListener.class));
            }
        }
    }
}
