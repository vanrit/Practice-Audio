package natalia.doskach.audioorganizer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
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
    private int PERMISSION_CODE = 15;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
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

        buttonRec.setOnClickListener(v -> {
            if (isRecording) {
                recordStop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                isRecording = false;
            } else {
//                if (askRuntimePermission()) {
                    try {
                        recordStart();
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.start();
                        isRecording = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Невозможно записать", Toast.LENGTH_SHORT).show();
                    }
//                }
            }
        });

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        fileName = path + "/recording_" + date + ".amr";

        if (!path.exists()) {
            path.mkdirs();
        }
    }

    public void recordStart() {
        try {
            releaseRecorder();

            File outFile = new File(fileName);
            if (outFile.exists()) {
                outFile.delete();
            }

            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(fileName);
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void recordStop() {
        if (recorder != null) {
            recorder.stop();
        }
    }

    private void releaseRecorder() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    private void releasePlayer() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        releaseRecorder();
    }

//    private void startRecording() {
//        recorder = new MediaRecorder();
//        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
//        recorder.setOutputFile(fileName);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
//
//        try {
//            recorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        recorder.start();
//    }
//
//    private void stopRecording() {
//        recorder.stop();
//        recorder.release();
//        recorder = null;
//    }
//
//    private boolean askRuntimePermission() {
//
//        if (ActivityCompat.checkSelfPermission(getBaseContext(), recordPermission) == PackageManager.PERMISSION_GRANTED)
//            return true;
//        else {
//            ActivityCompat.requestPermissions(getParent(), new String[]{recordPermission}, PERMISSION_CODE);
//            return false;
//        }

//        Dexter.withContext(getApplicationContext()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.RECORD_AUDIO).withListener(new MultiplePermissionsListener() {
//            @Override
//            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//                Toast.makeText(getApplicationContext(), "Разрешено", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//                permissionToken.continuePermissionRequest();
//            }
//        }).check();
//    }
}