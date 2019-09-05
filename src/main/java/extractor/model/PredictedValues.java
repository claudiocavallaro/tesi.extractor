package extractor.model;

public class PredictedValues {

    private int objectID;

    public int getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
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

    public String toString(){
        return "Predicted " + objectID + "\ndanceability " + danceability + "\nspeechiness " + speechiness
                + "\nloudness " + loudness + "\nenergy " + energy + "\nvalence " + valence + "\ntempo " + tempo + "\n----------------";
    }
}
