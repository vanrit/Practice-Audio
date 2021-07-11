package natalia.doskach.audioorganizer;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.ViewHolder> {

    private ArrayList<Audio> localDataSet;
    private int playingTune = -1;

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
        viewHolder.getMenuBtn().setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                Log.i("info","openMenu");
                showPopupMenu(viewHolder,v,position);
            }
        });
        if(localDataSet.get(position).isDownloaded)
            viewHolder.getPlayBtn().setImageResource(R.drawable.ic_play_circle_48);
        else
            viewHolder.getPlayBtn().setImageResource(R.drawable.ic_arrow_circle_down_48);

        viewHolder.getPlayBtn().setOnClickListener(new View.OnClickListener() {
                                                       public void onClick(View v) {
                                                           if (localDataSet.get(position).isDownloaded)
                                                               playSong(viewHolder, v, position);
                                                           else
                                                               downloadSong(viewHolder, v, position);

                                                       }});
    }

    private void showPopupMenu(ViewHolder viewHolder, View v,int position) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.audio_list_menu);
        if(localDataSet.get(position).isDownloaded)
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

    private void downloadSong(ViewHolder viewHolder, View v, int position) {
        Log.i("info","download song");
        localDataSet.get(position).isDownloaded = true;
        viewHolder.getPlayBtn().setImageResource(R.drawable.ic_play_circle_48);
    }

    private void playSong(ViewHolder viewHolder, View v, final int position) {
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
        ((MainActivity)v.getContext()).playTune();
    }

    private void playTune(ViewHolder viewHolder, View v, int position) {
        ((MainActivity)v.getContext()).pauseTune();
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}

