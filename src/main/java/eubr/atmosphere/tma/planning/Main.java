package eubr.atmosphere.tma.planning;

import java.util.Iterator;

import org.apache.kafka.clients.consumer.Consumer;
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

    public static void main( String[] args )
    {
        LOGGER.info("Hello World!");
    }

    private static void runConsumer() {

        Consumer<Long, String> consumer = ConsumerCreator.createConsumer();
        int noMessageFound = 0;

        while (true) {

          ConsumerRecords<Long, String> consumerRecords = consumer.poll(1000);

          // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
          if (consumerRecords.count() == 0) {
              noMessageFound++;

              int maxNoMessageFoundCount =
                      Integer.parseInt(PropertiesManager.getInstance().getProperty("maxNoMessageFoundCount"));
              if (noMessageFound > maxNoMessageFoundCount)
                // If no message found count is reached to threshold exit loop.
                break;
              else
                  continue;
          }

          //print each record.
          consumerRecords.forEach(record -> {
              System.out.println("Record Key " + record.key());
              System.out.println("Record value " + record.value());
              System.out.println("Record partition " + record.partition());
              System.out.println("Record offset " + record.offset());
           });

          // commits the offset of record to broker.
          consumer.commitAsync();
        }
        consumer.close();
    }
}
