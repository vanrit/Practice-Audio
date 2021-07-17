package natalia.doskach.audioorganizer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecordAudioActivity extends AppCompatActivity {

    private static final String TAG = "111";
    ImageButton buttonRec;
    ImageButton backBtn;
    Chronometer chronometer;
    String recordFile;

    private EditText editText;
    private MediaRecorder recorder;
    boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        buttonRec = findViewById(R.id.register);
        backBtn = findViewById(R.id.back);
        chronometer = findViewById(R.id.timer);
        editText = findViewById(R.id.editAudioName);

        isRecording = false;

        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RecordAudioActivity.this, MainActivity.class);
            startActivity(intent);
        });

        buttonRec.setOnClickListener(v -> {
            if (isRecording) {
                recordStop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                isRecording = false;
                MainActivity.audios.add(new Audio(recordFile, "unknown",10,"",false));
            } else {
                if (askRuntimePermission()) {
                    try {
                        recordStart();
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        isRecording = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Невозможно записать", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean askRuntimePermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permissions is granted");
            return true;
        } else {

            Log.v(TAG, "Permissions is revoked");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
            return false;
        }
    }

    public void recordStart() {

        String recordPath = getExternalFilesDir("").getAbsolutePath();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        Date now = new Date();

        String fileName = editText.getText().toString();
        recordFile = "Recording_" + format.format(now) + ".amr";

        if (!fileName.isEmpty()) recordFile = fileName + ".amr";

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(recordPath + "/" + recordFile);

        try {
            recorder.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }

        recorder.start();
    }

    public void recordStop() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }
}