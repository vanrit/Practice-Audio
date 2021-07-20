package natalia.doskach.audioorganizer;

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
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    EditText login;
    EditText password;
    EditText passwordRep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        login = findViewById(R.id.editLogin);
        password = findViewById(R.id.password);
        passwordRep = findViewById(R.id.repeatPassword);
    }

    public void register(View view) throws JSONException {
        String loginT = login.getText().toString();
        String passwordT = password.getText().toString();
        String passwordRepT = passwordRep.getText().toString();
        if(loginT.isEmpty() || passwordRepT.isEmpty() || passwordT.isEmpty()){
            Toast.makeText(this, "Заполните все формы", Toast.LENGTH_SHORT).show();
        }
        else if(!passwordT.equals(passwordRepT)){
            Toast.makeText(this, "Пароли не совпадают!", Toast.LENGTH_SHORT).show();
        }
        else{
// Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="http://84.201.143.25:8080/signup";
            JSONObject j = new JSONObject();
            j.put("firstName","firstName");
            j.put("lastName","lastName");
            j.put("username",loginT);
            j.put("password",passwordT);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,url,j, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if (response != null) {
                        try {
                            int id = response.getInt("id");
                            if(id==0)
                                Toast.makeText(getApplicationContext(), response.getString("status"), Toast.LENGTH_SHORT).show();
                            else{
                                Log.i("info","registered");
                                Intent data = new Intent();
                                setResult(Activity.RESULT_OK);
                                finish();
                            }
                        } catch (JSONException e) {
                            Log.i("ERROR","");
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(error instanceof ServerError)
                        Log.e("server-error", String.valueOf(error.networkResponse.statusCode));
                    if(error instanceof AuthFailureError)
                    {Log.e("auth-error", String.valueOf(error.networkResponse.statusCode));
                    }
                    if(error instanceof NetworkError)
                        Log.e("net-error", String.valueOf(error));
                }
            });

// Add the request to the RequestQueue.
            queue.add(jsonObjectRequest);
        }

}}