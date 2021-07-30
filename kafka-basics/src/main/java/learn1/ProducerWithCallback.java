package learn1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerWithCallback {
    private static Logger logger = LoggerFactory.getLogger(ProducerWithCallback.class);
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);


        //producer record

        //sending the data
        for (int i = 0; i < 10; i++) {
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>("first_topic",  "id_"+i,"Hello world, message: "+i);
            System.out.println("The message key id_"+i);
            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if(e == null){
                        logger.info("Received new Metadate "+"\n"+
                                "Partition "+ recordMetadata.partition()+ "\n"+
                                "Timestamp "+recordMetadata.timestamp()+"\n"+
                                "Offset "+recordMetadata.offset()+"\n");
                    }
                    else{
                        logger.error("Error producing "+ e);
                    }
                }
            }).get();
        }

        producer.flush();

    }
}
