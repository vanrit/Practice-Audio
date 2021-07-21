package natalia.doskach.audioorganizer;

import android.location.Location;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {
    @Multipart
    @POST("audios/upload/")
    Call<ResponseBody> addRecord(@Query("source") String source, @Query("scope") String scope,
                                  @Part MultipartBody.Part file);
    @POST("login/")
    Call<ResponseBody> login(@Query("username") String username, @Query("password") String password);
}