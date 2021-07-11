package natalia.doskach.audioorganizer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//activity for a list of recordings
public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    ArrayList<Audio> audios;
    AudioListAdapter a;
    RecyclerView list;
    ImageButton menuBtn;
    boolean isFloatingMenuOpen = false;
    ActivityResultLauncher<Intent> audioActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        resetPlayingTune();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = MediaPlayer.create((Activity)this, R.raw.sound);
        audios = new ArrayList<>();
        audios.add(new Audio("woof","dog",10,"",false));
        audios.add(new Audio("woof","dog",10,"/storage/emulated/0/Download/Test1.m4a",true));

        audios.add(new Audio("woof","dog",10,"/storage/emulated/0/Download/Test2.m4a",true));

        audios.add(new Audio("woof","dog",10,"/storage/emulated/0/Download/Test3.m4a",true));

        audios.add(new Audio("woof","dog",10,"",false));

        audios.add(new Audio("woof","dog",10,"3",false));
        audios.add(new Audio("woof","dog",100,"",false));

        audios.add(new Audio("woof","dog",100,"3",false));
        list = findViewById(R.id.audioList);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(list.getContext().getResources().getDrawable(R.drawable.sk_line_divider));
        list.addItemDecoration(dividerItemDecoration);
        a = new AudioListAdapter(audios);
        list.setAdapter(a);
        list.setLayoutManager(new LinearLayoutManager(this));
        a.notifyDataSetChanged();
        menuBtn = (ImageButton) findViewById(R.id.overflow_menu);
        menuBtn.setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                Log.i("info","openMenu");
                showPopupMenu(v);
            }
        });
    }

    private void resetPlayingTune() {
        SharedPreferences sharedPref = ((Activity)this).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(String.valueOf((R.string.playing_tune)), -1);
        editor.apply();
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.overflow_menu);

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                Log.i("info","close Menu");
            }
        });
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, menu);
        return true;
    }

    public void openRecordAudio(View view) {
        audioActivityResultLauncher.launch(new Intent(this, RecordAudioActivity.class));
    }

    public void importFromFile(View view) {
    }

    public void openTelegramImport(View view) {
        Log.i("info","import from telegram");
    }

    public void openWhatsAppImport(View view) {
        Log.i("info","import from whatsapp");
    }

    public void toggleMenu(View view) {
        FloatingActionButton fab0 = (FloatingActionButton) findViewById(R.id.fab0);
        ExtendedFloatingActionButton fab1 = (ExtendedFloatingActionButton) findViewById(R.id.fab1);
        ExtendedFloatingActionButton fab2 = (ExtendedFloatingActionButton) findViewById(R.id.fab2);
        ExtendedFloatingActionButton fab3 = (ExtendedFloatingActionButton) findViewById(R.id.fab3);
        ExtendedFloatingActionButton fab4 = (ExtendedFloatingActionButton) findViewById(R.id.fab4);
        if(!isFloatingMenuOpen){
            fab0.setImageResource(R.drawable.ic_close);
            fab1.setVisibility(View.VISIBLE);
            fab2.setVisibility(View.VISIBLE);
            fab3.setVisibility(View.VISIBLE);
            fab4.setVisibility(View.VISIBLE);
        }
        else{
            fab0.setImageResource(R.drawable.ic_add);
            fab1.setVisibility(View.INVISIBLE);
            fab2.setVisibility(View.INVISIBLE);
            fab3.setVisibility(View.INVISIBLE);
            fab4.setVisibility(View.INVISIBLE);
        }
        isFloatingMenuOpen = !isFloatingMenuOpen;
    }

    public void logout(MenuItem item) {
    }

    public void synchronize(MenuItem item) {
    }

    public void playTune() {
        Log.i("play","tune");
        mediaPlayer.start();
    }

    public void pauseTune() {
        Log.i("pause","tune");
        if(mediaPlayer.isPlaying())
        mediaPlayer.stop();
        mediaPlayer.seekTo(0);
    }


}