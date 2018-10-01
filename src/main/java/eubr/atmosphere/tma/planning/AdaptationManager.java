package eubr.atmosphere.tma.planning;

import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eubr.atmosphere.tma.data.Action;

public class AdaptationManager {

    public static void performAdaptation(Action action) {
        System.out.println("Adaptation will be performed!");
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
