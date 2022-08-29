package com.ex.kafka.consumer;

import com.ex.kafka.common.ClaimCheck;
import com.ex.kafka.common.CommonUtils;
import com.ex.kafka.common.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TestService {
    public void receive(Data data) {
        log.info("received data='{}' from {}", data, "TargetTopic");
    }

    public void receiveClaimCheck(ClaimCheck data) {
        Object payload = CommonUtils.readFromFile(data.getPath());
        log.info("received data='{}' from {}", payload, "claimCheckTopic");
    }
}
