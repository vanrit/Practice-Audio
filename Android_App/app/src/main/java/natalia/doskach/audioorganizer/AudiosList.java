package natalia.doskach.audioorganizer;

import java.util.ArrayList;

public class AudiosList {
    static ArrayList<Audio> audios;

    public AudiosList(){
        audios = new ArrayList<>();
    }
    public void changeData(ArrayList<Audio> audios){
        this.audios = new ArrayList<>();
        this.audios = audios;
    }
    public ArrayList<Audio> getAudios(){
        return audios;
    }
    public void add(Audio a){
        audios.add(a);
    }
}
