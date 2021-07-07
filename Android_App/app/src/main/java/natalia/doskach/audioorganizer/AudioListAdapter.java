package natalia.doskach.audioorganizer;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            twAuthor = (TextView) view.findViewById(R.id.authorTW);
            twTitle = (TextView) view.findViewById(R.id.nameTW);
            twLength = (TextView) view.findViewById(R.id.lengthTW);
            menuBtn = (ImageButton) view.findViewById(R.id.listMenuBtn);
            menuBtn.setOnClickListener(new ImageButton.OnClickListener() {
                public void onClick(View v) {
                    //open menu
                }
            });
        }

        void openMenu(View v){

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

