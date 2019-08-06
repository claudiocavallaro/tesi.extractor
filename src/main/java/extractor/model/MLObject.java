package extractor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class MLObject {

    private Preference preference;

    private String car;
    private float distance;

    @JsonCreator
    public MLObject(){}

    public MLObject(Preference preference, String car, float distance) {
        this.preference = preference;
        this.car = car;
        this.distance = distance;
    }

    public Preference getPreference() {
        return preference;
    }

    public void setPreference(Preference preference) {
        this.preference = preference;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
