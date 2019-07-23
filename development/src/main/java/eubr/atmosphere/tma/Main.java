package eubr.atmosphere.tma;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eubr.atmosphere.tma.planning.database.ConfigRulesManager;
import eubr.atmosphere.tma.planning.kafka.ConsumerCreator;
import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.PrivacyScore;
import eubr.atmosphere.tma.utils.TrustworthinessScore;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static List<FactHandle> factHandleList = new ArrayList<>();
    private static ConfigRulesManager configRulesManager = new ConfigRulesManager();

    public static void main( String[] args ) {
    	
    	//loading drools rule files
        //final KieSession performanceKSession = initSession("Rules");
        final KieSession privacyKSession = initSession("PrivacyRules");
        
        runConsumer(null, privacyKSession);
    }
    
    private static void runConsumer(KieSession performanceKSession, KieSession privacyKSession) {
        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;
        int maxNoMessageFoundCount =
                Integer.parseInt(PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));

        try {
            while (true) {

              ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));

              // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
              if (consumerRecords.count() == 0) {
                  noMessageFound++;

                  if (noMessageFound > maxNoMessageFoundCount) {
                    // If no message found count is reached to threshold exit loop.
                      sleep(2000);
                  } else {
                    continue;
                  }
              }

              LOGGER.info("ConsumerRecords: {}", consumerRecords.count());

              if (performanceKSession != null) {
            	  firePerformanceRules(consumerRecords, performanceKSession);
              }
              if (privacyKSession != null) {
            	  firePrivacyRules(consumerRecords, privacyKSession);
              }

              // commits the offset of record to broker.
              consumer.commitAsync();
              sleep(5000);
            }
        } finally {
            consumer.close();
        }
    }

    /**
     *######### MANIPULATE AND APPLY PERFORMANCE RULES ######### 
     */
    
	private static void firePerformanceRules(ConsumerRecords<Long, String> consumerRecords,
			KieSession performanceKSession) {
		// Manipulate the records
		consumerRecords.forEach(record -> {
			validatePerformanceValue(record, performanceKSession);
		});

		performanceKSession.fireAllRules();
		LOGGER.info("Rules were applied! ksession.getFactCount(): {}", performanceKSession.getFactCount());
		removeFactHandles(performanceKSession);
	}

    private static void validatePerformanceValue(ConsumerRecord<Long, String> record, KieSession ksession) {
        String stringJsonScore = record.value();
        TrustworthinessScore trustworthinessScore = new Gson().fromJson(stringJsonScore, TrustworthinessScore.class);
        LOGGER.info(record.toString());
        LOGGER.info("Score: {} / Offset: {}", trustworthinessScore.getScore(), record.offset());
        factHandleList.add(ksession.insert(trustworthinessScore));
    }
    
    /**
     *######################################################### 
     */
    
    /**
     *######### MANIPULATE AND APPLY PRIVACY RULES ######### 
     */
    
    private static void firePrivacyRules(ConsumerRecords<Long, String> consumerRecords,
			KieSession performanceKSession) {
		// Manipulate the records
		consumerRecords.forEach(record -> {
			validatePrivacyValue(record, performanceKSession);
		});

		performanceKSession.fireAllRules();
		LOGGER.info("Rules were applied! ksession.getFactCount(): {}", performanceKSession.getFactCount());
		removeFactHandles(performanceKSession);
	}
    
    private static void validatePrivacyValue(ConsumerRecord<Long, String> record, KieSession ksession) {
    	
    	LOGGER.info(record.toString());
        String stringJsonScore = record.value();
        TrustworthinessScore trustworthinessScore = new Gson().fromJson(stringJsonScore, TrustworthinessScore.class);
		
        if (trustworthinessScore != null && trustworthinessScore.getPrivacyScore() != null
				&& trustworthinessScore.getPrivacyScore().getScore() != null) {
			
        	PrivacyScore privacyScore = trustworthinessScore.getPrivacyScore();
			LOGGER.info("Score: {} / Offset: {}", privacyScore.getScore(), record.offset());
			
			Double threshold = null;
			if ( privacyScore.getConfigurationProfileId() != null ) {
				threshold = configRulesManager.searchThresholdByConfigProfileID(
						privacyScore.getConfigurationProfileId(), privacyScore.getAttributeId());
			}
			if ( threshold != null ) {
				privacyScore.setThreshold(threshold);
			}
			
			factHandleList.add(ksession.insert(privacyScore));
			
		} else {
			String msgWarn = "Privacy score not initialized."; 
			LOGGER.warn(msgWarn);
		}
    }
    
    /**
     *######################################################### 
     */
    
    private static void removeFactHandles(KieSession ksession) {
        for (FactHandle handle : factHandleList) {
            ksession.delete(handle);
        }
    }

    private static KieSession initSession(String rulesFileName) {
    	return KieServices.Factory.get().getKieClasspathContainer().newKieSession(rulesFileName);
    }
    
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

}
