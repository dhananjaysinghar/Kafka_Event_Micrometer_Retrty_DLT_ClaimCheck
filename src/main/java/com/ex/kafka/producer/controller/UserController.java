package com.ex.kafka.producer.controller;

import com.ex.kafka.common.Data;
import com.ex.kafka.producer.UserKafkaSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserKafkaSender userKafkaSender;

    @PostMapping("/send")
    public String sendData() {
        Data data = Data.builder().name("DJ").mobile("9090123451").address("Bangalore").build();
        userKafkaSender.sendToTarget(data);
        return "Successfully published Payload to Kafka topic";
    }
}
