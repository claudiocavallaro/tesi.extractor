package extractor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Traccia {

    private String idSong;
    private String name;
    private String artist;

    private String url;

    private int duration;

    private float loudness;
    private float speechiness;
    private float danceability;
    private float energy;

    private float valence;
    private float tempo;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Id
    public String getIdSong() {
        return idSong;
    }

    public void setIdSong(String idSong) {
        this.idSong = idSong;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public float getLoudness() {
        return loudness;
    }

    public void setLoudness(float loudness) {
        this.loudness = loudness;
    }

    public float getSpeechiness() {
        return speechiness;
    }

    public void setSpeechiness(float speechiness) {
        this.speechiness = speechiness;
    }

    public float getDanceability() {
        return danceability;
    }

    public void setDanceability(float danceability) {
        this.danceability = danceability;
    }

    public float getEnergy() {
        return energy;
    }

    public void setEnergy(float energy) {
        this.energy = energy;
    }

    public Traccia(String idSong, String name, String artist, float loudness, float speechiness, float danceability, float energy) {
        this.idSong = idSong;
        this.name = name;
        this.artist = artist;
        this.loudness = loudness;
        this.speechiness = speechiness;
        this.danceability = danceability;
        this.energy = energy;
    }

    @JsonCreator
    public Traccia(){}

    public float getNumber(String car){
        float myNumber = 0;
        switch (car){
            case "danceability":
                myNumber = danceability;
                break;
            case "energy":
                myNumber = energy;
                break;
            case "loudness":
                myNumber = loudness;
                break;
            case "speechiness":
                myNumber =speechiness;
                break;
            case "valence":
                myNumber = valence;
                break;
            case "tempo":
                myNumber = tempo;
                break;
        }
        return myNumber;
    }


    public String toString(){
        return "NAME : " + name + "\nARTIST : " + artist + "\n";
    }
}
