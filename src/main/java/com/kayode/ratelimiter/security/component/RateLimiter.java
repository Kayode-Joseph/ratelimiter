package com.kayode.ratelimiter.security.component;

public interface RateLimiter {
    boolean isAllowed(Long userId);
}
