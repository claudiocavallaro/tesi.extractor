package extractor.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Preference {

    @Id
    private long timestamp;

    private int power;

    private String preference;

    private Traccia traccia;


    @JsonCreator
    public Preference(){}

    public String toString(){
        return traccia.toString() + "\npreference : " + preference + "\npower : " + power;
    }

}
