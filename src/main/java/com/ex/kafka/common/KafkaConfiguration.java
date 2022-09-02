package com.ex.kafka.common;

import lombok.Getter;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Configuration
@Getter
public class KafkaConfiguration {

    @Value("${app.kafka.host}")
    private String bootstrapServers;

    // Optional (Added default properties)
    @Value("${app.kafka.producer.acks-config:all}")
    private String producerAcksConfig;
    @Value("${app.kafka.producer.linger:5}")
    private int producerLinger;
    @Value("${app.kafka.producer.batch-size:16384}")
    private int producerBatchSize;
    @Value("${app.kafka.producer.send-buffer:131072}")
    private int producerSendBuffer;
    @Value("${app.kafka.producer.request.timeout.ms:30000}")
    private int producerRequestTimeoutMs;
    @Value("${app.kafka.producer.retry.backoff.ms:500}")
    private int retryBackoffMs;
    @Value("${app.kafka.producer.max.block.ms:1000}")
    private int maxBlockMs;
    @Value("${app.kafka.producer.compression.type:gzip}")
    private String compressionType;
    @Value("${app.kafka.producer.max.request.size:15000000}")
    private int maxRequestSize;

    @Value("${app.kafka.topic:test_topic}")
    private String targetTopic;
    @Value("${app.kafka.dead-letter-topic}")
    private String dlTopicName;

    @Value("${app.kafka.record-too-large-path}")
    private String recordTooLargePath;

    @Value("${app.kafka.claim_check_topic}")
    private String claimCheckTopic;


    //SSL Related
    //@Value("${app.kafka.properties.saslRequired:false}")
    private Boolean saslRequired;
    //@Value("${app.kafka.producer.login-module:}")
    private String loginModule;
    // @Value("${app.kafka.properties.sasl.mechanism:PLAIN}")
    private String saslMechanism;

    //@Value("${app.kafka.properties.security.protocol:SASL_SSL}")
    private String securityProtocol;

    public KafkaConfiguration(Environment environment) {
        if (Boolean.TRUE.equals(environment.getProperty("app.kafka.properties.saslRequired", Boolean.class))) {
            this.loginModule = environment.getProperty("app.kafka.producer.login-module");
            this.saslMechanism = environment.getProperty("app.kafka.properties.sasl.mechanism");
            this.securityProtocol = environment.getProperty("app.kafka.properties.security.protocol");
        }
    }

    @Bean
    public AdminClient adminClient() {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(config);
    }
}
