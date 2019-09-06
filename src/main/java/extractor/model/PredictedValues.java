package extractor.model;

public class PredictedValues {

    private int power;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    //-----------------------------------------
    private float loudness;
    private float speechiness;
    private float danceability;
    private float energy;

    private float valence;
    private float tempo;

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

    public float getValence() {
        return valence;
    }

    public void setValence(float valence) {
        this.valence = valence;
    }

    public float getTempo() {
        return tempo;
    }

    public void setTempo(float tempo) {
        this.tempo = tempo;
    }

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
        return "id " + id  + "\nPredicted on " + power + "W\ndanceability " + danceability + "\nspeechiness " + speechiness
                + "\nloudness " + loudness + "\nenergy " + energy + "\nvalence " + valence + "\ntempo " + tempo + "\n----------------";
    }
}
