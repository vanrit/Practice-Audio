package natalia.doskach.audioorganizer;

import android.content.Context;
import android.media.AudioFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.*;


import java.io.Serializable;

public class Audio implements Serializable {
    public String name;
    public int ID = -1;
    public String author;
    public int len;
    public boolean isDownloaded;
    public String path;

    public Audio(String name, String author, int length, String path, Boolean isDownloaded) {
        this.name = name;
        this.author = author;
        this.len = length;
        this.path = path;
        this.isDownloaded = isDownloaded;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getDuration(Context c) {
        if (this.path == null || !(new File(path).exists()))
            return 99;
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(c, Uri.parse(this.path));
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            return Integer.parseInt(duration) / 1000;
        } catch (Exception ex) {
            return 0;
        }

    }

}
