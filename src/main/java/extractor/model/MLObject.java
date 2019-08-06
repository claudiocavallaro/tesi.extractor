package extractor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class MLObject {

    private Preference preference;

    private String predictedCar;
    private float distancePredictedOriginal;

    @JsonCreator
    public MLObject(){}

    public MLObject(Preference preference, String predictedCar, float distancePredictedOriginal) {
        this.preference = preference;
        this.predictedCar = predictedCar;
        this.distancePredictedOriginal = distancePredictedOriginal;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public String getPredictedCar() {
        return predictedCar;
    }

    public void setPredictedCar(String predictedCar) {
        this.predictedCar = predictedCar;
    }

    public float getDistancePredictedOriginal() {
        return distancePredictedOriginal;
    }

    public void setDistancePredictedOriginal(float distancePredictedOriginal) {
        this.distancePredictedOriginal = distancePredictedOriginal;
    }
}
