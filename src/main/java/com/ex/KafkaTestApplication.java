package com.ex;

import com.ex.kafka.common.CommonUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(exclude = {KafkaAutoConfiguration.class})
@EnableKafka
@EnableRetry
public class KafkaTestApplication {

    public static void main(String[] args) {
        CommonUtils.startKafkaServer();
        SpringApplication.run(KafkaTestApplication.class, args);
    }
}
