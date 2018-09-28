package eubr.atmosphere.tma.planning;

import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class AdaptationManager {

    public static void performAdaptation() { 
        System.out.println("Adaptation will be performed!");

        Action action = new Action();
        action.setAction("Test Action");
        action.setResourceId(33);

        JsonElement jsonElement = new Gson().toJsonTree(action);

        KafkaManager kafkaManager = new KafkaManager();
        try {
            kafkaManager.addItemKafka(jsonElement.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
