package com.kayode.ratelimiter.security.filter;

import com.kayode.ratelimiter.security.component.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiter rateLimiter;

    public RateLimitingFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //get logged-in user
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Long userId) {
            if (!rateLimiter.isAllowed(userId)) {
                System.err.printf("Request Rejected for user id: %s at timestamp UTC: %s%n", userId, Instant.now().toString());
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. Try again later.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
