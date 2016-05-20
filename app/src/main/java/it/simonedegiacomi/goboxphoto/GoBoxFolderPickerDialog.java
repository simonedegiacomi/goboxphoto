package it.simonedegiacomi.goboxphoto;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import it.simonedegiacomi.goboxapi.GBFile;
import it.simonedegiacomi.goboxapi.client.ClientException;
import it.simonedegiacomi.goboxapi.client.StandardGBClient;

/**
 * Create a dialog to let the user pick a folder on his storage
 * Created on 12/05/16. *
 * @author Degiacomi Simone
 */
public class GoBoxFolderPickerDialog extends DialogFragment {

    /**
     * Current father on the dialog
     */
    private GBFile currentFather = GBFile.ROOT_FILE;

    /**
     * Recycler view with the folders of the current father
     */
    private RecyclerView recyclerView;

    /**
     * Adapter used to show the children (only the folders) of the current father
     */
    private FolderPickerAdapter adapter;

    /**
     * Client to use to talk with the storage
     */
    private StandardGBClient client;

    /**
     * Loading spinner to show while retrieving the father information
     */
    private ProgressBar loadingSpinner;

    /**
     * Toolbar of the dialog
     */
    private Toolbar toolbar;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set the style
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_AppCompat_NoActionBar);

        // Load the view
        View view = LayoutInflater.from(getContext()).inflate(R.layout.gobox_folder_picker_dialog, null);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_gobox_folder_picker_dialog);
        loadingSpinner = (ProgressBar) view.findViewById(R.id.loading_spinner);
        builder.setView(view);

        // Close dialog button
        builder.setNegativeButton("USE ROOT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onFolderChosen(GBFile.ROOT_FILE);
            }
        });

        // Prepare the toolbar
        toolbar = (Toolbar) view.findViewById(R.id.folder_picker_toolbar);
        toolbar.setTitle("Folder Picker");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFolderByFather(new GBFile(currentFather.getFatherID()));
            }
        });


        // Create the dialog
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        // Prepare the adapter to show the folders
        prepareAdapter();

        // Prepare the client
        client = new StandardGBClient(GBAuthPreferencesUtility.loadFromSharedPreferences(getContext()));

        // Show the children on the root
        showFolderByFather(GBFile.ROOT_FILE);

        return dialog;
    }

    /**
     * Prepare the adapter
     */
    private void prepareAdapter () {
        adapter = new FolderPickerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setOpenFolderClickListener(new FolderPickerAdapter.FolderClickListener() {
            @Override
            public void onClick(GBFile folder) {
                onFolderChosen(folder);
            }
        });

        adapter.setSelectFolderClickListener(new FolderPickerAdapter.FolderClickListener() {
            @Override
            public void onClick(GBFile folder) {
                showFolderByFather(folder);
            }
        });
    }

    private void onFolderChosen(GBFile choosedFolder) {

        // Save the father ID of the folder
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit()
                .putLong("uploadFolderID", choosedFolder.getID())
                .apply();

        // Show a confirm toast
        Toast.makeText(getContext(), "Upload folder updated", Toast.LENGTH_SHORT).show();

        // Disconnect the client
        try {
            client.shutdown();
        } catch (ClientException ex) {}

        // Close the dialog
        dismiss();
    }

    /**
     * Show the children of the specified father
     * @param father Father
     */
    private void showFolderByFather(final GBFile father) {
        new AsyncTask<GBFile, Void, GBFile>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                // Show loading spinner
                loadingSpinner.setVisibility(View.VISIBLE);
            }

            @Override
            protected GBFile doInBackground(GBFile... params) {
                try {

                    // assert that the client is ready
                    if (!client.isReady()) {
                        client.init();
                    }
                    return client.getInfo(params[0]);
                } catch (ClientException ex) {
                    try {
                        // Disconnect the client
                        client.shutdown();
                    } catch (ClientException e) {}
                    return null;
                }
            }

            @Override
            protected void onPostExecute(GBFile detailedFather) {

                // Hide the spinner
                loadingSpinner.setVisibility(View.GONE);

                // Hide the dialog of there was an error
                if (detailedFather == null) {
                    Toast.makeText(getContext(), "Cannot connect to your storage", Toast.LENGTH_SHORT).show();
                    getDialog().dismiss();
                    return;
                }

                // Switch current father
                currentFather = detailedFather;

                // Prepare the toolbar
                if (detailedFather.getID() == GBFile.ROOT_ID) {
                    toolbar.setTitle("Root");
                    toolbar.setNavigationIcon(null);
                } else {
                    toolbar.setTitle(detailedFather.getName());
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
                }

                // Update the adapter
                adapter.setFoldersByFather(detailedFather);
            }

        }.execute(father);
    }
}
