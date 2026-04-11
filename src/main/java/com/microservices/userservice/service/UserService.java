package com.microservices.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    public String getUser(String id) {

        log.info("service=user-service event=request_received userId={}", id);

        long start = System.currentTimeMillis();

        log.info("service=user-service event=db_call_start");

        simulateLatency();

        log.info("service=user-service event=db_call_end");

        log.info("service=user-service event=processing");

        long latency = System.currentTimeMillis() - start;

        log.info("service=user-service event=response_sent latency={}ms", latency);

        return "user-" + id;
    }

    private void simulateLatency() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
