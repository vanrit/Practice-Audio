package natalia.doskach.audioorganizer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    EditText login;
    EditText password;
    ActivityResultLauncher<Intent> registrationLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = new Intent();
                    setResult(Activity.RESULT_FIRST_USER);
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        login = findViewById(R.id.editLogin);
        password = findViewById(R.id.editPassword);
    }

    @Override
    public void onBackPressed() {
    }

    public void register(View view) {
        registrationLauncher.launch(new Intent(this, RegisterActivity.class));

    }

    public void signin(View view) {
        String loginT = login.getText().toString();
        String passwordT = password.getText().toString();
        if(loginT.isEmpty()){
            Toast.makeText(this, "Введите логин", Toast.LENGTH_SHORT).show();
        }
        if(passwordT.isEmpty()){
            Toast.makeText(this, "Введите пароль", Toast.LENGTH_SHORT).show();
        }
        if(!loginT.isEmpty() & !passwordT.isEmpty()){
            RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
                    getRequestQueue();
            String url ="http://84.201.143.25:8081/login";

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(), "Вход выполнен", Toast.LENGTH_SHORT).show();
                            Intent data = new Intent();
                            setResult(Activity.RESULT_FIRST_USER);
                            finish();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error instanceof ServerError)
                        Log.e("server-error", String.valueOf(error.networkResponse.statusCode));
                    if(error instanceof AuthFailureError)
                    {Log.e("auth-error", String.valueOf(error.networkResponse.statusCode));
                        Toast.makeText(getApplicationContext(), "Пароль или логин неверный", Toast.LENGTH_SHORT).show();
                    }
                    if(error instanceof NetworkError)
                        Log.e("net-error", String.valueOf(error));
                }
            }){
                @Override
                protected Map<String,String> getParams(){
                    Map<String,String> params = new HashMap<String, String>();
                    params.put(KEY_USERNAME,loginT);
                    params.put(KEY_PASSWORD,passwordT);
                    return params;
                }


            };
            queue.add(stringRequest);
        }
    }
}