package natalia.doskach.audioorganizer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Authenticator;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import natalia.doskach.audioorganizer.telegram.TelegramActivity;
import natalia.doskach.audioorganizer.whatsapp.OpenWhatsAppActivity;

//activity for a list of recordings
public class MainActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    static AudiosList audios;
    AudioListAdapter a;
    RecyclerView list;
    ImageButton menuBtn;
    public static String FilePath, FileName;
    com.google.android.material.textfield.TextInputEditText input;
    boolean isFloatingMenuOpen = false;
    ActivityResultLauncher<Intent> audioActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    Audio au = (Audio)(data.getSerializableExtra("audio"));
                    a.addItem(au);
                }
                if(result.getResultCode() == Activity.RESULT_FIRST_USER) {
                    //synch with server
                    checkLog();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkLog();
        Log.i("onCreate","onCreate");
        resetPlayingTune();
        audios = new AudiosList();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        if(savedInstanceState != null){
            Log.i("info","got data");
            String data = savedInstanceState.getString("audios");
            audios.changeData(new Gson().fromJson(data, new TypeToken<List<Audio>>(){}.getType()));
        }
        else if(getDataFromFile()){

        }
        else{
            Log.i("info","no data");
        audios.add(new Audio("test1","unknown",10,"/storage/emulated/0/Download/Test1.m4a",true));

        audios.add(new Audio("test2","unknown",10,"/storage/emulated/0/Download/Test2.m4a",true));

        audios.add(new Audio("test3","unknown",10,"/storage/emulated/0/Download/Test3.m4a",true));
        }
        list = findViewById(R.id.audioList);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(list.getContext().getResources().getDrawable(R.drawable.sk_line_divider));
        list.addItemDecoration(dividerItemDecoration);
        a = new AudioListAdapter(audios.getAudios());
        list.setAdapter(a);
        list.setLayoutManager(new LinearLayoutManager(this));
        a.notifyDataSetChanged();
        menuBtn = findViewById(R.id.overflow_menu);
        menuBtn.setOnClickListener(v -> {
            Log.i("info","openMenu");
            showPopupMenu(v);
        });
        input = findViewById(R.id.input);
        input.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                Log.i("TEXT","CHANGED");
                String value = ((EditText)input).getText().toString();
                a.getFilter().filter(value);
            }
        });
        input.setOnEditorActionListener((v, actionId, event) -> {
            if(actionId== EditorInfo.IME_ACTION_DONE){
                Log.i("info","finished writing");
                getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                );

            }
            return false;
        });

        Button fab1 = findViewById(R.id.fab1);

        fab1.setOnClickListener(v -> {

            Intent intent = new Intent(MainActivity.this, GetFileActivity.class);
            startActivity(intent);

//                if (!FilePath.isEmpty()) {
//                    audios.add(new Audio(FileName, "unknown", 10, FilePath, true));
//                    FileName = "";
//                    FilePath = "";
//                }

            // Показываем все программы для запуска
//                Intent intent = new Intent(Intent.ACTION_PICK);
//                intent.setType("audio/*");
//                startActivityForResult(intent, 1);
//
//                String FilePath = intent.getData().getPath();
//                String FileName = intent.getData().getLastPathSegment();

//                Audio audio = new Audio(FileName, "unknown",10, FilePath,true);
//
//                Intent data = new Intent();
//                if(audio == null)
//                    setResult(RESULT_CANCELED, data);
//                else{
//                    data.putExtra("audio", audio);
//                    setResult(RESULT_OK, data);}
//                finish();
        });

        Button fab3 = findViewById(R.id.fab3);

        fab3.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OpenWhatsAppActivity.class);
            startActivity(intent);
        });
    }

    public void checkLog() {
        //TODO fix 401 error when logged in
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://84.201.143.25:8080/audios/all";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                     Log.i("log",response.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof ServerError)
                    Log.e("server-error", String.valueOf(error.networkResponse.statusCode));
                if(error instanceof AuthFailureError)
                {Log.e("auth-error", String.valueOf(error.networkResponse.statusCode));
                    lauchLogin();
                }
                if(error instanceof NetworkError)
                    Log.e("net-error", String.valueOf(error));
            }
        });
        queue.add(jsonObjectRequest);
    }

    void lauchLogin(){
        audioActivityResultLauncher.launch(new Intent(this, LoginActivity.class));

    }

    @Override
    protected void onStop() {
        Log.i("onStop","onStop");
        super.onStop();
        String filename = "data";
        String fileContents = new Gson().toJson(a.getAudios());
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected boolean getDataFromFile(){
        String contents;
        FileInputStream fis = null;
        try {
            fis = getApplicationContext().openFileInput("data");
        } catch (FileNotFoundException e) {
            Log.i("info","file not found");
            e.printStackTrace();
        }
        if(fis!=null){
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                contents = stringBuilder.toString();
            }
            audios.changeData(new Gson().fromJson(contents, new TypeToken<List<Audio>>(){}.getType()));
        return true;}
        return false;

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        Log.i("info","onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putString("audios", new Gson().toJson(a.getAudios()));
    }

    private void resetPlayingTune() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(String.valueOf((R.string.playing_tune)), -1);
        editor.apply();
    }

    private void showPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.overflow_menu);

        popupMenu.setOnDismissListener(menu -> Log.i("info","close Menu"));
        popupMenu.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.overflow_menu, menu);
        return true;
    }

    public void openRecordAudio(View view) {
        if(!mediaPlayer.isPlaying())
        audioActivityResultLauncher.launch(new Intent(this, RecordAudioActivity.class));
    }

