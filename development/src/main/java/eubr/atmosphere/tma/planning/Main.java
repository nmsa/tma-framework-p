package eubr.atmosphere.tma.planning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eubr.atmosphere.tma.planning.database.ConfigRulesManager;
import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.PrivacyScore;
import eubr.atmosphere.tma.utils.TrustworthinessScore;

public class Main 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static List<FactHandle> factHandleList = new ArrayList<>();
    private static ConfigRulesManager configRulesManager = new ConfigRulesManager();

    public static void main( String[] args ) {
        //final KieSession ksession = initSession();
        //runConsumer(ksession);
        
        //Checking privacy scores
        final KieSession privacyKSession = initPrivacySession();
        runPrivacyConsumer(privacyKSession);
    }

    private static void runConsumer(KieSession ksession) {
        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;
        int maxNoMessageFoundCount =
                Integer.parseInt(PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));

        try {
            while (true) {

              ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);

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

              // Manipulate the records
              consumerRecords.forEach(record -> {
                  validateValue(record, ksession);
               });

              ksession.fireAllRules();
              LOGGER.info("Rules were applied! ksession.getFactCount(): {}", ksession.getFactCount());
              removeFactHandles(ksession);

              // commits the offset of record to broker.
              consumer.commitAsync();
              sleep(5000);
            }
        } finally {
            consumer.close();
        }
    }

    private static void validateValue(ConsumerRecord<Long, String> record, KieSession ksession) {
        String stringJsonScore = record.value();
        TrustworthinessScore score = new Gson().fromJson(stringJsonScore, TrustworthinessScore.class);
        LOGGER.info(record.toString());
        LOGGER.info("Score: {} / Offset: {}", score.getScore(), record.offset());
        factHandleList.add(ksession.insert(score));
    }

    private static void removeFactHandles(KieSession ksession) {
        for (FactHandle handle : factHandleList) {
            ksession.delete(handle);
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private static KieSession initSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newClassPathResource( "Rules.drl",
                                                            Main.class ),
                                                            ResourceType.DRL );

        final InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages( kbuilder.getKnowledgePackages() );

        if ( kbuilder.hasErrors() ) {
            throw new RuntimeException( "Compilation error.\n" + kbuilder.getErrors().toString() );
        }

        return kbase.newKieSession();
    }
    
    /**
     * ################# Methods that check privacy rules #################
     */
    
    private static void runPrivacyConsumer(KieSession ksession) {
        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;
        int maxNoMessageFoundCount =
                Integer.parseInt(PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));

        try {
            while (true) {

              ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);

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

//              for (ConsumerRecord<Long, String> record : consumerRecords) {
//            	  System.out.println(record);
//              }
              
              //deleteMessages("topic-planning", 1, 498);
              
              // Manipulate the records
              consumerRecords.forEach(record -> {
				addFactList(record, ksession);
              });
              
              ksession.fireAllRules();
              LOGGER.info("Rules were applied! ksession.getFactCount(): {}", ksession.getFactCount());
              removeFactHandles(ksession);

              // commits the offset of record to broker.
              consumer.commitAsync();
              sleep(5000);
            }
        } finally {
            consumer.close();
        }
    }
    
    private static void addFactList(ConsumerRecord<Long, String> record, KieSession ksession) {
        String stringJsonScore = record.value();
        PrivacyScore score = new Gson().fromJson(stringJsonScore, PrivacyScore.class);
        LOGGER.info(record.toString());
        LOGGER.info("Score: {} / Offset: {}", score.getScore(), record.offset());
        
        Double threshold = null;
        if (score.getConfigurationProfileId() != null) {
			threshold = configRulesManager.searchThresholdByConfigProfileID(score.getConfigurationProfileId(),
					score.getAttributeId());
        }
        if (threshold != null) {
        	score.setThreshold(threshold);
        }
        LOGGER.info("Threshold: {} " + threshold);
        factHandleList.add(ksession.insert(score));
    }
    
    private static KieSession initPrivacySession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newClassPathResource( "privacy_rules.drl",
                                                            Main.class ),
                                                            ResourceType.DRL );

        final InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages( kbuilder.getKnowledgePackages() );

        if ( kbuilder.hasErrors() ) {
            throw new RuntimeException( "Compilation error.\n" + kbuilder.getErrors().toString() );
        }

        return kbase.newKieSession();
    }
    
    public static void deleteMessages(String topicName, int partitionIndex, int beforeIndex) {
        TopicPartition topicPartition = new TopicPartition(topicName, partitionIndex);
        Map<TopicPartition, RecordsToDelete> deleteMap = new HashMap<>();
        deleteMap.put(topicPartition, RecordsToDelete.beforeOffset(beforeIndex));
        
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
                PropertiesManager.getInstance().getProperty("bootstrapServers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, 
                PropertiesManager.getInstance().getProperty("groupIdConfig"));
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 
                Integer.parseInt(PropertiesManager.getInstance().getProperty("maxPollRecords")));
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 
                PropertiesManager.getInstance().getProperty("offsetResetEarlier"));

        
        AdminClient kafkaAdminClient = KafkaAdminClient.create(props);
        kafkaAdminClient.deleteRecords(deleteMap);
    }

}
