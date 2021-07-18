package natalia.doskach.audioorganizer;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.ViewHolder> implements Filterable {

    private ArrayList<Audio> localDataSet;
    private ArrayList<Audio> filteredDataSet;
    private int playingTune = -1;
    AlertDialog dialog;
    private ItemFilter mFilter = new ItemFilter();

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            if(filterString.isEmpty())
            {results.values = localDataSet;
            return results;
            }
            final List<Audio> list = localDataSet;
            int count = list.size();
            final ArrayList<Audio> nlist = new ArrayList<Audio>(count);
            String filterableString ;
            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).name;
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredDataSet = (ArrayList<Audio>) results.values;
            notifyDataSetChanged();
            Log.i("info","data changed");
        }

    }
    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView twAuthor;
        private final TextView twTitle;
        private final TextView twLength;
        private final ImageButton menuBtn;
        private final ImageButton playBtn;
        private final ConstraintLayout card;
        private final MenuItem downloadRemoveBtn;
        MediaPlayer mediaPlayer;
        Context context;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            twAuthor = (TextView) view.findViewById(R.id.authorTW);
            twTitle = (TextView) view.findViewById(R.id.nameTW);
            twLength = (TextView) view.findViewById(R.id.lengthTW);
            menuBtn = (ImageButton) view.findViewById(R.id.listMenuBtn);
            playBtn = (ImageButton) view.findViewById(R.id.playBtn);
            card = (ConstraintLayout) view.findViewById(R.id.itemCard);
            downloadRemoveBtn = (MenuItem) view.findViewById(R.id.downloadRemoveBtn);


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

        public ImageButton getPlayBtn() {
            return playBtn;
        }

        public ImageButton getMenuBtn() {
            return menuBtn;
        }

        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }

        public MenuItem getDownloadRemoveBtn() {
            return downloadRemoveBtn;
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
        filteredDataSet = dataSet;
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
        viewHolder.getAuthorTW().setText(filteredDataSet.get(position).author);
        viewHolder.getLengthTW().setText(filteredDataSet.get(position).len/60+":"+filteredDataSet.get(position).len%60);
        viewHolder.getTitleTW().setText(filteredDataSet.get(position).name);
        viewHolder.getMenuBtn().setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                Log.i("info","openMenu");
                showPopupMenu(viewHolder,v,position);
            }
        });
        if(filteredDataSet.get(position).isDownloaded)
            viewHolder.getPlayBtn().setImageResource(R.drawable.ic_play_circle_48);
        else
            viewHolder.getPlayBtn().setImageResource(R.drawable.ic_arrow_circle_down_48);

        viewHolder.getPlayBtn().setOnClickListener(new View.OnClickListener() {
                                                       public void onClick(View v) {
                                                           if (filteredDataSet.get(position).isDownloaded) {
                                                               try {
                                                                   playSong(viewHolder, v, position);
                                                               } catch (IOException e) {
                                                                   e.printStackTrace();
                                                               }
                                                           }
                                                           else
                                                               downloadSong(viewHolder, v, position);

                                                       }});
    }

    private void showPopupMenu(ViewHolder viewHolder, View v,int position) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.audio_list_menu);
        if(filteredDataSet.get(position).isDownloaded)
            popupMenu.getMenu().findItem(R.id.downloadRemoveBtn).setTitle("удалить файл");
        else
            popupMenu.getMenu().findItem(R.id.downloadRemoveBtn).setTitle("скачать файл");
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
                                renameItem(position,v);
                                return true;
                            case R.id.downloadRemoveBtn:
                                Log.i("info","download/remove");
                                if(!filteredDataSet.get(position).isDownloaded)
                                    downloadSong(viewHolder,v,position);
                                else{
                                    removeFile(position, viewHolder);
                                }
                                return true;
                            case R.id.deleteBtn:
                                Log.i("info","delete");
                                deleteItem(position);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }

    private void removeFile(int position, ViewHolder viewHolder) {
        //TODO: remove File and delete item if not on server
        Log.i("info","remove song");
        filteredDataSet.get(position).isDownloaded = false;
        notifyItemChanged(position);
        viewHolder.getPlayBtn().setImageResource(R.drawable.ic_arrow_circle_down_48);
    }

    private void deleteItem(int position) {
        filteredDataSet.remove(position);
        notifyItemRemoved(position);
    }

    private void renameItem(int position, View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(((Activity)v.getContext()));
        builder.setTitle(R.string.dialog_title);
        LayoutInflater inflater = ((Activity)v.getContext()).getLayoutInflater();
        View moduleWindow = inflater.inflate(R.layout.dialog_rename, null);
        builder.setView(moduleWindow);
        EditText form= (EditText) moduleWindow.findViewById(R.id.renameET);
        form.setText(filteredDataSet.get(position).name);
        Button yesButton= (Button) moduleWindow.findViewById(R.id.YESbutton);
        Button noButton= (Button) moduleWindow.findViewById(R.id.NObutton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User clicked OK button
                String text = form.getText().toString();
                filteredDataSet.get(position).name = text;
                notifyItemChanged(position);
                dialog.dismiss();
            }});
        noButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }});
        dialog = builder.show();

    }

    private void downloadSong(ViewHolder viewHolder, View v, int position) {
        Log.i("info","download song");
        filteredDataSet.get(position).isDownloaded = true;
        notifyItemChanged(position);
        viewHolder.getPlayBtn().setImageResource(R.drawable.ic_play_circle_48);
    }

    private void playSong(ViewHolder viewHolder, View v, final int position) throws IOException {
        if(playingTune==-1){ //start playing music
            Log.i("info","start playing music pos:"+position+" pt:"+playingTune);
            playingTune = position;
            playTune(viewHolder,v, position);
        }
        else if(playingTune==position){ //pause music
            Log.i("info","pause music pos:"+position+" pt:"+playingTune);
            playingTune = -1;
            stopTune(viewHolder,v,position);
        }
        else{ //change music
            Log.i("info","change music pos:"+position+" pt:"+playingTune);
            stopTune(viewHolder,v,playingTune);
            playingTune = position;
            playTune(viewHolder,v, position);
        }
    }

    private void stopTune(ViewHolder viewHolder, View v, int position) {
        ((MainActivity)v.getContext()).pauseTune(position);
    }

    private void playTune(ViewHolder viewHolder, View v, int position) throws IOException {
        viewHolder.getPlayBtn().setImageResource(R.drawable.ic_pause_circle_48);
        ((MainActivity)v.getContext()).playTune(filteredDataSet.get(position).path);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredDataSet.size();
    }
}

