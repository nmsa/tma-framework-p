package eubr.atmosphere.tma.planning.kafka;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import eubr.atmosphere.tma.planning.utils.PropertiesManager;
import eubr.atmosphere.tma.utils.MessageExecute;

public class KafkaManager {

    private final static String TOPIC = "topic-execute";
    private final static String BOOTSTRAP_SERVERS =
            PropertiesManager.getInstance().getProperty("bootstrapServers");

    static Producer<Long, String> producer = createProducer();

    public void addItemKafka(String jsonAction) throws InterruptedException, ExecutionException {
        long time = System.currentTimeMillis();
        final ProducerRecord<Long, String> record =
                new ProducerRecord<>(TOPIC, time, jsonAction);
        RecordMetadata metadata = producer.send(record).get();
        long elapsedTime = System.currentTimeMillis() - time;
        System.out.printf("sent record(key=%s value=%s) " +
                        "meta(partition=%d, offset=%d) time=%d %n",
                record.key(), record.value(), metadata.partition(),
                metadata.offset(), elapsedTime);
        producer.flush();
    }
    
    public void addItemKafka(MessageExecute messageExecute) throws InterruptedException, ExecutionException {
        long time = System.currentTimeMillis();
        JsonElement jsonElement = new Gson().toJsonTree(messageExecute);
        final ProducerRecord<Long, String> record =
                new ProducerRecord<>(TOPIC, time, jsonElement.toString());
        RecordMetadata metadata = producer.send(record).get();
        long elapsedTime = System.currentTimeMillis() - time;
        System.out.printf("sent record(key=%s value=%s) " +
                        "meta(partition=%d, offset=%d) time=%d\n",
                record.key(), record.value(), metadata.partition(),
                metadata.offset(), elapsedTime);
        producer.flush();
    }

    private static Producer<Long, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                                            BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                                        LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                                    StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
