package extractor.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import extractor.MainExec;
import extractor.model.MLObject;
import extractor.model.Preference;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class MachineLearning {


    private ArrayList<Preference> lista = MainExec.getLista();

    private File file ;
    private FileWriter fr = null;


    private static String[] properties = {"danceability", "speechiness", "energy", "loudness", "valence", "tempo"};


    public MachineLearning(){
        String mode = "single";
        file = new File("result_ml/single/summary.txt");

        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            ml(prop, mode, file);
        }

        mode = "duplication";
        file = new File("result_ml/duplication/summary.txt");
        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            ml(prop, mode, file);
        }
    }

    //-------------------- ML ----------------------------------------

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

    public Instances prepareSet(Instances testSet) {
        Instances neww = null;
        try {
            int[] att = {0};
            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray(att);
            removeFilter.setInvertSelection(true);
            removeFilter.setInputFormat(testSet);
            Instances newData = Filter.useFilter(testSet, removeFilter);

            Add add = new Add();
            add.setAttributeName("CAR");
            add.setInputFormat(newData);

            neww = Filter.useFilter(newData, add);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return neww;
    }

    private void ml(String car, String mode, File file) {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        ArrayList<MLObject> listMLObj = new ArrayList<>();

        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource("outMod/"+ mode +"/"+ car +".arff");

            Instances dataset = source.getDataSet();

            int trainSize = (int) Math.round(dataset.numInstances() * 80 / 100);
            int testSize = dataset.numInstances() - trainSize;

            Instances trainSet = new Instances(dataset, 0, trainSize);
            Instances set = new Instances(dataset, trainSize, testSize);

            trainSet.setClassIndex(trainSet.numAttributes() - 1);
            set.setClassIndex(set.numAttributes() - 1);

            //System.out.println(set.numInstances() + " " + trainSet.numInstances() + " " + dataset.numInstances());

            Instances testSet = prepareSet(set);
            testSet.setClassIndex(testSet.numAttributes() - 1);

            RandomForest rf = new RandomForest();
            rf.setNumIterations(100);
            rf.buildClassifier(trainSet);

            Evaluation eval = new Evaluation(trainSet);
            eval.evaluateModel(rf, set);

            fr = new FileWriter(file, true);

            fr.write("------ SUMMARY " + car + mode + " ----- \n");
            fr.write(eval.toSummaryString() + "\n");
            fr.close();

            System.out.println(eval.toSummaryString());

            System.out.print("the expression for the input data as per alogorithm is ");
            System.out.println(rf);

            BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml/" + mode + "/" + car + ".json"));

            for (Prediction p : eval.predictions()) {
                System.out.println("REAL " + p.actual() + " PREDICTED " + p.predicted());

                MLObject ml = findNear(car, (float) p.predicted());
                listMLObj.add(ml);
            }


            String json = gson.toJson(listMLObj);
            bf.write(json);
            bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
