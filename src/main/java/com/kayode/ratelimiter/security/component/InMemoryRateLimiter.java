package com.kayode.ratelimiter.security.component;

import com.kayode.ratelimiter.security.model.RateLimitProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiter implements RateLimiter {

    //configurable max number of request per configurable window
    private final RateLimitProperties properties;
    private final Map<Long, Deque<Instant>> requestTimestamps = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isAllowed(Long userId) {
        Instant now = Instant.now();
        Deque<Instant> timestamps = requestTimestamps.computeIfAbsent(userId, k -> new ArrayDeque<>());
        //sync on specific user queue so other users are not blocked
        //while we are trying to figure out if a user has exceeded their rate
        synchronized (timestamps) {
            // Remove expired timestamps
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(now.minus(properties.getWindow()))) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= properties.getMaxRequests()) {
                return false;
            } else {
                timestamps.addLast(now);
                return true;
            }
        }
    }
}
