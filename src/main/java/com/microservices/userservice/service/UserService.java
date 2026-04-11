package com.microservices.userservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

    public String getUser(String id, String failure, int duration) {

        long start = System.currentTimeMillis();

        log.info("event=request_received userId={}", id);

        try {

            log.info("event=db_call_start");

            simulateFailure(failure,  duration);

            log.info("event=db_call_end");

        } catch (Exception e) {

            log.error("event=exception message={}", e.getMessage());

            throw e;
        }

        long latency = System.currentTimeMillis() - start;

        if(latency > 1000){
            log.warn("event=slow_response latency={}", latency);
        }

        log.info("event=response_sent latency={}", latency);

        return "user-" + id;
    }

    private void simulateFailure(String failure , int duration) {

        try {

            if ("latency".equalsIgnoreCase(failure)) {
                Thread.sleep(duration); // simulate latency
            } else if ("error".equalsIgnoreCase(failure)) {
                throw new RuntimeException("Simulated failure");
            } else if ("cpu".equalsIgnoreCase(failure)) {
                long end = System.currentTimeMillis() + duration;

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