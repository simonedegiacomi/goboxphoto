package it.simonedegiacomi.goboxphoto;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import it.simonedegiacomi.goboxapi.authentication.GBAuth;
import it.simonedegiacomi.goboxapi.utils.URLBuilder;

/**
 * Created on 06/05/16.
 * @author Degiacomi Simone
 */
public class LoginActivity extends AppCompatActivity {

    private TextView username;
    private TextView password;
    private TextView registerLink;
    private Button loginButton;

    private final GBAuth auth = new GBAuth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout
        setContentView(R.layout.login_activity);

        // Assert that the user is not logged
        if (GBAuthPreferencesUtility.isLogged(this)) {
            startMainActivity();
        }

        // Get the input fields
        username = (TextView) findViewById(R.id.username_input);
        password = (TextView) findViewById(R.id.password_input);

        loginButton = (Button) findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Prepare the auth object
                auth.setMode(GBAuth.Modality.CLIENT);
                auth.setUsername(username.getText().toString());

                // Prepare a simple progress dialog
                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Authenticating...");

                new AsyncTask<String, Void, Boolean>() {

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();

                        // Show the dialog
                        progressDialog.show();
                    }

                    @Override
                    protected Boolean doInBackground(String[] params) {
                        try {
                            return auth.login(params[0]);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean logged) {

                        // Hide the dialog
                        progressDialog.hide();
                        progressDialog.dismiss();

                        if (logged) {
                            GBAuthPreferencesUtility.saveToSharedPreferences(auth, LoginActivity.this);
                            startMainActivity();

                            return;
                        }

                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Error")
                                .setMessage("Invalid username or password")
                                .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                }.execute(password.getText().toString());
            }
        });

        registerLink = (TextView) findViewById(R.id.link_signup);

        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(URLBuilder.DEFAULT.get("webapp").toString()));
                startActivity(openBrowser);
            }
        });
    }

    private void startMainActivity () {
        Intent main = new Intent(this, GoBoxPhoto.class);
        startActivity(main);
        finish();
    }
}