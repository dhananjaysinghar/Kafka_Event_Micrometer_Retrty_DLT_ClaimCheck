package com.ex.kafka.producer;

import com.ex.kafka.common.ClaimCheck;
import com.ex.kafka.common.CommonUtils;
import com.ex.kafka.common.Data;
import com.ex.kafka.common.KafkaConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.RecordTooLargeException;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.NonNull;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserKafkaSender {

    private final KafkaTemplate<String, Data> userKafkaTemplate;
    private final KafkaTemplate<String, ClaimCheck> kafkaClaimCheckTemplate;
    private final MeterRegistry registry;
    private final AdminClient adminClient;
    private final KafkaConfiguration kafkaConfiguration;

    @SneakyThrows
    @Retryable(value = {TimeoutException.class, TransactionTimedOutException.class}, maxAttemptsExpression = "3", backoff = @Backoff(delayExpression = "500", multiplierExpression = "2", maxDelayExpression = "1000"))
    public void sendToTarget(Data data) {
        sendToKafka(data, kafkaConfiguration.getTargetTopic());
    }

    @Recover
    public void sendToDLT(Exception ex, Data data) {
        if ((ex instanceof RecordTooLargeException)) {
            log.warn("Sending kafka payload to Claim check topic due to {}", ex.getMessage());
            sendClaimCheckPayload(data);
        } else {
            log.warn("Sending kafka payload to DLT due to {}", ex.getMessage());
            sendToKafka(data, kafkaConfiguration.getDlTopicName());
        }
    }


    @SneakyThrows
    public void sendClaimCheckPayload(Data data) {
        String fileLocation = kafkaConfiguration.getRecordTooLargePath() + "/kafka_payload_" + System.currentTimeMillis();
        try {
            CommonUtils.storeInFile(fileLocation, new ObjectMapper().writeValueAsString(data));
            ClaimCheck claimCheck = ClaimCheck.builder().path(fileLocation).build();
            String topicName = kafkaConfiguration.getClaimCheckTopic();
            long startTime = System.currentTimeMillis();
            log.info("sending data='{}={}' to topic='{}'", claimCheck.hashCode(), claimCheck, topicName);
            try {
                ArrayList<String> topicNames = new ArrayList<>(adminClient.listTopics().names().get());
                if (!CollectionUtils.isEmpty(topicNames) && !topicNames.contains(topicName)) {
                    throw new TopicAuthorizationException("Topic " + topicName + " is not exist in metadata");
                }
                ProducerRecord<String, ClaimCheck> producerRecord = new ProducerRecord<>(topicName, UUID.randomUUID().toString(), claimCheck);
                producerRecord.headers().add("X-HEADER-VALUE", "TEST-DATA".getBytes(StandardCharsets.UTF_8));
                ListenableFuture<SendResult<String, ClaimCheck>> future = kafkaClaimCheckTemplate.send(producerRecord);
                logKafkaResponse(producerRecord, future);
            } catch (Exception ex) {
                log.error("Unable to push Payload with to kafka topic: {} : {}", topicName, ex.getMessage());
                throw ex;
            }
            registry.timer("events.timeTaken", "UserDataKafkaPayloadProducer", "UserDataKafkaPayloadProducer-total-processing-time")
                    .record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);

            log.info("Successfully published Payload to Kafka topic: {} in {} milliseconds", topicName, System.currentTimeMillis() - startTime);
        } catch (Exception ex) {
            log.error("Exception Occurred during file storage");
            throw ex;
        }

    }

    @SneakyThrows
    private void sendToKafka(Data data, String topicName) {
        long startTime = System.currentTimeMillis();
        log.info("sending data='{}={}' to topic='{}'", data.hashCode(), data, topicName);
        try {
            ArrayList<String> topicNames = new ArrayList<>(adminClient.listTopics().names().get());
            if (!CollectionUtils.isEmpty(topicNames) && !topicNames.contains(topicName)) {
                throw new TopicAuthorizationException("Topic " + topicName + " is not exist in metadata");
            }
            ProducerRecord<String, Data> producerRecord = new ProducerRecord<>(topicName, UUID.randomUUID().toString(), data);
            producerRecord.headers().add("X-HEADER-VALUE", "TEST-DATA".getBytes(StandardCharsets.UTF_8));
            ListenableFuture<SendResult<String, Data>> future = userKafkaTemplate.send(producerRecord);
            logKafkaResponse(producerRecord, future);
        } catch (Exception ex) {
            log.error("Unable to push Payload with to kafka topic: {} : {}", topicName, ex.getMessage());
            throw ex;
        }
        registry.timer("events.timeTaken", "UserDataKafkaPayloadProducer", "UserDataKafkaPayloadProducer-total-processing-time")
                .record(System.currentTimeMillis() - startTime, TimeUnit.MILLISECONDS);

        log.info("Successfully published Payload to Kafka topic: {} in {} milliseconds", topicName, System.currentTimeMillis() - startTime);
    }

    @SneakyThrows
    private <T> void logKafkaResponse(ProducerRecord<String, T> producerRecord, ListenableFuture<SendResult<String, T>> future) {
        future.addCallback(new ListenableFutureCallback<SendResult<String, T>>() {
            @Override
            public void onSuccess(SendResult<String, T> result) {
                log.info("Sent Payload to kafka topic:[{}] on partition:[{}] with offset=[{}]", producerRecord.topic(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                registry.counter("events.sent", "UserDataKafkaPayloadProducer", "UserDataKafkaPayloadProducer-total-processed")
                        .increment();
            }

            @Override
            public void onFailure(@NonNull Throwable ex) {
                log.error("Unable to send Payload to kafka topic:[{}] due to : {}", producerRecord.topic(), ex);
            }
        });
    }
}
