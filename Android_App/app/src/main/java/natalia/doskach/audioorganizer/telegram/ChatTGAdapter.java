package natalia.doskach.audioorganizer.telegram;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import natalia.doskach.audioorganizer.R;

public class ChatTGAdapter extends RecyclerView.Adapter<ChatTGAdapter.ViewHolder> {
    private ArrayList<ChatItems> mData;
    private LayoutInflater mInflater;
    Context context;
    int position;

    // data is passed into the constructor
    ChatTGAdapter(Context context, ArrayList<ChatItems> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.td_chats_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String chat = mData.get(position).title;
        this.position = position;
        holder.chat.setText(chat);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView chat;

        ViewHolder(View itemView) {
            super(itemView);
            chat = itemView.findViewById(R.id.chatTW);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.i(mData.get(getAdapterPosition()).title,"clicked");
            view.setBackgroundColor(view.getContext().getResources().getColor(R.color.light_gray));
            long chat_id = mData.get(getAdapterPosition()).id;
            Example.chat_id = chat_id;
            Example.chatIDLatch.countDown();
        }
    }
}
