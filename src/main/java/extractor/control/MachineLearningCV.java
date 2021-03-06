package extractor.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import extractor.MainExec;
import extractor.model.PredictedValues;
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

    private File file ;
    private FileWriter fr = null;

    private ArrayList<PredictedValues> values;

    private static String[] properties = {"danceability", "speechiness", "energy", "loudness", "valence", "tempo"};

    public MachineLearningCV(){
        performSingle();
        performWithDuplication();
    }

    private void performSingle(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        file = new File("result_ml_cv/single/summary.txt");
        values = new ArrayList<>();
        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            mlCV(prop, "single");
        }
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml_cv/single/prediction.json"));
            bf.write(gson.toJson(values));
            bf.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void performWithDuplication(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        values = new ArrayList<>();
        file = new File("result_ml_cv/duplication/summary.txt");
        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            mlCV(prop, "duplication");
        }

        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml_cv/duplication/prediction.json"));
            bf.write(gson.toJson(values));
            bf.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //-------------------- ML WITH CROSS VALIDATION ------------------

    public void mlCV(String car, String mode){


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

            System.out.println(eval.predictions().size());

            for (int i = 0 ; i < eval.predictions().size(); i++){
                Prediction p = eval.predictions().get(i);

                int power = (int) predictedData.get(i).value(0);
                //System.out.println("REAL " + p.actual() + " PREDICTED " + p.predicted());

                PredictedValues pv = null;
                if (values.size() < eval.predictions().size()){
                    pv = new PredictedValues();
                } else{
                    pv = values.get(i);
                }

                pv.setId(i);
                pv.setPower(power);

                switch (car){
                    case "danceability":
                        pv.setDanceability((float)p.predicted());
                        break;
                    case "energy":
                        pv.setEnergy((float) p.predicted());
                        break;
                    case "loudness":
                        pv.setLoudness((float) p.predicted());
                        break;
                    case "speechiness":
                        pv.setSpeechiness((float)p.predicted());
                        break;
                    case "valence":
                        pv.setValence((float) p.predicted());
                        break;
                    case "tempo":
                        pv.setTempo((float) p.predicted());
                        break;
                }

                if (values.size() < eval.predictions().size()){
                    values.add(pv);
                }

            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
