package extractor.control;

import com.jayway.jsonpath.JsonPath;
import extractor.model.Preference;
import extractor.model.Traccia;
import weka.classifiers.evaluation.Prediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class CarEx {

    ArrayList<Preference> list = new ArrayList<>();

    public CarEx(){
        File file = new File("energy.json");
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String pathPower = "$.power.power";
            String pathSong = "$.idSong";
            String pathPreference = "$.preference";
            while ((line = br.readLine()) != null) {

                int power = JsonPath.read(line, pathPower);

                String idSong = JsonPath.read(line, pathSong);

                String preference = JsonPath.read(line, pathPreference);

                Preference p = new Preference();

                Traccia t = new Traccia();
                t.setIdSong(idSong);
                p.setTraccia(t);
                p.setPower(power);
                p.setPreference(preference);
                list.add(p);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int contatore = 0;
        for (Preference p : list){
            float pref = Float.valueOf(p.getPreference());
            if (pref >= 3){
                contatore++;
            }
        }

        System.out.println(contatore);
    }
}
