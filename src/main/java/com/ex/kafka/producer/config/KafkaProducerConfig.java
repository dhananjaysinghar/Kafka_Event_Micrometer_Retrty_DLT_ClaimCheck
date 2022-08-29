package com.ex.kafka.producer.config;

import com.ex.kafka.common.ClaimCheck;
import com.ex.kafka.common.Data;
import com.ex.kafka.common.KafkaConfiguration;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.Metrics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.MicrometerProducerListener;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.retry.annotation.EnableRetry;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@AllArgsConstructor
@EnableRetry
public class KafkaProducerConfig {

    private KafkaConfiguration kafkaConfiguration;
    private static final Map<String, Object> DEFAULT_PRODUCER_CONFIG_MAP = new HashMap<>();

    @PostConstruct
    public void configureKafkaProperties() {
        //Required
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfiguration.getBootstrapServers());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Compress the kafka payload
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, kafkaConfiguration.getCompressionType());

        //Optional
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfiguration.getProducerLinger());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfiguration.getProducerRequestTimeoutMs());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.BATCH_SIZE_CONFIG, kafkaConfiguration.getProducerBatchSize());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.SEND_BUFFER_CONFIG, kafkaConfiguration.getProducerSendBuffer());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.ACKS_CONFIG, kafkaConfiguration.getProducerAcksConfig());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaConfiguration.getRetryBackoffMs());
        DEFAULT_PRODUCER_CONFIG_MAP.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, kafkaConfiguration.getMaxRequestSize());


//        if (Boolean.TRUE.equals(kafkaConfiguration.getSaslRequired())) {
//            DEFAULT_PRODUCER_CONFIG_MAP.put("security.protocol", kafkaConfiguration.getSecurityProtocol());
//            DEFAULT_PRODUCER_CONFIG_MAP.put("sasl.mechanism", kafkaConfiguration.getSaslMechanism());
//            DEFAULT_PRODUCER_CONFIG_MAP.put("sasl.jaas.config", kafkaConfiguration.getLoginModule());
//        }
    }

    @Bean
    public KafkaTemplate<String, Data> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, Data> producerFactory() {
        JsonSerializer<Data> userJsonSerializer = new JsonSerializer<>();
        userJsonSerializer.setAddTypeInfo(false);
        DefaultKafkaProducerFactory<String, Data> producerFactory = new DefaultKafkaProducerFactory<>(DEFAULT_PRODUCER_CONFIG_MAP, new StringSerializer(), userJsonSerializer);
        //Add Micrometer customMetrics
        producerFactory.addListener(new MicrometerProducerListener<>(Metrics.globalRegistry, Collections.singletonList(new ImmutableTag("producerCustomMetrics", "kafka-producer-metrics"))));
        return producerFactory;
    }


    @Bean
    public KafkaTemplate<String, ClaimCheck> kafkaClaimCheckTemplate() {
        return new KafkaTemplate<>(producerClaimCheckFactory());
    }

    @Bean
    public ProducerFactory<String, ClaimCheck> producerClaimCheckFactory() {
        JsonSerializer<ClaimCheck> userJsonSerializer = new JsonSerializer<>();
        userJsonSerializer.setAddTypeInfo(false);
        DefaultKafkaProducerFactory<String, ClaimCheck> producerFactory = new DefaultKafkaProducerFactory<>(DEFAULT_PRODUCER_CONFIG_MAP, new StringSerializer(), userJsonSerializer);
        //Add Micrometer customMetrics
        producerFactory.addListener(new MicrometerProducerListener<>(Metrics.globalRegistry, Collections.singletonList(new ImmutableTag("producerClaimCheckCustomMetrics", "kafka-claim-check-producer-metrics"))));
        return producerFactory;
    }
}
