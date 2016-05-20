package it.simonedegiacomi.goboxphoto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class GoBoxPhoto extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_go_box_photo);

        // Assert that the user is logged
        assertLogged();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, new GoBoxPreferenceFragment())
                .commit();

    }

    private void assertLogged () {
        if (!GBAuthPreferencesUtility.isLogged(this)) {

            // Start login activity
            Intent startLogin = new Intent(this, LoginActivity.class);
            startActivity(startLogin);

            // Close this activity
            finish();
        }
    }
}
