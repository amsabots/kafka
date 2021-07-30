package learn1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThread {

    private void runThreads() {
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThread.class.getName());
        CountDownLatch latch = new CountDownLatch(1);
        Runnable myRunnable = new ConsumerThread("localhost:9092", "first_topic", "another-application", latch);
        Thread consumeQ = new Thread(myRunnable);

        //close down the consumer

        consumeQ.start();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            logger.info("Caught shutdown instruction.");
            ((ConsumerThread)myRunnable).shutDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application shutdown. Consumer not running");
        }));


        try {
            latch.await();
        } catch (InterruptedException e) {
          logger.info("Application  has been interrupted, Shutting down gracefully");
        }finally {
            logger.info("Application closed");
        }



    }

    public static void main(String[] args) {

        new ConsumerDemoWithThread().runThreads();


    }

    public class ConsumerThread implements Runnable {
        private Logger logger = LoggerFactory.getLogger(ConsumerThread.class.getName());
        private CountDownLatch latch;

        KafkaConsumer<String, String> consumer;

        public ConsumerThread(String bootstrap_servers, String topic, String groupId, CountDownLatch latch) {
            this.latch = latch;
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            consumer = new KafkaConsumer<String, String>(properties);
            consumer.subscribe(Arrays.asList(topic));
        }


        @Override
        public void run() {



                try {
                    while (true) {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                        for (ConsumerRecord<String, String> record : records) {
                            logger.info("Key " + record.key() + " Value: " + record.value());
                            logger.info("Partition: " + record.partition() + " Offset: " + record.offset());
                        }
                    }
                } catch (WakeupException e) {
                    logger.info("Wake up interrupt received");
                } finally {
                    consumer.close();
                    latch.countDown();
                }

        }

        public void shutDown() {
            consumer.wakeup();
        }
    }
}
