package extractor.control;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Extractor {

    /*private static ArrayList<MLObject> mlObjectArrayList = new ArrayList<>();

    private static String[] output = {"danceability.json", "energy.json", "loudness.json", "speechiness.json", "valence.json", "tempo.json"};
    private static String[] path = {"result_ml/single/", "result_ml/duplication/", "result_ml_cv/single/", "result_ml_cv/duplication/"};

    public Extractor(){
        readJSON();
    }

    private void readJSON() {
        Gson gson = new Gson();
        try {
            for (int i = 0; i < path.length; i++) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(path[i] + "distanceRecap.txt"));
                for (int j = 0; j < output.length; j++) {
                    mlObjectArrayList = gson.fromJson(new FileReader(path[i]+output[j]), new TypeToken<List<MLObject>>(){}.getType());
                    String car = mlObjectArrayList.get(0).getPredictedCar();
                    float distance = 0;
                    for (MLObject object : mlObjectArrayList){
                        distance += object.getDistancePredictedOriginal();
                    }
                    float distanceAVG = distance / mlObjectArrayList.size();
                    writer.write(car + "\t" + distanceAVG + "\n");
                }
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}
