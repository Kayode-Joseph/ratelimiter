package com.kayode.ratelimiter.security.filter;

import com.kayode.ratelimiter.security.model.PathUserIdAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PathUserIdAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Pattern pattern = Pattern.compile("/users/(\\d+)");
        Matcher matcher = pattern.matcher(path);

        if (matcher.find()) {
            Long userId = Long.parseLong(matcher.group(1));

            // Set authentication in SecurityContext so we can get it back in the rate limiting filter
            PathUserIdAuthenticationToken authToken = new PathUserIdAuthenticationToken(userId);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
