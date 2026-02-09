package com.minhtetthar.post_now.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;
    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Skip rate limiting for health checks and static resources
        if (shouldSkipRateLimiting(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get identifier (IP for anonymous, username for authenticated)
        String identifier = getIdentifier(request);

        // Get rate limit tier based on endpoint
        Bucket bucket = resolveBucket(identifier, path, method);

        // Try to consume a token
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;

            log.warn("Rate limit exceeded for identifier: {} on path: {}", identifier, path);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again in %d seconds.\",\"retryAfter\":%d}",
                    waitForRefill, waitForRefill));
        }
    }

    private boolean shouldSkipRateLimiting(String path) {
        return path.startsWith("/h2-console") ||
                path.startsWith("/actuator") ||
                path.equals("/health") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs");
    }

    private String getIdentifier(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Use username for authenticated users
            return "user:" + authentication.getName();
        } else {
            // Use IP address for anonymous users
            String clientIP = getClientIP(request);
            return "ip:" + clientIP;
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private Bucket resolveBucket(String identifier, String path, String method) {
        String key = identifier + ":" + getRateLimitTier(path, method);
        return cache.computeIfAbsent(key, k -> createBucketForTier(path, method));
    }

    private String getRateLimitTier(String path, String method) {
        // Strict limits for authentication endpoints
        if (path.startsWith("/api/auth/")) {
            return "STRICT";
        }

        // Moderate limits for write operations
        if ("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method)) {
            if (path.contains("/posts") || path.contains("/comments") ||
                    path.contains("/likes") || path.contains("/follow")) {
                return "MODERATE";
            }
        }

        // Check if user is authenticated for higher limits
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "AUTHENTICATED";
        }

        // Lenient limits for read operations
        return "LENIENT";
    }

    private Bucket createBucketForTier(String path, String method) {
        String tier = getRateLimitTier(path, method);

        return switch (tier) {
            case "STRICT" -> rateLimitConfig.createStrictBucket();
            case "MODERATE" -> rateLimitConfig.createModerateBucket();
            case "AUTHENTICATED" -> rateLimitConfig.createAuthenticatedBucket();
            default -> rateLimitConfig.createLenientBucket();
        };
    }
}
