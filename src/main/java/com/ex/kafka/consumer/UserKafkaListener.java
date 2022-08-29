package com.ex.kafka.consumer;

import com.ex.kafka.common.ClaimCheck;
import com.ex.kafka.common.Data;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserKafkaListener {

    private final TestService testService;
    private final MeterRegistry registry;


    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${app.kafka.consumer-group-id}", containerFactory = "concurrentKafkaListenerUserConsumerFactory")
    public void receive(@Payload Data data, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, Acknowledgment acknowledgment) {
        long timeMillis = System.currentTimeMillis();
        String eventName = "event_name";
        registry.counter("events.received", "event_name", "UserDataKafkaPayload").increment();
        testService.receive(data);
        acknowledgment.acknowledge();
        registry.counter("events.acknowledged", eventName, "UserDataKafkaPayload_acknowledged").increment();
        registry.timer("events.timeTaken", eventName, "UserDataKafkaPayload-total-processing-time")
                .record(System.currentTimeMillis() - timeMillis, TimeUnit.MILLISECONDS);
        log.info("acknowledged the kafka message in kafka listener : {} from partition {}", data, partition);
    }


    @KafkaListener(topics = {"${app.kafka.dead-letter-topic}"}, groupId = "${app.kafka.consumer-group-id}", containerFactory = "concurrentKafkaListenerUserConsumerFactory")
    public void receiveDeadLetterTopicData(@Payload Data data, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, Acknowledgment acknowledgment) {
        long timeMillis = System.currentTimeMillis();
        String eventName = "event_name";
        registry.counter("events.received", "event_name", "UserDataKafkaPayload").increment();
        testService.receive(data);
        acknowledgment.acknowledge();
        registry.counter("events.acknowledged", eventName, "UserDataKafkaPayload_acknowledged").increment();
        registry.timer("events.timeTaken", eventName, "UserDataKafkaPayload-total-processing-time")
                .record(System.currentTimeMillis() - timeMillis, TimeUnit.MILLISECONDS);
        log.info("Received data in kafka listener : {} from partition {}", data, partition);
    }

    @KafkaListener(topics = "${app.kafka.claim_check_topic}", groupId = "${app.kafka.consumer-group-id}", containerFactory = "concurrentKafkaListenerClaimCheckConsumerFactory")
    public void receiveClaimCheck(@Payload ClaimCheck data, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition, Acknowledgment acknowledgment) {
        long timeMillis = System.currentTimeMillis();
        String eventName = "event_name";
        registry.counter("events.received", "event_name", "UserDataKafkaPayload").increment();
        testService.receiveClaimCheck(data);
        acknowledgment.acknowledge();
        registry.counter("events.acknowledged", eventName, "UserDataKafkaPayload_acknowledged").increment();
        registry.timer("events.timeTaken", eventName, "UserDataKafkaPayload-total-processing-time")
                .record(System.currentTimeMillis() - timeMillis, TimeUnit.MILLISECONDS);
        log.info("Received data in kafka listener : {} from partition {}", data, partition);
    }
}
