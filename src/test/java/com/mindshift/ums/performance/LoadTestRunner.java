package com.mindshift.ums.performance;

import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Standalone load testing tool for UMS Service.
 * Can be run independently to test a deployed UMS instance.
 *
 * Usage:
 * java -cp ... com.mindshift.ums.performance.LoadTestRunner
 */
public class LoadTestRunner {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String API_KEY = "perf-test-api-key";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final AtomicInteger successCounter = new AtomicInteger(0);
    private final AtomicInteger errorCounter = new AtomicInteger(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final List<Long> responseTimes = new CopyOnWriteArrayList<>();

    public LoadTestRunner() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(50, 5, TimeUnit.MINUTES))
                .build();
    }

    public static void main(String[] args) {
        LoadTestRunner runner = new LoadTestRunner();

        System.out.println("=== UMS Load Test Runner ===");
        System.out.println("Target: " + BASE_URL);
        System.out.println("API Key: " + API_KEY);
        System.out.println();

        try {
            // Test 1: Single request latency
            runner.testSingleRequestLatency();

            // Test 2: Sustained load test
            runner.testSustainedLoad(10, 60, 100); // 10 threads, 60 seconds, 100 req/sec target

            // Test 3: Spike test
            runner.testSpikeLoad(50, 10, 500); // 50 threads, 10 seconds, 500 req/sec target

            // Test 4: Stress test
            runner.testStressLoad(100, 30); // 100 threads, 30 seconds

        } catch (Exception e) {
            System.err.println("Load test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            runner.shutdown();
        }
    }

    public void testSingleRequestLatency() throws IOException {
        System.out.println("=== Single Request Latency Test ===");

        Map<String, Object> requestBody = createTestMessageRequest("latency-test");
        String json = objectMapper.writeValueAsString(requestBody);

        long startTime = System.nanoTime();

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/messages")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("X-Idempotency-Key", "latency-test")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            long endTime = System.nanoTime();
            long latencyMs = Duration.ofNanos(endTime - startTime).toMillis();

            System.out.println("Response Code: " + response.code());
            System.out.println("Latency: " + latencyMs + "ms");

            if (response.isSuccessful()) {
                System.out.println("✅ Single request test passed");
            } else {
                System.out.println("❌ Single request test failed: " + response.body().string());
            }
        }

        System.out.println();
    }

    public void testSustainedLoad(int threadCount, int durationSeconds, int targetRps) throws InterruptedException {
        System.out.println("=== Sustained Load Test ===");
        System.out.println("Threads: " + threadCount);
        System.out.println("Duration: " + durationSeconds + " seconds");
        System.out.println("Target RPS: " + targetRps);

        resetCounters();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);

        long delayBetweenRequests = 1000L / targetRps; // ms between requests

        Instant startTime = Instant.now();
        Instant endTime = startTime.plusSeconds(durationSeconds);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            scheduler.scheduleAtFixedRate(() -> {
                if (Instant.now().isAfter(endTime)) {
                    return;
                }

                try {
                    sendTestMessage("sustained-" + threadId + "-" + requestCounter.incrementAndGet());
                } catch (Exception e) {
                    errorCounter.incrementAndGet();
                }
            }, threadId * (delayBetweenRequests / threadCount), delayBetweenRequests, TimeUnit.MILLISECONDS);
        }

        // Wait for test duration
        Thread.sleep(durationSeconds * 1000L);
        scheduler.shutdown();

        printResults("Sustained Load Test", durationSeconds);
    }

    public void testSpikeLoad(int threadCount, int durationSeconds, int targetRps) throws InterruptedException {
        System.out.println("=== Spike Load Test ===");
        System.out.println("Threads: " + threadCount);
        System.out.println("Duration: " + durationSeconds + " seconds");
        System.out.println("Target RPS: " + targetRps);

        resetCounters();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Instant startTime = Instant.now();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    Instant threadEndTime = startTime.plusSeconds(durationSeconds);
                    int requestsPerThread = (targetRps * durationSeconds) / threadCount;

                    for (int j = 0; j < requestsPerThread && Instant.now().isBefore(threadEndTime); j++) {
                        try {
                            sendTestMessage("spike-" + threadId + "-" + j);
                        } catch (Exception e) {
                            errorCounter.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();

        // Wait for all threads to complete
        endLatch.await();
        executor.shutdown();

        printResults("Spike Load Test", durationSeconds);
    }

    public void testStressLoad(int threadCount, int durationSeconds) throws InterruptedException {
        System.out.println("=== Stress Load Test ===");
        System.out.println("Threads: " + threadCount);
        System.out.println("Duration: " + durationSeconds + " seconds");
        System.out.println("Mode: Maximum throughput");

        resetCounters();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        Instant startTime = Instant.now();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    Instant threadEndTime = startTime.plusSeconds(durationSeconds);
                    int requestCount = 0;

                    while (Instant.now().isBefore(threadEndTime)) {
                        try {
                            sendTestMessage("stress-" + threadId + "-" + requestCount);
                            requestCount++;
                        } catch (Exception e) {
                            errorCounter.incrementAndGet();
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        printResults("Stress Load Test", durationSeconds);
    }

    private void sendTestMessage(String idempotencyKey) throws IOException {
        Map<String, Object> requestBody = createTestMessageRequest(idempotencyKey);
        String json = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(BASE_URL + "/v1/messages")
                .post(RequestBody.create(json, MediaType.get("application/json")))
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("X-Idempotency-Key", idempotencyKey)
                .build();

        long startTime = System.nanoTime();

        try (Response response = httpClient.newCall(request).execute()) {
            long endTime = System.nanoTime();
            long responseTimeMs = Duration.ofNanos(endTime - startTime).toMillis();

            responseTimes.add(responseTimeMs);
            totalResponseTime.addAndGet(responseTimeMs);

            if (response.isSuccessful()) {
                successCounter.incrementAndGet();
            } else {
                errorCounter.incrementAndGet();
                System.err.println("Request failed: " + response.code() + " - " + response.message());
            }
        }
    }

    private Map<String, Object> createTestMessageRequest(String idempotencyKey) {
        return Map.of(
            "channel", "SMS",
            "templateCode", "perf_test_sms",
            "locale", "ko",
            "to", Map.of("phone", "+821012345678"),
            "routing", Map.of("priority", "NORMAL"),
            "templateData", Map.of("username", "로드테스트")
        );
    }

    private void resetCounters() {
        requestCounter.set(0);
        successCounter.set(0);
        errorCounter.set(0);
        totalResponseTime.set(0);
        responseTimes.clear();
    }

    private void printResults(String testName, int durationSeconds) {
        int totalRequests = successCounter.get() + errorCounter.get();
        double rps = (double) totalRequests / durationSeconds;
        double successRate = totalRequests > 0 ? (double) successCounter.get() / totalRequests * 100 : 0;
        double avgResponseTime = totalRequests > 0 ? (double) totalResponseTime.get() / totalRequests : 0;

        // Calculate percentiles
        responseTimes.sort(Long::compareTo);
        long p50 = getPercentile(responseTimes, 50);
        long p95 = getPercentile(responseTimes, 95);
        long p99 = getPercentile(responseTimes, 99);

        System.out.println();
        System.out.println("=== " + testName + " Results ===");
        System.out.println("Total Requests: " + totalRequests);
        System.out.println("Successful: " + successCounter.get());
        System.out.println("Failed: " + errorCounter.get());
        System.out.println("Success Rate: " + String.format("%.2f", successRate) + "%");
        System.out.println("Requests/Second: " + String.format("%.2f", rps));
        System.out.println("Avg Response Time: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("P50 Response Time: " + p50 + "ms");
        System.out.println("P95 Response Time: " + p95 + "ms");
        System.out.println("P99 Response Time: " + p99 + "ms");
        System.out.println();
    }

    private long getPercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) return 0;
        int index = (int) Math.ceil(sortedValues.size() * percentile / 100.0) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    private void shutdown() {
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }
}