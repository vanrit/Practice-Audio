package natalia.doskach.audioorganizer.telegram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;

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

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import natalia.doskach.audioorganizer.R;

public class TelegramActivity extends AppCompatActivity {
    FragmentTransaction fTrans;
    InputPhoneFragment f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram);
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.setReorderingAllowed(true);

        fTrans.add(R.id.telegramLayout, InputPhoneFragment.class, null)
                .commit();


}
    public void changeFragmentToChats(ArrayList<ChatItems> i) {
        Bundle b = new Bundle();
        b.putSerializable("chats",i);
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.setReorderingAllowed(true);
        fTrans.replace(R.id.telegramLayout,ListOfChatsFragment.class, b).commit();
    }

    public void changeFragmentToAudios(ArrayList<AudioItems> i) {
        Bundle b = new Bundle();
        b.putSerializable("audios",i);
        fTrans = getSupportFragmentManager().beginTransaction();
        fTrans.setReorderingAllowed(true);
        fTrans.replace(R.id.telegramLayout,ListOfAudiosFragment.class, b).commit();
    }


}

class AudioItems {
    String id;
    String senderID;
    String sender;
    String date;
    public AudioItems(String id, String senderID, String sender,String date){
        this.id = id;
        this.senderID = senderID;
        this.sender = sender;
        this.date = date;
    }
}

class ChatItems {
    String id;
    String title;
    public ChatItems(String id, String title){
        this.id = id;
        this.title = title;
    }}

