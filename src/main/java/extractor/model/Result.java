package extractor.model;

public class Result {

    private Traccia traccia;
    private PredictedValues predictedValues;
    private float distance;
    private String car;

    public Traccia getTraccia() {
        return traccia;
    }

    public void setTraccia(Traccia traccia) {
        this.traccia = traccia;
    }

    public PredictedValues getPredictedValues() {
        return predictedValues;
    }

    public void setPredictedValues(PredictedValues predictedValues) {
        this.predictedValues = predictedValues;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }
}
