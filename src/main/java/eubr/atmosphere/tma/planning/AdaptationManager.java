package eubr.atmosphere.tma.planning;

import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;

public class AdaptationManager {

    public static void performAdaptation() { 
        System.out.println("Adaptation will be performed!");

        Action action = new Action();
        action.setAction("Test Action");
        action.setResourceId(33);

        /*JsonObject jsObj =  (JsonObject) new Gson().toJsonTree(action);
        System.out.println(jsObj);*/

        JsonElement jsonElement = new Gson().toJsonTree(action);

        KafkaManager kafkaManager = new KafkaManager();
        try {
            kafkaManager.addItemKafka(jsonElement.toString());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
