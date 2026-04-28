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

        addNoise(requestId);

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

        addNoise(requestId);

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

            long dbStart = System.currentTimeMillis();

            simulateFailure(failure,  duration, requestId);

            long dbLatency = System.currentTimeMillis() - dbStart;

            addNoise(requestId);

            log.info("event=db_call_end");

            addNoise(requestId);

            logSender.send(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "INFO",
                    "db_call_end",
                    requestId,
                    null,
                    null
            ));

            sendLogEvent(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "INFO",
                    "db_latency",
                    requestId,
                    dbLatency,
                    null
            ));

            sendLogEvent(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "INFO",
                    "request_completed_success",
                    requestId,
                    dbLatency,
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

            sendLogEvent(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "ERROR",
                    "request_completed_failure",
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
                "INFO",
                "response_sent",
                requestId,
                latency,
                null
        ));

        return "user-" + id;
    }

    private void simulateFailure(String failure , int duration, String requestId) {

        try {

            if ("slow_db".equalsIgnoreCase(failure)) {
                int delay = 500 + new Random().nextInt(1000);
                Thread.sleep(delay);

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "WARN",
                        "slow_db_response",
                        requestId,
                        (long) delay,
                        "Simulated slow database response"
                ));
            }

            else if ("db_spike".equalsIgnoreCase(failure)) {
                Thread.sleep(duration);

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "WARN",
                        "db_spike",
                        requestId,
                        (long) duration,
                        "Database spike simulated with "+ duration + "second delay"
                ));
            }

            else if ("db_timeout".equalsIgnoreCase(failure)) {

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "ERROR",
                        "db_timeout",
                        requestId,
                        null,
                        "Database timeout occurred."
                ));

                throw new RuntimeException("Database timeout");


            }

            else if ("null_data".equalsIgnoreCase(failure)) {

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "ERROR",
                        "null_data_issue",
                        requestId,
                        null,
                        "Null data returned."
                ));

                throw new RuntimeException("Null data returned");
            }

            else if ("validation_error".equalsIgnoreCase(failure)) {
                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "ERROR",
                        "validation_error",
                        requestId,
                        null,
                        "Validation error occurred."
                ));

                throw new IllegalArgumentException("Invalid input");
            }

            else if ("intermittent_db".equalsIgnoreCase(failure)) {
                if (Math.random() < 0.3) {

                    sendLogEvent(new LogEvent(
                            Instant.now().toString(),
                            "user-service",
                            "ERROR",
                            "intermittent_db_issue",
                            requestId,
                            null,
                            "Intermittent DB failure occurred."
                    ));

                    throw new RuntimeException("Intermittent DB failure");
                }
            }

            else if ("degradation".equalsIgnoreCase(failure)) {
                int delay = 200 + new Random().nextInt(8800);
                Thread.sleep(delay);

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "WARN",
                        "degradation",
                        requestId,
                        (long) delay,
                        "Degradation simulated with random delay"
                ));
            }

            else if ("cpu".equalsIgnoreCase(failure)) {
                long end = System.currentTimeMillis() + 2000;
                while (System.currentTimeMillis() < end) {
                    Math.sqrt(Math.random());
                }

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "WARN",
                        "high_cpu_usage",
                        requestId,
                        2000L,
                        "CPU usage spiked for 2 seconds"
                ));
            }

            else if ("memory".equalsIgnoreCase(failure)) {
                byte[] memoryHog = new byte[20 * 1024 * 1024]; // 20MB
                for (int i = 0; i < memoryHog.length; i++) {
                    memoryHog[i] = (byte) (i % 256);
                }
                Thread.sleep(500);

                sendLogEvent(new LogEvent(
                        Instant.now().toString(),
                        "user-service",
                        "WARN",
                        "high_memory_usage",
                        requestId,
                        500L,
                        "Memory usage spiked to 100MB"
                ));

            }

            else if ("random_failure".equalsIgnoreCase(failure)) {
                if (Math.random() < 0.5) {
                    sendLogEvent(new LogEvent(
                            Instant.now().toString(),
                            "user-service",
                            "ERROR",
                            "random_failure_occurred",
                            requestId,
                            null,
                            "Random failure triggered."
                    ));

                    throw new RuntimeException("Random failure occurred");
                }
            }

            else if ("retry".equalsIgnoreCase(failure)) {

                for (int i = 1; i <= 3; i++) {

                    sendLogEvent(new LogEvent(
                            Instant.now().toString(),
                            "user-service",
                            "WARN",
                            "retry_attempt",
                            requestId,
                            null,
                            "Retry attempt " + i
                    ));

                    Thread.sleep(200);
                }

                throw new RuntimeException("Failed after retries");
            }

            else {
                Thread.sleep(100); // normal
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendLogEvent(LogEvent logEvent) {
        logSender.send(logEvent);
    }

    private void addNoise(String requestId) {
        if (Math.random() < 0.2) {
            sendLogEvent(new LogEvent(
                    Instant.now().toString(),
                    "user-service",
                    "INFO",
                    "background_task",
                    requestId,
                    null,
                    "Non-critical background process"
            ));
        }
    }

       /*private void simulateFailure(String failure , int duration) {

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
    }*/

}