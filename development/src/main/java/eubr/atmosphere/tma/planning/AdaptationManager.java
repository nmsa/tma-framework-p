package eubr.atmosphere.tma.planning;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.data.Action;
import eubr.atmosphere.tma.data.ActionPlan;
import eubr.atmosphere.tma.data.Configuration;
import eubr.atmosphere.tma.data.ConfigurationData;
import eubr.atmosphere.tma.data.MetricData;
import eubr.atmosphere.tma.data.Plan;
import eubr.atmosphere.tma.planning.database.PlanManager;
import eubr.atmosphere.tma.utils.Score;

public class AdaptationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptationManager.class);
    
    private static PlanManager planManager = new PlanManager();

    public static void performAdaptation(Action action, MetricData metricData) {
        LOGGER.info("Adaptation will be performed!");

        Plan plan = createPlan(metricData);
        addActionPlan(plan, action);
        planManager.saveActionPlan(plan);

        KafkaManager kafkaManager = new KafkaManager();
        try {
            kafkaManager.addItemKafka(plan.getPlanId().toString());
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
    
    public static MetricData obtainMetricData(Score score) {
    	MetricData metricData = new MetricData();
    	metricData.setMetricId(score.getMetricId());
    	metricData.setValueTime(score.getValueTime());
    	return metricData;
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

    private static Plan createPlan(MetricData metricData) {
        Plan plan = new Plan();
        plan.setValueTime(Instant.now().getEpochSecond());

        plan.setMetricId(metricData.getMetricId());
        plan.setValueTime(metricData.getValueTime());
        plan.setStatus(Plan.STATUS.TO_DO);

        int planId = planManager.saveNewPlan(plan);
        plan.setPlanId(planId);
        return plan;
    }
}