package natalia.doskach.audioorganizer;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.FileUtils;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.loopj.android.http.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import cz.msebera.android.httpclient.Header;
import natalia.doskach.audioorganizer.telegram.Example;
import natalia.doskach.audioorganizer.telegram.TelegramActivity;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.ViewHolder> implements Filterable {

    private static final String KEY_ID = "id";
    private ArrayList<Audio> localDataSet;
    private ArrayList<Audio> filteredDataSet;
    private int playingTune = -1;
    AlertDialog dialog;
    private ItemFilter mFilter = new ItemFilter();
    Context cont;


    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public Object getAudios() {
        return filteredDataSet;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            if (filterString.isEmpty()) {
                results.values = localDataSet;
                return results;
            }
            final List<Audio> list = localDataSet;
            int count = list.size();
            final ArrayList<Audio> nlist = new ArrayList<Audio>(count);
            String filterableString;
            for (int i = 0; i < count; i++) {
                filterableString = list.get(i).name;
                if (filterableString.toLowerCase().contains(filterString)) {
                    nlist.add(list.get(i));
                }
            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredDataSet = (ArrayList<Audio>) results.values;
            notifyDataSetChanged();
            Log.i("info", "data changed");
        }

    }

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView twAuthor;
        private final TextView twTitle;
        private final TextView twLength;
        private final ProgressBar progressBar;
        private final ImageButton menuBtn;
        private final ImageButton playBtn;
        private final ConstraintLayout card;
        private final MenuItem downloadRemoveBtn;
        MediaPlayer mediaPlayer;
        Context context;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            twAuthor = (TextView) view.findViewById(R.id.authorTW);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            twTitle = (TextView) view.findViewById(R.id.nameTW);
            twLength = (TextView) view.findViewById(R.id.lengthTW);
            menuBtn = (ImageButton) view.findViewById(R.id.listMenuBtn);
            playBtn = (ImageButton) view.findViewById(R.id.playBtn);
            card = (ConstraintLayout) view.findViewById(R.id.itemCard);
            downloadRemoveBtn = (MenuItem) view.findViewById(R.id.downloadRemoveBtn);


        }

        public TextView getAuthorTW() {
            return twAuthor;
        }

        public TextView getTitleTW() {
            return twTitle;
        }

        public TextView getLengthTW() {
            return twLength;
        }

        public ImageButton getPlayBtn() {
            return playBtn;
        }

        public ImageButton getMenuBtn() {
            return menuBtn;
        }

        public MediaPlayer getMediaPlayer() {
            return mediaPlayer;
        }

        public MenuItem getDownloadRemoveBtn() {
            return downloadRemoveBtn;
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public AudioListAdapter(ArrayList<Audio> dataSet,Context con) {
        cont = con;
        localDataSet = dataSet;
        filteredDataSet = dataSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.audio_list_item, viewGroup, false);


        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getAuthorTW().setText(filteredDataSet.get(position).author);
        viewHolder.getLengthTW().setText(filteredDataSet.get(position).len / 60 + ":" + filteredDataSet.get(position).len % 60);
        viewHolder.getTitleTW().setText(filteredDataSet.get(position).name);
        viewHolder.getMenuBtn().setOnClickListener(new ImageButton.OnClickListener() {
            public void onClick(View v) {
                Log.i("info", "openMenu");
                showPopupMenu(viewHolder, v, position);
            }
        });
        if (filteredDataSet.get(position).isDownloaded)
            viewHolder.getPlayBtn().setImageResource(R.drawable.ic_play_circle_48);
        else
            viewHolder.getPlayBtn().setImageResource(R.drawable.ic_arrow_circle_down_48);

        viewHolder.getPlayBtn().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (filteredDataSet.get(position).isDownloaded) {
                    try {
                        playSong(viewHolder, v, position);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else
                    downloadSong(viewHolder, v, position);

            }
        });
    }

    private void showPopupMenu(ViewHolder viewHolder, View v, int position) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.inflate(R.menu.audio_list_menu);
        if (filteredDataSet.get(position).isDownloaded) {
            if (filteredDataSet.get(position).ID > 0) //файл есть на сервере
                popupMenu.getMenu().findItem(R.id.downloadRemoveBtn).setTitle("удалить файл");
            else //файла нет на сервеое
                popupMenu.getMenu().findItem(R.id.downloadRemoveBtn).setTitle("загрузить на сервер");
        } else
            popupMenu.getMenu().findItem(R.id.downloadRemoveBtn).setTitle("скачать файл");
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                Log.i("info", "close Menu");
            }
        });

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.renameBtn:
                                Log.i("info", "rename");
                                renameItem(position, v);
                                return true;
                            case R.id.downloadRemoveBtn:
                                Log.i("info", "download/remove");
                                if (!filteredDataSet.get(position).isDownloaded)
                                    downloadSong(viewHolder, v, position);
                                else {

                                    if (filteredDataSet.get(position).ID > 0) //файл есть на сервере
                                        removeFile(position, viewHolder);
                                    else //файла нет на сервеое
                                    {
                                        try {
                                            uploadFile(position, viewHolder);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                return true;
                            case R.id.deleteBtn:
                                Log.i("info", "delete");
                                deleteItem(position, viewHolder);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void uploadFile(int position, ViewHolder viewHolder) throws IOException {
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(cont, "Загрузка: Конвертация", Toast.LENGTH_SHORT).show();
        File file = new File(filteredDataSet.get(position).path);
        int index = file.getName().lastIndexOf(".");
        String ext = file.getName().substring(index+1);
        if(ext.equals("m4a"))
            finishUpload(position,viewHolder,file);
        else
            Example.convert(file,this,position, viewHolder);


    }

    public void finishUpload(int position, ViewHolder viewHolder, File file) {
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(cont, "Загрузка: Отправка", Toast.LENGTH_SHORT).show();
        RequestQueue queue = MySingleton.getInstance(viewHolder.context).
                getRequestQueue();
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Example.url + "/audios/upload",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        viewHolder.progressBar.setVisibility(View.INVISIBLE);
                        Log.i("response","from upload:");
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Log.i("message",obj.toString());
                            if(obj.getInt("status")==1)
                            {
                                Toast.makeText(cont, "Загрузка успешна", Toast.LENGTH_SHORT).show();
                                filteredDataSet.get(position).ID = obj.getInt("songId");
                                file.exists();
                            }
                            else if(obj.getInt("status")==0) {
                                Toast.makeText(cont, "Невозможно загрузить аудио на сервер, ошибка при сохранении", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(cont, "Невозможно загрузить аудио на сервер, неподдерживаемый формат или такой файл уже существует", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                               Toast.makeText(cont, "Невозможно загрузить аудио на сервер", Toast.LENGTH_LONG).show();
                        if (error instanceof ServerError)
                            Log.e("server-error", String.valueOf(error.networkResponse.statusCode));
                        if (error instanceof AuthFailureError) {
                            Log.e("auth-error", String.valueOf(error.networkResponse.statusCode));
                        }
                        if (error instanceof NetworkError)
                            Log.e("net-error", String.valueOf(error));
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("source", "local");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() throws IOException {
                Map<String, DataPart> params = new HashMap<>();
                params.put("file", new DataPart(file.getName(), getFileDataFromFile(file)));
                return params;
            }
        };
        Log.i("file", file.getName());
        //adding the request to volley
        queue.add(volleyMultipartRequest);
    }

//        Retrofit retrofit = new retrofit2.Retrofit.Builder()
//                .baseUrl(Example.url)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//        ApiInterface service = retrofit.create(ApiInterface.class);
//        Call<ResponseBody> responseBodyCall = service.login("TomB","password");
//        responseBodyCall.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//
//                Log.i("success", response.toString());
//                Retrofit retrofit = new retrofit2.Retrofit.Builder()
//                        .baseUrl(Example.url)
//                        .addConverterFactory(GsonConverterFactory.create())
//                        .build();
//                ApiInterface service = retrofit.create(ApiInterface.class);
//                File file = new File(filteredDataSet.get(position).path);
//                String source = "Local";
//                String scope = "private";
//                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), filteredDataSet.get(position).path);
//
//                MultipartBody.Part multipartBody =MultipartBody.Part.createFormData("file",file.getName(),requestFile);
//
//                Call<ResponseBody> responseBodyCall = service.addRecord(source,scope,  multipartBody);
//                responseBodyCall.enqueue(new Callback<ResponseBody>() {
//                    @Override
//                    public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
//
//                        Log.i("success", response.toString());
//                    }
//
//                    @Override
//                    public void onFailure(Call<ResponseBody> call, Throwable t) {
//                        Log.d("failure", "message = " + t.getMessage());
//                        Log.d("failure", "cause = " + t.getCause());
//                    }
//                });
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.d("failure", "message = " + t.getMessage());
//                Log.d("failure", "cause = " + t.getCause());
//            }
//        });

    @RequiresApi(api = Build.VERSION_CODES.O)
    public byte[] getFileDataFromFile(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return fileContent;
    }

    private void removeFile(int position, ViewHolder viewHolder) {
        Log.i("info", "remove song");
        Toast.makeText(cont, "Файл удалён", Toast.LENGTH_LONG).show();
        filteredDataSet.get(position).isDownloaded = false;
        notifyItemChanged(position);
        viewHolder.getPlayBtn().setImageResource(R.drawable.ic_arrow_circle_down_48);
    }

    private void deleteItem(int position, ViewHolder viewHolder) { //delete from the list, delete file, delete file from server
//        if(filteredDataSet.get(position).ID > 0){ //file is on server
//            RequestQueue queue = Volley.newRequestQueue(viewHolder.context);
//            String url =Example.url+"/audios/delete";
//            StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
//                    new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                            deleteFromFiles(position);
//                        }
//                    }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    if(error instanceof ServerError)
//                        Log.e("server-error", String.valueOf(error.networkResponse.statusCode));
//                    if(error instanceof AuthFailureError)
//                    {Log.e("auth-error", String.valueOf(error.networkResponse.statusCode));
//                    }
//                    if(error instanceof NetworkError)
//                        Toast.makeText(dialog.getContext(), "Невозможно удалить без интернета", Toast.LENGTH_SHORT).show();
//                }
//            }){
//                @Override
//                protected Map<String,String> getParams(){
//                    Map<String,String> params = new HashMap<String, String>();
//                    params.put(KEY_ID, String.valueOf(filteredDataSet.get(position).ID));
//                    return params;
//                }
//            };
//            queue.add(stringRequest);
//        }
        deleteFromFiles(position);
    }

    private void deleteFromFiles(int position) {
        File f = new File(filteredDataSet.get(position).path);
        if (f.exists())
            f.delete();
        filteredDataSet.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void addItem(Audio a) {
        Log.i("audio", "added");
        filteredDataSet.add(a);
        notifyItemInserted(getItemCount());

    }

    private void renameItem(int position, View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(((Activity) v.getContext()));
        builder.setTitle(R.string.dialog_title);
        LayoutInflater inflater = ((Activity) v.getContext()).getLayoutInflater();
        View moduleWindow = inflater.inflate(R.layout.dialog_rename, null);
        builder.setView(moduleWindow);
        EditText form = (EditText) moduleWindow.findViewById(R.id.renameET);
        form.setText(filteredDataSet.get(position).name);
        Button yesButton = (Button) moduleWindow.findViewById(R.id.YESbutton);
        Button noButton = (Button) moduleWindow.findViewById(R.id.NObutton);
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User clicked OK button
                String text = form.getText().toString();
                filteredDataSet.get(position).name = text;
                notifyItemChanged(position);
                dialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog = builder.show();

    }
    boolean  isDownloading = false;

    private void downloadSong(ViewHolder viewHolder, View v, int position) {

        Log.i("info", "download song");
        int dur = filteredDataSet.get(position).len / 10;
        if(isDownloading)
            return;
        isDownloading = true;
        Toast.makeText(cont, "Начало загрузки", Toast.LENGTH_SHORT).show();
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                isDownloading = false;
                viewHolder.progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(cont, "Загрузка успешна", Toast.LENGTH_SHORT).show();
                filteredDataSet.get(position).isDownloaded = true;
                notifyItemChanged(position);
                viewHolder.getPlayBtn().setImageResource(R.drawable.ic_play_circle_48);
            }
        }, 1000 + dur*1000);
    }

    private void playSong(ViewHolder viewHolder, View v, final int position) throws IOException {
        if (playingTune == -1) { //start playing music
            Log.i("info", "start playing music pos:" + position + " pt:" + playingTune);
            playingTune = position;
            playTune(viewHolder, v, position);
        } else if (playingTune == position) { //pause music
            Log.i("info", "pause music pos:" + position + " pt:" + playingTune);
            playingTune = -1;
            pauseTune(viewHolder, v, position);
        } else { //change music
            Log.i("info", "change music pos:" + position + " pt:" + playingTune);
            pauseTune(viewHolder, v, playingTune);
            playingTune = position;
            playTune(viewHolder, v, position);
        }
    }


    private void pauseTune(ViewHolder viewHolder, View v, int position) {
        ((MainActivity) v.getContext()).pauseTune(position);
    }

    private void playTune(ViewHolder viewHolder, View v, int position) throws IOException {
        viewHolder.getPlayBtn().setImageResource(R.drawable.ic_pause_circle_48);
        ((MainActivity) v.getContext()).playTune(filteredDataSet.get(position).path);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return filteredDataSet.size();
    }
}

