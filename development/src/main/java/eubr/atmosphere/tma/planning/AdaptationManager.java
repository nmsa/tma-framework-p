package eubr.atmosphere.tma.planning;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eubr.atmosphere.tma.data.Action;
import eubr.atmosphere.tma.planning.database.Plan;
import eubr.atmosphere.tma.planning.database.PlanManager;

public class AdaptationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptationManager.class);

    public static void performAdaptation(Action action) {
        LOGGER.info("Adaptation will be performed!");

        createPlan();

        JsonElement jsonElement = new Gson().toJsonTree(action);
        KafkaManager kafkaManager = new KafkaManager();
        try {
            // TODO: this will need to change, to add only the planId
            kafkaManager.addItemKafka(jsonElement.toString());
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static void createPlan() {
        Plan plan = new Plan();
        plan.setValueTime(Instant.now().getEpochSecond());

        plan.setMetricId(1);
        plan.setQualityModelId(1);
        plan.setStatus(Plan.STATUS.TO_DO);

        PlanManager planManager = new PlanManager();
        planManager.saveNewPlan(plan);
    }

    public static void testPlanCreation() {
        createPlan();
    }
}
