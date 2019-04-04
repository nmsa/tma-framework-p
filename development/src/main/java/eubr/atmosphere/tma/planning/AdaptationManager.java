package eubr.atmosphere.tma.planning;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eubr.atmosphere.tma.data.Action;
import eubr.atmosphere.tma.data.Configuration;
import eubr.atmosphere.tma.data.ActionPlan;
import eubr.atmosphere.tma.data.ConfigurationData;
import eubr.atmosphere.tma.data.Plan;
import eubr.atmosphere.tma.planning.database.PlanManager;

public class AdaptationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptationManager.class);
    private static PlanManager planManager = new PlanManager();

    public static void performAdaptation(Action action) {
        LOGGER.info("Adaptation will be performed!");

        Plan plan = createPlan();
        addActionPlan(plan, action);
        planManager.saveActionPlan(plan);

        KafkaManager kafkaManager = new KafkaManager();
        try {
            // TODO: this will need to change, to add only the planId
            kafkaManager.addItemKafka(plan.getPlanId().toString());
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static void addActionPlan(Plan plan, Action action) {
        // TODO: when we change to more than one action, the execution order needs to be specified
        int executionOrder = 1;
        ActionPlan actionPlan = new ActionPlan(plan.getPlanId(), action.getActionId(), executionOrder);

        for (Configuration config: action.getConfigurationList()) {
            actionPlan.addConfiguration(new ConfigurationData(config.getConfigurationId(), config.getValue()));
        }

        plan.addAction(actionPlan);
    }

    private static Plan createPlan() {
        Plan plan = new Plan();
        plan.setValueTime(Instant.now().getEpochSecond());

        plan.setMetricId(1);
        plan.setQualityModelId(1);
        plan.setStatus(Plan.STATUS.TO_DO);

        int planId = planManager.saveNewPlan(plan);
        plan.setPlanId(planId);
        return plan;
    }

    public static void testPlanCreation() {
        Plan plan = createPlan();

        ActionPlan actionPlan = new ActionPlan(plan.getPlanId(), 1, 1); // actionId: 1 ("scale")
        actionPlan.addConfiguration(new ConfigurationData(1, "default")); // configurationId: 1 ("metadata.namespace")
        actionPlan.addConfiguration(new ConfigurationData(2, "wildfly")); // configurationId: 2 ("metadata.name")
        actionPlan.addConfiguration(new ConfigurationData(3, "3")); // configurationId: 3 ("spec.replicas")

        plan.addAction(actionPlan);
        planManager.saveActionPlan(plan);
    }
}