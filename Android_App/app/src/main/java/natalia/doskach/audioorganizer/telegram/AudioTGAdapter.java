package natalia.doskach.audioorganizer.telegram;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;

import natalia.doskach.audioorganizer.Audio;
import natalia.doskach.audioorganizer.R;

public class AudioTGAdapter extends RecyclerView.Adapter<AudioTGAdapter.ViewHolder> {
    private ArrayList<AudioItems> mData;
    private LayoutInflater mInflater;

    // data is passed into the constructor
    AudioTGAdapter(Context context, ArrayList<AudioItems> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.td_audios_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String sender = mData.get(position).sender;
        String date = mData.get(position).date;
        holder.sender.setText(sender);
        holder.date.setText(date);
        TdApi.GetUser user = new TdApi.GetUser(mData.get(position).senderID);
        Example.getClient().send(user, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object instanceof TdApi.User) {
                    holder.sender.setText(((TdApi.User) object).username);
                    notifyItemChanged(position);
                    return;
                }
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView sender;
        TextView date;

        ViewHolder(View itemView) {
            super(itemView);
            sender = itemView.findViewById(R.id.senderTW);
            date = itemView.findViewById(R.id.dateTW);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.i("audio","clicked");
            view.setBackgroundColor(view.getContext().getResources().getColor(R.color.light_gray));
            Audio audio = new Audio("TG "+date.getText().toString(),sender.getText().toString(),4,"",true);
            Log.i("audio chosen",audio.name);
            ((TelegramActivity)view.getContext()).audio = audio;
            Example.audio_id = mData.get(getAdapterPosition()).id;
            Example.chooseAChat = false;
            Example.proceedLatch.countDown();

        }
    }
}
