package extractor;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jayway.jsonpath.JsonPath;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import extractor.model.MLObject;
import extractor.model.ArffObject;
import extractor.model.Preference;
import extractor.model.Traccia;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Add;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainExec {

    private static ArrayList<Preference> lista = new ArrayList<Preference>();


    public static final String clientId = "143e6b65125042178bcf14796211d5d2";
    public static final String clientSecret = "c39a44105867434396c5dbecbce77a8e";


    private static String[] properties = {"danceability", "speechiness", "energy", "loudness", "valence", "tempo"};
    private static String[] output = {"danceability.arff", "energy.arff", "loudness.arff", "speechiness.arff", "valence.arff", "tempo.arff"};
    private static String[] path = {"outMod/single/", "outMod/duplication/"};

    public static String token = "";

    public static void main(String[] args) {
        MainExec mainExec = new MainExec();

        String fileInput = "/phone.json";

        int contatore = 0;

        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < output.length; j++) {
                File file = new File(path[i] + output[j]);
                if (file.exists() == false) {
                    contatore++;
                }
            }
        }


        if (contatore != 0) {
            mainExec.leggiJSON(fileInput);
            mainExec.exportData("single");
            mainExec.exportData("duplication");
            mainExec.createJSONDB();
        } else {
            mainExec.readJSONDB();
        }

        for (int i = 0 ; i < properties.length; i++){
            String prop = properties[i];
            mainExec.ml(prop);
        }
    }

    //----------------------------------------------------------------
    private void readJSONDB() {
        Gson gson = new Gson();
        try {
            lista = gson.fromJson(new FileReader("db.json"), new TypeToken<List<Preference>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void createJSONDB() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(lista);
            BufferedWriter writer = new BufferedWriter(new FileWriter("db.json"));
            writer.write(json);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //----------------------------------------------------------------

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

            //System.out.println("MIN DISTANCE " + ml.getDistance());
            //System.out.println("PREFERENCE " + ml.getPreference().getPreference());
            //System.out.println(ml.getPreference().getTraccia().toString());

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

    private void ml(String car) {

        //Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //ArrayList<MLObject> listMLObj = new ArrayList<>();

        try {
            ConverterUtils.DataSource source = new ConverterUtils.DataSource("outMod/duplication/"+ car +".arff");

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

            System.out.println(eval.toSummaryString());

            System.out.print("the expression for the input data as per alogorithm is ");
            System.out.println(rf);

            //BufferedWriter bf = new BufferedWriter(new FileWriter("result_ml/duplication/" + car + ".json"));

            for (Prediction p : eval.predictions()) {
                System.out.println(p.actual() + " " + p.predicted());

                //MLObject ml = findNear(car, (float) p.predicted());
                //listMLObj.add(ml);
            }


            //String json = gson.toJson(listMLObj);
            //bf.write(json);
            //bf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //--------------- CREATE LIST AND EXPORT DATA -----------------------------

    private void leggiJSON(String s) {
        File file = new File(this.getClass().getResource(s).getFile());

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
                lista.add(p);

                spotifyExec(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //--------------- SPOTIFY -------------------------------------
    private void spotifyExec(Preference preference) {
        SpotifyApi api = new SpotifyApi.Builder().setClientId(clientId).setClientSecret(clientSecret).build();
        ClientCredentialsRequest clientCredentialsRequest = api.clientCredentials().build();
        try {
            token = clientCredentialsRequest.execute().getAccessToken();
            api.setAccessToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {

            GetTrackRequest getTrackRequest = api.getTrack(preference.getTraccia().getIdSong()).build();
            getTrack(getTrackRequest, preference);

            GetAudioFeaturesForTrackRequest request1 = api.getAudioFeaturesForTrack(preference.getTraccia().getIdSong()).build();
            getAudioFeaturesForTrack_Sync(request1, preference);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getTrack(GetTrackRequest getTrackRequest, Preference preference) {
        try {

            Track track = getTrackRequest.execute();

            String name = track.getName();

            int artistL = track.getArtists().length;
            String artists = "";
            for (int j = 0; j < artistL; j++) {
                artists += track.getArtists()[j].getName() + " ";
            }

            int duration = track.getDurationMs();

            preference.getTraccia().setName(name);
            preference.getTraccia().setArtist(artists);
            preference.getTraccia().setDuration(duration);


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public void getAudioFeaturesForTrack_Sync(GetAudioFeaturesForTrackRequest getAudioFeaturesForTrackRequest, Preference preference) {
        try {
            final AudioFeatures audioFeatures = getAudioFeaturesForTrackRequest.execute();

            float speechiness = audioFeatures.getSpeechiness();
            float daceability = audioFeatures.getDanceability();
            float loudness = audioFeatures.getLoudness();
            float energy = audioFeatures.getEnergy();
            float valence = audioFeatures.getValence();
            float tempo = audioFeatures.getTempo();

            preference.getTraccia().setSpeechiness(speechiness);
            preference.getTraccia().setDanceability(daceability);
            preference.getTraccia().setEnergy(energy);
            preference.getTraccia().setLoudness(loudness);
            preference.getTraccia().setValence(valence);
            preference.getTraccia().setTempo(tempo);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage() + " on " + preference.getTraccia().getIdSong());
        }
    }

    //------------------------------------------------------------------
    //------------- CREATE ARFF ----------------------------------------

    public void exportData(String quantity) {
        try {

            ArffObject obj = new ArffObject();

            BufferedWriter writerLoud = new BufferedWriter(new FileWriter("outMod/"+ quantity +"/loudness.arff"));
            BufferedWriter writerDanc = new BufferedWriter(new FileWriter("outMod/"+ quantity +"/danceability.arff"));
            BufferedWriter writerEne = new BufferedWriter(new FileWriter("outMod/"+ quantity +"/energy.arff"));
            BufferedWriter writerSpec = new BufferedWriter(new FileWriter("outMod/"+ quantity +"/speechiness.arff"));
            BufferedWriter writerVal = new BufferedWriter(new FileWriter("outMod/"+ quantity +"/valence.arff"));
            BufferedWriter writerTempo = new BufferedWriter(new FileWriter("outMod/"+ quantity +"/tempo.arff"));

            writerDanc.write("@relation danceability\n@attribute POWER numeric\n@attribute CAR numeric\n\n@data\n");
            writerEne.write("@relation energy\n@attribute POWER numeric\n@attribute CAR numeric\n\n@data\n");
            writerLoud.write("@relation loudness\n@attribute POWER numeric\n@attribute CAR numeric\n\n@data\n");
            writerSpec.write("@relation speechiness\n@attribute POWER numeric\n@attribute CAR numeric\n\n@data\n");
            writerVal.write("@relation valence\n@attribute POWER numeric\n@attribute CAR numeric\n\n@data\n");
            writerTempo.write("@relation tempo\n@attribute POWER numeric\n@attribute CAR numeric\n\n@data\n");

            ArrayList<Preference> listaP = new ArrayList<Preference>();
            listaP.addAll(lista);

            if (quantity.equals("duplication")){
                listaP.addAll(lista);

                listaP.addAll(lista);

                listaP.addAll(lista);
            }

            Collections.shuffle(listaP);

            for (Preference p : listaP) {
                float pref = Float.valueOf(p.getPreference());

                if (pref >= 3) {

                    obj.setId(p.getTraccia().getIdSong());
                    obj.setPower(p.getPower());
                    obj.setPref(p.getPreference());

                    obj.setCar(p.getTraccia().getLoudness());
                    writerLoud.write(obj.getPower() + "," + obj.getCar() + "\n");

                    obj.setCar(p.getTraccia().getDanceability());
                    writerDanc.write(obj.getPower() + "," + obj.getCar() + "\n");

                    obj.setCar(p.getTraccia().getEnergy());
                    writerEne.write(obj.getPower() + "," + obj.getCar() + "\n");

                    obj.setCar(p.getTraccia().getSpeechiness());
                    writerSpec.write(obj.getPower() + "," + obj.getCar() + "\n");

                    obj.setCar(p.getTraccia().getValence());
                    writerVal.write(obj.getPower() + "," + obj.getCar() + "\n");

                    obj.setCar(p.getTraccia().getTempo());
                    writerTempo.write(obj.getPower() + "," + obj.getCar() + "\n");
                }

            }

            writerLoud.close();
            writerDanc.close();
            writerEne.close();
            writerSpec.close();
            writerVal.close();
            writerTempo.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
