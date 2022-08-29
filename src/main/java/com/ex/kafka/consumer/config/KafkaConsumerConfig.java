package com.ex.kafka.consumer.config;

import com.ex.kafka.common.ClaimCheck;
import com.ex.kafka.common.Data;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${app.kafka.host}")
    private String kafkaHost;

    @Value("${app.kafka.max-poll-record:15000000}")
    private int maxPollRecord;

    @Value("${app.kafka.consumer-group-id}")
    private String kafkaConsumerGroupId;

    private static final Map<String, Object> DEFAULT_CONSUMER_CONFIG_MAP = new HashMap<>();

    @PostConstruct
    public void configureKafkaProperties() {
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaHost);
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecord);

        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 20000);
    }

    @Bean
    public ConsumerFactory<String, Data> userConsumerFactory() {
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);

        Map<String, Object> map = new HashMap<>(DEFAULT_CONSUMER_CONFIG_MAP);
        map.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);
        JsonDeserializer<Data> userJsonDeserializer = new JsonDeserializer<>(Data.class);
        userJsonDeserializer.ignoreTypeHeaders();
        DefaultKafkaConsumerFactory<String, Data> consumerFactory = new DefaultKafkaConsumerFactory<>(map, new StringDeserializer(), userJsonDeserializer);
        consumerFactory.addListener(new MicrometerConsumerListener<>(Metrics.globalRegistry,
                Collections.singletonList(new ImmutableTag("consumerCustomMetrics", "kafka-consumer-metrics"))));
        return consumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Data> concurrentKafkaListenerUserConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Data> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userConsumerFactory());
        factory.setConcurrency(3);
        // factory.setMessageConverter(new StringJsonMessageConverter(new ObjectMapper()));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setAutoStartup(Boolean.TRUE);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ClaimCheck> claimCheckConsumerFactory() {
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        DEFAULT_CONSUMER_CONFIG_MAP.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);

        Map<String, Object> map = new HashMap<>(DEFAULT_CONSUMER_CONFIG_MAP);
        map.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroupId);
        JsonDeserializer<ClaimCheck> userJsonDeserializer = new JsonDeserializer<>(ClaimCheck.class);
        userJsonDeserializer.ignoreTypeHeaders();
        DefaultKafkaConsumerFactory<String, ClaimCheck> consumerFactory = new DefaultKafkaConsumerFactory<>(map, new StringDeserializer(), userJsonDeserializer);
        consumerFactory.addListener(new MicrometerConsumerListener<>(Metrics.globalRegistry,
                Collections.singletonList(new ImmutableTag("consumerCustomMetrics", "kafka-consumer-metrics"))));
        return consumerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ClaimCheck> concurrentKafkaListenerClaimCheckConsumerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ClaimCheck> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(claimCheckConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setAutoStartup(Boolean.TRUE);
        return factory;
    }
}
