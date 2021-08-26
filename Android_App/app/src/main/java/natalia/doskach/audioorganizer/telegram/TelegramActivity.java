package natalia.doskach.audioorganizer.telegram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import it.tdlight.common.utils.CantLoadLibrary;
import natalia.doskach.audioorganizer.Audio;
import natalia.doskach.audioorganizer.R;

public class TelegramActivity extends AppCompatActivity {
    FragmentTransaction fTrans;
    InputPhoneFragment f1;
    static Context context;
    ArrayList<ChatItems> chats;
    int page = 1;
    public Audio audio;

    @Override
    public void onBackPressed() {
        if(page == 4)
        {   Example.proceedLatch.countDown();
            Example.proceedLatch = new CountDownLatch(1);
            changeFragmentToChats(chats);

        }
    }

    public void returnAudio(String path){
        Intent data = new Intent();
        audio.path = path;
//---set the data to pass back---
        if(audio == null)
            this.setResult(Activity.RESULT_CANCELED, data);
        else{
            data.putExtra("audio", audio);
            this.setResult(Activity.RESULT_OK, data);}
//---close the activity---
            this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            context = getApplicationContext();
            final Activity a = this;


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram);
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.setReorderingAllowed(true);
        fTrans.add(R.id.telegramLayout, InputPhoneFragment.class, null) //TODO change
                .commit();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Example.main(getApplicationContext(),a);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

}
    public void changeFragmentToChats(ArrayList<ChatItems> i) {
        page = 3;
        chats = i;
        Bundle b = new Bundle();
        b.putSerializable("chats",i);
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.setReorderingAllowed(true);
        fTrans.replace(R.id.telegramLayout,ListOfChatsFragment.class, b).commit();
    }

    public void changeFragmentToAudios(ArrayList<AudioItems> i) {
        page = 4;
        Bundle b = new Bundle();
        b.putSerializable("audios",i);
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.setReorderingAllowed(true);
        fTrans.replace(R.id.telegramLayout,ListOfAudiosFragment.class, b).commit();
    }

    public static void makeAToast(String info) {
        Toast.makeText(context, info, Toast.LENGTH_SHORT).show();
    }

}

class AudioItems implements Serializable {
    int id;
    int senderID;
    String sender;
    String date;
    public AudioItems(int id, int senderID, String sender, String date){
        this.id = id;
        this.senderID = senderID;
        this.sender = sender;
        this.date = date;
    }
}

class ChatItems implements Serializable {
    long id;
    String title;
    public ChatItems(long id, String title){
        this.id = id;
        this.title = title;
    }}

