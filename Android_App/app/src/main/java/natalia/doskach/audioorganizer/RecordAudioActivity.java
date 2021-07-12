package natalia.doskach.audioorganizer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecordAudioActivity extends AppCompatActivity {

    ImageButton buttonRec;
    Chronometer chronometer;

    private static String fileName;
    private MediaRecorder recorder;
    boolean isRecording;

    File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioNotes");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_audio);
        buttonRec = findViewById(R.id.register);
        chronometer = findViewById(R.id.timer);

        isRecording = false;

        askRuntimePermission();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        fileName = path + "/recording_" + date + ".amr";

        if (!path.exists()) {
            path.mkdirs();
        }

        buttonRec.setOnClickListener(v -> {
            if (!isRecording) {
                try {
                    startRecording();
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    isRecording = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Невозможно записать", Toast.LENGTH_SHORT).show();
                }
            } else if (isRecording) {
                stopRecording();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                isRecording = false;
            }
        });
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;
    }

    private void askRuntimePermission() {

        Dexter.withContext(getApplicationContext()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
    }
}