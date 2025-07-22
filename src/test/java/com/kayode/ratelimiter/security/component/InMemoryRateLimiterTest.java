package com.kayode.ratelimiter.security.component;

import com.kayode.ratelimiter.security.model.RateLimitProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRateLimiterTest {

    private InMemoryRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Given: a rate limiter with 3 requests allowed per 1-second window
        RateLimitProperties props = new RateLimitProperties();
        props.setMaxRequests(3);
        props.setWindow(Duration.ofSeconds(1));
        rateLimiter = new InMemoryRateLimiter(props);
    }

    @Test
    void givenUserWithinLimit_whenRequesting_thenAllowRequest() {
        // Given
        Long userId = 1L;

        // When
        boolean first = rateLimiter.isAllowed(userId);
        boolean second = rateLimiter.isAllowed(userId);
        boolean third = rateLimiter.isAllowed(userId);

        // Then
        assertTrue(first);
        assertTrue(second);
        assertTrue(third);
    }

    @Test
    void givenUserExceedsLimit_whenRequesting_thenRejectRequest() {
        // Given
        Long userId = 2L;
        rateLimiter.isAllowed(userId);
        rateLimiter.isAllowed(userId);
        rateLimiter.isAllowed(userId);

        // When
        boolean fourth = rateLimiter.isAllowed(userId);

        // Then
        assertFalse(fourth);
    }

    @Test
    void givenUserExceededLimit_whenWindowResets_thenAllowRequestAgain() throws InterruptedException {
        // Given
        Long userId = 3L;
        rateLimiter.isAllowed(userId);
        rateLimiter.isAllowed(userId);
        rateLimiter.isAllowed(userId);
        Thread.sleep(1100); // wait for window to reset

        // When
        boolean allowed = rateLimiter.isAllowed(userId);

        // Then
        assertTrue(allowed);
    }

    @Test
    void givenConcurrentRequests_whenExceedingLimit_thenOnlyAllowMaxRequests() throws InterruptedException, ExecutionException {
        // Given
        Long userId = 4L;
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Boolean>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> rateLimiter.isAllowed(userId)));
        }

        executor.shutdown();

        // Then
        long allowedCount = futures.stream().filter(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertEquals(3, allowedCount, "Only 3 requests should be allowed");
    }

    @Test
    void givenTwoUsers_whenOneHitsLimit_thenOtherIsNotAffected() throws InterruptedException, ExecutionException {
        // Given
        Long user1 = 100L;
        Long user2 = 200L;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable user1Task = () -> {
            for (int i = 0; i < 5; i++) {
                rateLimiter.isAllowed(user1); // will exceed limit
            }
        };

        Callable<Boolean> user2Task = () -> rateLimiter.isAllowed(user2);

        // When
        executor.submit(user1Task);
        Future<Boolean> user2Future = executor.submit(user2Task);

        executor.shutdown();

        // Then
        assertTrue(user2Future.get(), "User2 should not be blocked by User1");
    }
}
