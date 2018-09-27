package eubr.atmosphere.tma.planning;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
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
        LOGGER.info("Hello World!");
        runConsumer();
    }

    private static void runConsumer() {

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
                  validateValue(record);
               });

              // commits the offset of record to broker.
              consumer.commitAsync();
            }
        } finally {
            consumer.close();
        }
    }

    private static void validateValue(ConsumerRecord<Long, String> record) {
        System.out.println("Record Key " + record.key());
        System.out.println("Record value " + record.value());
        System.out.println("Record partition " + record.partition());
        System.out.println("Record offset " + record.offset());

        if (Double.parseDouble(record.value()) > threshold) {
            System.out.println("Add item to the new topic");
        }
    }

    private static void sleep(int millis) {
        try {
            System.out.println("=== PAUSE (" + millis + ")===");
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
