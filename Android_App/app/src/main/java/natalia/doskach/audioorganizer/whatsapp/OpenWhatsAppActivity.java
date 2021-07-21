package natalia.doskach.audioorganizer.whatsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Objects;

import natalia.doskach.audioorganizer.Audio;
import natalia.doskach.audioorganizer.MainActivity;
import natalia.doskach.audioorganizer.R;

public class OpenWhatsAppActivity extends AppCompatActivity {

    ImageButton button;
    ImageButton backBtn;
    Audio audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_whats_app);

        backBtn = findViewById(R.id.back);
        button = findViewById(R.id.button123);

        backBtn.setOnClickListener(v -> {
            Intent data = new Intent();
            if(audio == null)
                setResult(RESULT_CANCELED, data);
            else{
                data.putExtra("audio", audio);
                setResult(RESULT_OK, data);}
            finish();
        });

        button.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Выберите \"Проводник\"", Toast.LENGTH_SHORT).show();
            openFolder();
        });
    }

    public void openFolder(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
                +  File.separator + "WhatsApp/Media/WhatsApp Voice Notes" + File.separator);
        intent.setDataAndType(uri, "resource/folder");
        startActivityForResult(Intent.createChooser(intent, "Open folder"),1);
//        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                MainActivity.FilePath = data.getData().getPath();
                MainActivity.FileName = data.getData().getLastPathSegment();
                copyFileOrDirectory(MainActivity.FilePath, getExternalFilesDir("").getAbsolutePath());
            }
        }
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {

        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());

            if (src.isDirectory()) {

                String[] files = src.list();
                assert files != null;
                for (String file : files) {
                    String src1 = (new File(src, file).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);

                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!Objects.requireNonNull(destFile.getParentFile()).exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }
}