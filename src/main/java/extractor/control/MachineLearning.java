package extractor.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import extractor.MainExec;

import extractor.model.PredictedValues;
import extractor.model.Preference;
import javafx.animation.ParallelTransition;
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

    private ArrayList<PredictedValues> values;

    private static String[] properties = {"danceability", "speechiness", "energy", "loudness", "valence", "tempo"};


    public MachineLearning(){
        performSingle();
        performWithDuplication();
    }

    private void performSingle(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        file = new File("result_ml/single/summary.txt");
        values = new ArrayList<>();
        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            ml(prop, "single");
        }
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml/single/prediction.json"));
            bf.write(gson.toJson(values));
            bf.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void performWithDuplication(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        values = new ArrayList<>();
        file = new File("result_ml/duplication/summary.txt");
        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            ml(prop, "duplication");
        }

        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml/duplication/prediction.json"));
            bf.write(gson.toJson(values));
            bf.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //-------------------- ML ----------------------------------------


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

    private void ml(String car, String mode) {


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
            rf.setNumIterations(10);
            rf.buildClassifier(trainSet);

            Evaluation eval = new Evaluation(trainSet);
            eval.evaluateModel(rf, set);

            fr = new FileWriter(file, true);

            fr.write("------ SUMMARY " + car + " " +  mode + " ----- \n");
            fr.write(eval.toSummaryString() + "\n");
            fr.close();

            System.out.println(eval.toSummaryString());

            System.out.print("the expression for the input data as per alogorithm is ");
            System.out.println(rf);

            for (int i = 0 ; i < eval.predictions().size() ; i++) {
                Prediction p = eval.predictions().get(i);
                //System.out.println("REAL " + p.actual() + " PREDICTED " + p.predicted());

                int power = (int) set.get(i).value(0);

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

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
