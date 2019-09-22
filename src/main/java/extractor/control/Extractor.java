package extractor.control;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import extractor.MainExec;
import extractor.model.PredictedValues;
import extractor.model.Preference;
import extractor.model.Result;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Extractor {

    private ArrayList<PredictedValues> lista ;

    private static ArrayList<Preference> music = MainExec.getLista();

    private ArrayList<Result> resultList;

    private String path = "result_ml_cv/duplication/";
    //private String path = "result_ml/duplication/";
    //private String path = "result_ml_cv/single/";
    //private String path = "result_ml/single/";

    private String[] car = {"danceability", "energy", "loudness", "speechiness", "tempo", "valence"};

    public Extractor(){

        readPrediction();

        for (String s : car){
            minDistance(s);
        }

    }


    private void readPrediction(){
        Gson gson = new Gson();
        try {
            lista = new ArrayList<>();
            lista = gson.fromJson(new FileReader(path + "prediction.json"), new TypeToken<List<PredictedValues>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void minDistance(String car){
        resultList = new ArrayList<>();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(path + car + "_music_search.txt"));
            writer.write("Distance based on " + car + "\n");
            for (PredictedValues pv : lista){

                float value = pv.getNumber(car);
                float min = value - 0;

                Result result = new Result();
                result.setCar(car);
                result.setPredictedValues(pv);

                for (Preference p : music){
                    float musicValue = p.getTraccia().getNumber(car);
                    float distance = Math.abs(musicValue - value);
                    if (distance < Math.abs(min)){
                        min = distance;
                        result.setDistance(min);
                        result.setTraccia(p.getTraccia());
                    }
                }
                resultList.add(result);
                writer.write("id " + pv.getId() + "\nPower " + pv.getPower() + "\nPredicted " + pv.getNumber(car) + "\nDistance " + min + "\nTRACK " + result.getTraccia() + "\n");
            }

            float sum = 0 ;
            for (Result result : resultList){
                sum += result.getDistance();
            }
            float avg = sum / (resultList.size());
            writer.write("\nAVG DISTANCE " + avg);
            writer.close();
        }catch (Exception e){
            e.printStackTrace();
        }


    }
}
