package eubr.atmosphere.tma.planning;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.TrustworthinessScore;

public class Main 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static List<FactHandle> factHandleList = new ArrayList<>();

    public static void main( String[] args ) {
        final KieSession ksession = initSession();
        runConsumer(ksession);
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
}
