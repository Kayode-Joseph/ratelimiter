package com.kayode.ratelimiter.security.model;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class PathUserIdAuthenticationToken extends AbstractAuthenticationToken {
    private final Long userId;

    public PathUserIdAuthenticationToken(Long userId) {
        super(null);
        this.userId = userId;
        setAuthenticated(true); // you can set this to false and have a provider validate it too
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public Object getCredentials() {
        return null; // No credentials, just a userId
    }
}
