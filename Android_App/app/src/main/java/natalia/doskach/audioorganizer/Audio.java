package natalia.doskach.audioorganizer;

public class Audio {
    public String name;
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
