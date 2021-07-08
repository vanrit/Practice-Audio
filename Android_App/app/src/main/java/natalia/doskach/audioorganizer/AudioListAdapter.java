package natalia.doskach.audioorganizer;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.ViewHolder> {

    private ArrayList<Audio> localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView twAuthor;
        private final TextView twTitle;
        private final TextView twLength;
        private final ImageButton menuBtn;
        Context context;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            twAuthor = (TextView) view.findViewById(R.id.authorTW);
            twTitle = (TextView) view.findViewById(R.id.nameTW);
            twLength = (TextView) view.findViewById(R.id.lengthTW);
            menuBtn = (ImageButton) view.findViewById(R.id.listMenuBtn);
            menuBtn.setOnClickListener(new ImageButton.OnClickListener() {
                public void onClick(View v) {
                    Log.i("info","openMenu");
                    showPopupMenu(v);
                }
            });

        }
        private void showPopupMenu(View v) {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            popupMenu.inflate(R.menu.audio_list_menu);

            popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                @Override
                public void onDismiss(PopupMenu menu) {
                    Log.i("info","close Menu");
                }
            });

            popupMenu
                    .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.renameBtn:
                                    Log.i("info","rename");
                                    return true;
                                case R.id.downloadRemoveBtn:
                                    Log.i("info","download/remove");
                                    return true;
                                case R.id.deleteBtn:
                                    Log.i("info","delete");
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

            popupMenu.show();
        }


        public TextView getAuthorTW() {
            return twAuthor;
        }

        public TextView getTitleTW() {
            return twTitle;
        }

        public TextView getLengthTW() {
            return twLength;
        }
    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public AudioListAdapter(ArrayList<Audio> dataSet) {
        localDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.audio_list_item, viewGroup, false);


        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getAuthorTW().setText(localDataSet.get(position).author);
        viewHolder.getLengthTW().setText(localDataSet.get(position).len/60+":"+localDataSet.get(position).len%60);
        viewHolder.getTitleTW().setText(localDataSet.get(position).name);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}

