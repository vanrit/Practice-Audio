package natalia.doskach.audioorganizer.telegram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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

import natalia.doskach.audioorganizer.R;

public class TelegramActivity extends AppCompatActivity {
    ConstraintLayout mainLayout;
    TextView prompt;
    TextInputEditText text;
    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telegram);
        mainLayout = findViewById(R.id.telegramLayout);
        prompt = findViewById(R.id.prompt);
        text = findViewById(R.id.textInput);
        pb = findViewById(R.id.progressBar);
        pb.setVisibility(View.INVISIBLE);
        text.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId== EditorInfo.IME_ACTION_DONE){
                    Log.i("info","finished writing");
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                    );
                    pb.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        public void run() {
                            sendPhone(text.getText().toString(),pb);
                        }
                    }).start();
                    pb.setVisibility(View.INVISIBLE);

                }
                return false;
            }
        });
    }

    private void sendPhone(String s, ProgressBar pb) {

      try {
        Thread.sleep(20000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    }
}