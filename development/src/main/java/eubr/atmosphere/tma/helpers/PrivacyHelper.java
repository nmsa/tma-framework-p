package eubr.atmosphere.tma.helpers;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.data.Action;
import eubr.atmosphere.tma.data.Configuration;
import eubr.atmosphere.tma.data.Plan;
import eubr.atmosphere.tma.planning.AdaptationManager;
import eubr.atmosphere.tma.planning.database.ConfigRulesManager;
import eubr.atmosphere.tma.planning.database.PlanManager;
import eubr.atmosphere.tma.planning.kafka.KafkaManager;
import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.MessageExecute;
import eubr.atmosphere.tma.utils.PrivacyScore;

public class PrivacyHelper {

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

					Plan plan = AdaptationManager.createPlan();

					Action action = new Action(1, "UPDATE", resourceId, 1);
					action.addConfiguration(new Configuration(1, "k", privacyScore.getK().toString()));
					action.addConfiguration(new Configuration(2, "instanceId", privacyScore.getInstanceId().toString()));

					AdaptationManager.addActionPlan(plan, action);
					planManager.saveActionPlan(plan);

					MessageExecute messageExecute = new MessageExecute(action.getAction(), resourceId, 1,
							plan.getValueTime(), action.getConfigurationList());

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
			Double kValue = configRulesManager.searchKAnonimityByID(privacyScore.getTimestamp());
			privacyScore.setK(kValue);

			Integer resourceId = Integer.parseInt(PropertiesManager.getInstance().getProperty("resource.id"));

			Plan plan = AdaptationManager.createPlan();

			Action action = new Action(1, "NONE", resourceId, 1);
			action.addConfiguration(new Configuration(1, "k", privacyScore.getK().toString()));
			action.addConfiguration(new Configuration(2, "instanceId", privacyScore.getInstanceId().toString()));

			AdaptationManager.addActionPlan(plan, action);
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
	
}
