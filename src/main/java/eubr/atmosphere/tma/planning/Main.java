package eubr.atmosphere.tma.planning;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eubr.atmosphere.tma.planning.utils.PropertiesManager;

/**
 * Hello world!
 *
 */
public class Main 
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final double threshold = 0.4;

    public static void main( String[] args )
    {
        KieSession ksession = initSession();
        runConsumer(ksession);
    }

    private static void runConsumer(KieSession ksession) {

        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;

        try {
            while (true) {

              ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);

              // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
              if (consumerRecords.count() == 0) {
                  noMessageFound++;

                  int maxNoMessageFoundCount =
                          Integer.parseInt(PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));
                  if (noMessageFound > maxNoMessageFoundCount) {
                    // If no message found count is reached to threshold exit loop.
                      sleep(2000);
                  } else {
                    continue;
                  }
              }

              // Manipulate the records
              consumerRecords.forEach(record -> {
                  validateValue(record, ksession);
               });

              ksession.fireAllRules();

              // commits the offset of record to broker.
              consumer.commitAsync();
            }
        } finally {
            consumer.close();
        }
    }

    private static void validateValue(ConsumerRecord<Long, String> record, KieSession ksession) {
        System.out.println("Record Key " + record.key());
        System.out.println("Record value " + record.value());
        System.out.println("Record partition " + record.partition());
        System.out.println("Record offset " + record.offset());

        if (Double.parseDouble(record.value()) > threshold) {
            System.out.println("Add item to the new topic");
        }
        ksession.insert(record.value());
    }

    private static void sleep(int millis) {
        try {
            System.out.println("=== PAUSE (" + millis + ") ===");
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
