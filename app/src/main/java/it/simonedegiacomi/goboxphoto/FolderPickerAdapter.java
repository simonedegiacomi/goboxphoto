package it.simonedegiacomi.goboxphoto;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import it.simonedegiacomi.goboxapi.GBFile;

/**
 * Adapter to use to show a list of folder in the gobox storage
 * Created on 13/05/16.
 * @author Degiacomi Simone
 */
public class FolderPickerAdapter extends RecyclerView.Adapter<FolderPickerAdapter.FolderPickerViewHolder> {

    /**
     * List of folder to show
     */
    private final List<GBFile> folders = new LinkedList<>();

    @Override
    public FolderPickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_gobox_folder_picker_dialog, parent, false);
        return new FolderPickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderPickerViewHolder holder, int position) {
        holder.setFolder(folders.get(position));
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    /**
     * Remove the current folder and show the children folder of the given father
     * @param father Father to which children show
     */
    public void setFoldersByFather (GBFile father) {

        // Remove old children
        folders.clear();

        // Add only the folders
        for (GBFile child : father.getChildren()) {
            if (child.isDirectory()) {
                folders.add(child);
            }
        }

        // Notify changes
        notifyDataSetChanged();
    }

    /**
     * Set the listener for the open folder action (click on the name of the folder
     * @param listener Listener to call
     */
    public void setOpenFolderClickListener (FolderClickListener listener) {
        FolderPickerViewHolder.setOpenFolderClickListener(listener);
    }

    /**
     * Set the listener for the click on the button on the right of the folder name
     * @param listener Listener to call
     */
    public void setSelectFolderClickListener (FolderClickListener listener) {
        FolderPickerViewHolder.setSelectFolderClickListener(listener);
    }

    public static class FolderPickerViewHolder extends RecyclerView.ViewHolder {

        private static FolderClickListener openFolderClickListener;
        private static FolderClickListener selectFolderClickListener;

        private TextView name;

        private GBFile folder;

        public static void setOpenFolderClickListener (FolderClickListener listener) {
            openFolderClickListener = listener;
        }

        public static void setSelectFolderClickListener (FolderClickListener listener) {
            selectFolderClickListener = listener;
        }

        public FolderPickerViewHolder(View itemView) {
            super(itemView);

            // Get the views
            name = (TextView) itemView.findViewById(R.id.name_recycler_view_item_gobox_folder_picker_dialog);
            ImageView openButton = (ImageView) itemView.findViewById(R.id.open_recycler_view_item_gobox_folder_picker_dialog);

            // Set the listeners
            if (selectFolderClickListener !=  null) {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectFolderClickListener.onClick(folder);
                    }
                });
            }

            if (openFolderClickListener != null) {
                openButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFolderClickListener.onClick(folder);
                    }
                });
            }
        }

        public void setFolder (GBFile folder) {
            this.folder = folder;
            name.setText(folder.getName());
        }
    }

    public interface FolderClickListener {

        /**
         * Method called when the user clicks
         * @param folder Clicked folder
         */
        void onClick (GBFile folder);
    }
}