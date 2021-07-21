package natalia.doskach.audioorganizer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Objects;

public class GetFileActivity extends AppCompatActivity {

//    TextView textView1, textView2;
//    String FilePath;
//    String FileName;
    ImageButton backBtn;
    Audio audio;

    private static final int PICKFILE_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_file);

        backBtn = findViewById(R.id.back);
        ImageButton buttonPick = findViewById(R.id.button123);
//        textView1 = findViewById(R.id.text123);
//        textView2 = findViewById(R.id.text124);

        buttonPick.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(intent,PICKFILE_RESULT_CODE);
        });

        backBtn.setOnClickListener(v -> {
            Intent data = new Intent();
//---set the data to pass back---
            if(audio == null)
                setResult(RESULT_CANCELED, data);
            else{
                data.putExtra("audio", audio);
                setResult(RESULT_OK, data);}
//---close the activity---
            finish();

//            Intent intent = new Intent(GetFileActivity.this, MainActivity.class);
//            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                MainActivity.FilePath = data.getData().getPath();
//                textView1.setText(MainActivity.FilePath);
                MainActivity.FileName = data.getData().getLastPathSegment();
//                textView2.setText(MainActivity.FileName);
                audio = new Audio(MainActivity.FileName, "unknown", 10, MainActivity.FilePath, true);
//                File original = new File(MainActivity.FilePath);
//                File copied = new File(getExternalFilesDir("").getAbsolutePath());
//                try (
//                        InputStream in = new BufferedInputStream(
//                                new FileInputStream(original));
//                        OutputStream out = new BufferedOutputStream(
//                                new FileOutputStream(copied))) {
//
//                    byte[] buffer = new byte[1024];
//                    int lengthRead;
//                    while ((lengthRead = in.read(buffer)) > 0) {
//                        out.write(buffer, 0, lengthRead);
//                        out.flush();
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                File sourceFile = new File(MainActivity.FilePath);
//                File destDir = new File(getExternalFilesDir("").getAbsolutePath() + "/" + MainActivity.FileName + ".mp3");
                copyFileOrDirectory(MainActivity.FilePath, getExternalFilesDir("").getAbsolutePath());
//                try {
//                    copyFileUsingStream(sourceFile, destDir);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        //copyAsset(MainActivity.FilePath, MainActivity.FileName);
//                        copyDir();
//                    }
//                    Intent intent = new Intent();
////---set the data to pass back---
//                    if(audio == null)
//                        setResult(RESULT_CANCELED, intent);
//                    else{
//                        data.putExtra("audio", audio);
//                        setResult(RESULT_OK, intent);}
////---close the activity---
//                    finish();
//                    MainActivity.audios.add(audio);
            }
        }

//        audio = new Audio(FileName, "unknown",10, FilePath,true);
//
//        Intent intent = new Intent();
////---set the data to pass back---
//        if(audio == null)
//            setResult(RESULT_CANCELED, intent);
//        else{
//            intent.putExtra("audio", audio);
//            setResult(RESULT_OK, intent);}
////---close the activity---
//        finish();
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
//
//    private static void copyFileUsingStream(File source, File dest) throws IOException {
//        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = is.read(buffer)) > 0) {
//                os.write(buffer, 0, length);
//            }
//        }
//    }
//
//    private static void copyDir(String sourceDirName, String targetSourceDir) throws IOException {
//        File folder = new File(sourceDirName);
//
//        File[] listOfFiles = folder.listFiles();
//
//        Path destDir = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            destDir = Paths.get(targetSourceDir);
//        }
//        if (listOfFiles != null)
//            for (File file : listOfFiles)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    Files.copy(file.toPath(), destDir.resolve(file.getName()));
//                }
//
//    }

//    private void copyAsset(String filePath, String fileName) {
//
//        File file = new File(filePath);
//
//        String sourceDirName = file.getParent();
//
//        File folder = new File(sourceDirName);
//
//        File[] listOfFiles = folder.listFiles();
//
//        Path destDir = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            destDir = Paths.get(getExternalFilesDir("").getAbsolutePath());
//        }
//        if (listOfFiles != null)
//            for (File file1 : listOfFiles) {
//                try {
//                    if (file1.getName().equals(fileName))
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            Files.copy(file1.toPath(), destDir.resolve(file1.getName()));
//                        }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
////        File file = new File(filePath);
////        File folder = new File(file.getParent());
////
////        Path destDir = Paths.get(getExternalFilesDir("").getAbsolutePath());
////        Files.copy(fileName.toPath(), destDir.resolve(file.getName()), StandardCopyOption.REPLACE_EXISTING);
//
////        String dirPath = getExternalFilesDir("").getAbsolutePath();
////        AssetManager assetManager = getAssets();
////        InputStream in = null;
////        OutputStream out = null;
////        try {
////            in = assetManager.open(fileName);
////            File outFile = new File(filePath, fileName);
////            out = new FileOutputStream(outFile);
////            copyFile(in, out);
////        } catch (IOException e) {
////            e.printStackTrace();
////        } finally {
////            if (in != null) {
////                try {
////                    in.close();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////
////            if (out != null) {
////                try {
////                    out.close();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        }
//    }
//
//    private  void copyFile(InputStream in, OutputStream out) throws IOException {
//        byte[] buffer = new byte[1024];
//        int read;
//        while((read = in.read(buffer)) != -1) {
//            out.write(buffer, 0, read);
//        }
//    }
}