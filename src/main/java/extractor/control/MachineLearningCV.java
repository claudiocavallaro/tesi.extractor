package extractor.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import extractor.MainExec;
import extractor.model.MLObject;
import extractor.model.Preference;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AddClassification;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

public class MachineLearningCV {

    private ArrayList<Preference> lista = MainExec.getLista();

    private File file ;
    private FileWriter fr = null;

    private static String[] properties = {"danceability", "speechiness", "energy", "loudness", "valence", "tempo"};

    public MachineLearningCV(){
        /*String mode = "single";
        file = new File("result_ml_cv/single/summary.txt");

        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            mlCV(prop, mode, file);
        }*/

        String mode = "";
        mode = "duplication";
        file = new File("result_ml_cv/duplication/summary.txt");
        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            mlCV(prop, mode, file);
        }
    }

    //-------------------- ML WITH CROSS VALIDATION ------------------

    public void mlCV(String car, String mode, File file){
        ArrayList<MLObject> listMLObj = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource("outMod/" + mode + "/"+ car +".arff");
            Instances dataset = source.getDataSet();

            dataset.setClassIndex(dataset.numAttributes() - 1);

            int seed = 1;
            int fold = 0;
            if (mode.equals("single")){
                //istance number 96
                fold = 96;
            } else if (mode.equals("duplication")){
                //istance number 384
                fold = 384;
            }

            Random random = new Random(seed);
            Instances randData = new Instances(dataset);
            randData.randomize(random);

            Instances predictedData = null;
            Evaluation eval = new Evaluation(randData);

            for (int i = 0 ; i < fold ; i++){
                Instances train = randData.trainCV(fold, i, random);
                Instances test = randData.testCV(fold, i);

                // build and evaluate classifier
                RandomTree rf = new RandomTree();
                rf.buildClassifier(train);
                eval.evaluateModel(rf, test);

                // add predictions
                AddClassification filter = new AddClassification();
                filter.setClassifier(rf);
                filter.setOutputClassification(true);
                filter.setOutputDistribution(true);
                filter.setOutputErrorFlag(true);
                filter.setInputFormat(train);
                Filter.useFilter(train, filter);  // trains the classifier
                Instances pred = Filter.useFilter(test, filter);  // perform predictions on test set
                if (predictedData == null)
                    predictedData = new Instances(pred, 0);
                for (int j = 0; j < pred.numInstances(); j++)
                    predictedData.add(pred.instance(j));
            }

            // output evaluation
            System.out.println();
            System.out.println("=== Setup ===");
            System.out.println("Dataset: " + dataset.relationName());
            System.out.println("Folds: " + fold);
            System.out.println("Seed: " + seed);
            System.out.println();
            String summary = eval.toSummaryString("=== " + fold + "-fold Cross-validation ===", false);
            System.out.println(summary);

            fr = new FileWriter(file, true);

            fr.write("------ SUMMARY " + car + mode + " ----- \n");
            fr.write(summary + "\n");
            fr.close();

            BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml_cv/" + mode + "/" + car + ".json"));

            for (Prediction p : eval.predictions()){
                System.out.println("REAL " + p.actual() + " PREDICTED " + p.predicted());

                MLObject ml = findNear(car, (float) p.predicted());
                listMLObj.add(ml);
            }

            String json = gson.toJson(listMLObj);
            bf.write(json);
            bf.close();

        } catch (Exception e){
            e.printStackTrace();
        }
    }



    public MLObject findNear(String car, float predicted){
        MLObject ml = new MLObject();
        ml.setPredictedCar(car);
        try{
            int zero = 0;
            float predictedABS = Math.abs(predicted);
            float maxDistance = predictedABS - zero;
            for (Preference pref : lista){
                float myNumber = 0;
                float prefNum = new Float(pref.getPreference());
                switch (car){
                    case "danceability":
                        myNumber = pref.getTraccia().getDanceability();
                        break;
                    case "energy":
                        myNumber = pref.getTraccia().getEnergy();
                        break;
                    case "loudness":
                        myNumber = Math.abs(pref.getTraccia().getLoudness());
                        break;
                    case "speechiness":
                        myNumber = pref.getTraccia().getSpeechiness();
                        break;
                    case "valence":
                        myNumber = pref.getTraccia().getValence();
                        break;
                    case "tempo":
                        myNumber = pref.getTraccia().getTempo();
                        break;
                }

                float distance = Math.abs(myNumber - predictedABS);
                if ((distance < maxDistance) && (prefNum > 3)){
                    maxDistance = distance;
                    ml.setPreference(pref);
                    ml.getPreference().setTraccia(pref.getTraccia());
                    ml.setDistancePredictedOriginal(maxDistance);
                }
            }

            System.out.println("MIN " + car + " FROM PREDICTED " + ml.getPreference().getTraccia().getNumber(car));
            System.out.println("MIN DISTANCE " + ml.getDistancePredictedOriginal());
            System.out.println("PREFERENCE \n" + ml.getPreference().getTraccia() + "VOTE " + ml.getPreference().getPreference() + "\n");

        }catch (Exception e){
            e.printStackTrace();
        }

        return ml;
    }
}
