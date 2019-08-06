package extractor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

@Data
public class ArffObject {

    private String id;
    private float car;

    private int power;
    private String pref;

    @JsonCreator
    public ArffObject(){}


    public String toString(){
        return id + "," + car + "," + power + "," + pref + "\n";
    }
}
