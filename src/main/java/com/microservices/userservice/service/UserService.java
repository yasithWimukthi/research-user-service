package com.microservices.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    public String getUser(String id) {

        long start = System.currentTimeMillis();

        log.info("event=request_received userId={}", id);

        simulateLatency();

        log.info("event=db_call_completed");

        long latency = System.currentTimeMillis() - start;

        log.info("event=response_sent latency={}", latency);

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