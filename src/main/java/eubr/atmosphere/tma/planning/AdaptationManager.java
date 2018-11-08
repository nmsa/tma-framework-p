package eubr.atmosphere.tma.planning;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eubr.atmosphere.tma.data.Action;

public class AdaptationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptationManager.class);

    public static void performAdaptation(Action action) {
        LOGGER.info("Adaptation will be performed!");
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
