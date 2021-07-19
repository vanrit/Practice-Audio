package natalia.doskach.audioorganizer;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Audio implements Serializable {
    public String name;
    public int ID;
    public String author;
    public int len;
    public boolean isDownloaded;
    public String path;
    public Audio(String name, String author, int length, String path,Boolean isDownloaded){
        this.name = name;
        this.author = author;
        this.len = length;
        this.path = path;
        this.isDownloaded = isDownloaded;
    }

}
