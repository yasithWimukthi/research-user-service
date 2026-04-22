package com.microservices.userservice.service;

import com.microservices.userservice.model.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final LogSender logSender;

    public String getUser(String id, String failure, int duration) {

        long start = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();

        log.info("event=request_received userId={}", id);

        logSender.send(new LogEvent(
                Instant.now().toString(),
                "user-service",
                "INFO",
                "request_received",
                requestId,
                null,
                null
        ));

        try {

            log.info("event=db_call_start");

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "INFO",
                    "db_call_start",
                    requestId,
                    null,
                    null
            ));

            simulateFailure(failure,  duration);

            log.info("event=db_call_end");

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "INFO",
                    "db_call_end",
                    requestId,
                    null,
                    null
            ));

        } catch (Exception e) {

            log.error("event=exception message={}", e.getMessage());

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "ERROR",
                    "error",
                    requestId,
                    null,
                    e.getMessage()
            ));

            throw e;
        }

        long latency = System.currentTimeMillis() - start;

        if(latency > 1000){
            log.warn("event=slow_response latency={}", latency);
            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "WARN",
                    "slow_response",
                    requestId,
                    latency,
                    null
            ));
        }

        log.info("event=response_sent latency={}", latency);

        logSender.send(new LogEvent(
                Instant.now().toString(),
                "user-service",
                "WARN",
                "response_sent",
                requestId,
                latency,
                null
        ));

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