package eubr.atmosphere.tma.planning;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
import eubr.atmosphere.tma.planning.api.RulesManagerRest;
import eubr.atmosphere.tma.planning.database.DatabaseManager;

import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import eubr.atmosphere.tma.planning.utils.ScoreKafka;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main{
    private final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private final List<FactHandle> factHandleList = new ArrayList<>();
    
    private final AtomicBoolean updateRules = new AtomicBoolean(false);
    
    private final RulesManagerRest rulesManagerRest = new RulesManagerRest();

    public static void main(String[] args){
        Main planning = new Main();
        planning.begin();
    }
    
    
    private void begin() {
        KieSession ksession = initSession();
        rulesManagerRest.start(updateRules);
        runConsumer(ksession);
    }
    
    private void runConsumer(KieSession ksession) {
            Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
            int noMessageFound = 0;
            int maxNoMessageFoundCount = Integer
                            .parseInt(PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));

            try {
                while (true) {
                    synchronized(updateRules){
                        if(updateRules.get()){
                            ksession = initSession();
                            LOGGER.warn("Rules got updated!");
                            updateRules.set(false);
                        }
                    }

                    ConsumerRecords<Long, String> consumerRecords = consumer.poll(Duration.ofMillis(1000));

                    // 1000 is the time in milliseconds consumer will wait if no record is found at
                    // broker.
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
                    for(ConsumerRecord<Long, String> record : consumerRecords){
                        validateValue(record, ksession);
                    }

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

    private void validateValue(ConsumerRecord<Long, String> record, KieSession ksession) {
        String stringJsonScore = record.value();
        ScoreKafka score = new Gson().fromJson(stringJsonScore, ScoreKafka.class);
        LOGGER.info(record.toString());
        LOGGER.info("Score: {} / Offset: {}", 
                score.getScore().get(score.getScore().keySet().toArray()[0]), record.offset());
        factHandleList.add(ksession.insert(score));
    }

    private void removeFactHandles(KieSession ksession) {
        for (FactHandle handle : factHandleList) {
            ksession.delete(handle);
        }
        factHandleList.clear();
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private KieSession initSession() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        
        DatabaseManager db = new DatabaseManager();
        /*String text = new String(Files.readAllBytes(Paths.get("C:\\Users\\Jodao\\Documents\\GitHub\\tma-framework-p\\development\\src\\main\\resources\\eubr\\atmosphere\\tma\\planning\\AllRules.drl")), StandardCharsets.UTF_8);
        db.saveRules(text);*/
        kbuilder.add(ResourceFactory.newByteArrayResource(db.readRules()),ResourceType.DRL);
        
        final InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages( kbuilder.getKnowledgePackages() );

        if ( kbuilder.hasErrors() ) {
            throw new RuntimeException( "Compilation error.\n" + kbuilder.getErrors().toString() );
        }
        
        return kbase.newKieSession();
    }
}
