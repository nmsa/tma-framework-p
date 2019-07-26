package eubr.atmosphere.tma.planning;

import java.time.Instant;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.data.Action;
import eubr.atmosphere.tma.data.ActionPlan;
import eubr.atmosphere.tma.data.Configuration;
import eubr.atmosphere.tma.data.ConfigurationData;
import eubr.atmosphere.tma.data.Plan;
import eubr.atmosphere.tma.data.PlanStatus;
import eubr.atmosphere.tma.planning.database.ConfigRulesManager;
import eubr.atmosphere.tma.planning.database.PlanManager;
import eubr.atmosphere.tma.planning.kafka.KafkaManager;
import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.MessageExecute;
import eubr.atmosphere.tma.utils.PrivacyScore;

public class AdaptationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdaptationManager.class);
    
    private static PlanManager planManager = new PlanManager();
    private static ConfigRulesManager configRulesManager = new ConfigRulesManager();

    public static void increasePRIVAASAnonymization(PrivacyScore privacyScore) {
    	if (privacyScore != null) {
    		KafkaManager kafkaManager = new KafkaManager();
			try {
				Double kValue = configRulesManager.searchKAnonimityByID(privacyScore.getTimestamp());
				if (kValue != null) {
					kValue = kValue + 1;
					privacyScore.setK(kValue);
					
					Integer resourceId = Integer.parseInt(PropertiesManager.getInstance().getProperty("resource.id"));
					
					Plan plan = getPlanById(privacyScore.getPlanId());
					plan.setMetricId(privacyScore.getAttributeId());
					plan.setQualityModelId(privacyScore.getConfigurationProfileId());
					plan.setStatus(PlanStatus.IN_PROGRESS);
					
					Action action = new Action(1, "Increase anonymization", resourceId, 1);
					action.addConfiguration(new Configuration(1, "k", privacyScore.getK().toString()));
					
					addActionPlan(plan, action);
					planManager.saveActionPlan(plan);

					MessageExecute messageExecute = new MessageExecute(action.getAction(), resourceId, 1, plan.getValueTime(),
							action.getConfigurationList());
					
			        kafkaManager.addItemKafka(messageExecute);
				}
			} catch (InterruptedException e) {
				LOGGER.warn(e.getMessage(), e);
			} catch (ExecutionException e) {
				LOGGER.warn(e.getMessage(), e);
			}
    	}
    }
    
    public static void noIncreasePRIVAASAnonymization(PrivacyScore privacyScore) {
    	KafkaManager kafkaManager = new KafkaManager();
        try {
			Double kValue = configRulesManager
					.searchKAnonimityByID(privacyScore.getTimestamp());
        	privacyScore.setK(kValue);
        	
			Integer resourceId = Integer.parseInt(PropertiesManager.getInstance().getProperty("resource.id"));

			Plan plan = getPlanById(privacyScore.getPlanId());
			plan.setMetricId(privacyScore.getAttributeId());
			plan.setQualityModelId(privacyScore.getConfigurationProfileId());
			plan.setStatus(PlanStatus.COMPLETED);
			
			Action action = new Action(1, "No increase anonymization", resourceId, 1);
			action.addConfiguration(new Configuration(1, "k", privacyScore.getK().toString()));
			
			addActionPlan(plan, action);
			planManager.saveActionPlan(plan);

			MessageExecute messageExecute = new MessageExecute(action.getAction(), resourceId, 1, plan.getValueTime(),
					action.getConfigurationList());
			
	        kafkaManager.addItemKafka(messageExecute);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
   
    private static Plan getPlanById(Integer planId) {
    	Plan plan = null;
		if ( planId == null ) {
			plan = buildInitialPlan();
		} else {
			plan = planManager.searchPlan(planId);
		}
		return plan;
    }
    
    private static Plan buildInitialPlan() {
        Plan plan = new Plan();
        //plan.setValueTime(Instant.now().getEpochSecond());
        plan.setStatus(PlanStatus.TO_DO);

        int planId = planManager.saveNewPlan(plan);
        plan.setPlanId(planId);
        
        return plan;
    }
    
    
    
    
    
    public static void performAdaptation(Action action) {
        LOGGER.info("Adaptation will be performed!");

        Plan plan = createPlan();
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
        plan.setStatus(PlanStatus.TO_DO);

        int planId = planManager.saveNewPlan(plan);
        plan.setPlanId(planId);
        return plan;
    }
}