package natalia.doskach.audioorganizer;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

//activity for a list of recordings
public class MainActivity extends AppCompatActivity {

    ArrayList<Audio> audios;
    AudioListAdapter a;
    RecyclerView list;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        audios = new ArrayList<>();
        audios.add(new Audio("woof","dog",10,"woof.mp3"));
        audios.add(new Audio("woof","dog",10,"woof.mp3"));
        audios.add(new Audio("woof","dog",10,"woof.mp3"));
        audios.add(new Audio("woof","dog",10,"woof.mp3"));
        audios.add(new Audio("woof","dog",10,"woof.mp3"));
        audios.add(new Audio("woof","dog",10,"woof.mp3"));
        list = findViewById(R.id.audioList);
        a = new AudioListAdapter(audios);
        list.setAdapter(a);
        list.setLayoutManager(new LinearLayoutManager(this));
        a.notifyDataSetChanged();
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
}