//    public void importFromFile(View view) {
//        File path = this.getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
//        File f = new File(path,"Test1.mp4");
//    }

    public void openTelegramImport(View view) {
        if(!mediaPlayer.isPlaying())
        audioActivityResultLauncher.launch(new Intent(this, TelegramActivity.class));
    }

    public void openWhatsAppImport(View view) {
        Log.i("info","import from whatsapp");
    }

    public void toggleMenu(View view) {
        FloatingActionButton fab0 = findViewById(R.id.fab0);
        ExtendedFloatingActionButton fab1 = findViewById(R.id.fab1);
        ExtendedFloatingActionButton fab2 = findViewById(R.id.fab2);
        ExtendedFloatingActionButton fab3 = findViewById(R.id.fab3);
        ExtendedFloatingActionButton fab4 = findViewById(R.id.fab4);
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
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://84.201.143.25:8080/logout";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        checkLog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof ServerError)
                    Log.e("server-error", String.valueOf(error.networkResponse.statusCode));
                if(error instanceof AuthFailureError)
                {Log.e("auth-error", String.valueOf(error.networkResponse.statusCode));
                    Toast.makeText(getApplicationContext(), "Невозможно выйти из системы", Toast.LENGTH_SHORT).show();
                }
                if(error instanceof NetworkError)
                    Toast.makeText(getApplicationContext(), "Проблемы с интернетом", Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void synchronize(MenuItem item) {
    }

    public void playTune(String path) throws IOException {
        Log.i("play","tune");
        Uri myUri = Uri.fromFile(new File(path));
        
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );
        mediaPlayer.setDataSource(getApplicationContext(), myUri);
        mediaPlayer.prepare();
        mediaPlayer.start();
    }

    public void pauseTune(int position) {
        View v = Objects.requireNonNull(list.getLayoutManager()).findViewByPosition(position);
        ImageButton playBtn = Objects.requireNonNull(v).findViewById(R.id.playBtn);
        playBtn.setImageResource(R.drawable.ic_play_circle_48);
        Log.i("pause","tune");
        if(mediaPlayer.isPlaying())
        mediaPlayer.stop();
        mediaPlayer.release();
//        mediaPlayer.seekTo(0);
    }

}