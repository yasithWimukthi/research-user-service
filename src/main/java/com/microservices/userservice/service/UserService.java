package com.microservices.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    public String getUser(String id, String failure) {

        long start = System.currentTimeMillis();

        log.info("event=request_received userId={} failure={}", id, failure);

        simulateFailure(failure);

        long latency = System.currentTimeMillis() - start;

        log.info("event=response_sent latency={}", latency);

        return "user-" + id;
    }

    private void simulateFailure(String failure) {

        try {

            if ("latency".equalsIgnoreCase(failure)) {
                Thread.sleep(2000);
            } else if ("error".equalsIgnoreCase(failure)) {
                throw new RuntimeException("Simulated failure");
            } else if ("cpu".equalsIgnoreCase(failure)) {
                long end = System.currentTimeMillis() + 2000;

                while (System.currentTimeMillis() < end) {
                    Math.sqrt(Math.random());
                }
            } else {
                Thread.sleep(100); // normal
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}