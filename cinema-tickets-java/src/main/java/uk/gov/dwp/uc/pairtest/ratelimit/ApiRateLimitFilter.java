package uk.gov.dwp.uc.pairtest.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.dwp.uc.pairtest.api.ApiErrorResponse;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

@Component
@ConditionalOnProperty(name = "feature.rate-limit.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(RateLimitProperties.class)
public class ApiRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final Map<String, TokenBucket> bucketsByKey = new ConcurrentHashMap<>();

    public ApiRateLimitFilter(RateLimitProperties props) {
        this.props = props;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String key = clientKey(request);
        TokenBucket bucket = bucketsByKey.computeIfAbsent(key, k -> new TokenBucket(props));

        TokenBucket.TryConsumeResult result = bucket.tryConsume(1);
        if (result.consumed()) {
            response.setHeader("X-RateLimit-Remaining", String.valueOf(result.remainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(result.retryAfterSeconds()));

        var body = new ApiErrorResponse(
                Instant.now().truncatedTo(ChronoUnit.MILLIS),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase(),
                "Rate limit exceeded",
                List.of(),
                List.of()
        );

        // Avoid adding JSON dependency: write small JSON manually
        response.getWriter().write("{"
                + "\"timestamp\":\"" + body.timestamp() + "\","
                + "\"status\":" + body.status() + ","
                + "\"error\":\"" + body.error() + "\","
                + "\"message\":\"" + body.message() + "\","
                + "\"fieldErrors\":[],"
                + "\"ruleViolations\":[]"
                + "}");
    }

    private static String clientKey(HttpServletRequest request) {
        // Prefer forwarded IP if present (common behind reverse proxies).
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma >= 0 ? xff.substring(0, comma) : xff).trim();
        }

        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }

    static final class TokenBucket {
        private final long capacity;
        private final long refillTokens;
        private final long refillPeriodNanos;

        private long tokens;
        private long lastRefillNanos;

        TokenBucket(RateLimitProperties props) {
            this.capacity = Math.max(1, props.capacity());
            this.refillTokens = Math.max(1, props.refillTokens());
            this.refillPeriodNanos = Math.max(1, props.refillPeriod().toNanos());
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
        }

        synchronized TryConsumeResult tryConsume(long n) {
            refillIfNeeded();

            if (n <= tokens) {
                tokens -= n;
                return new TryConsumeResult(true, tokens, 0);
            }

            long nanosUntilNext = nanosUntilNextRefill();
            long retryAfterSeconds = Math.max(1, (nanosUntilNext + 999_999_999L) / 1_000_000_000L);
            return new TryConsumeResult(false, tokens, retryAfterSeconds);
        }

        private void refillIfNeeded() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed < refillPeriodNanos) {
                return;
            }

            long periods = elapsed / refillPeriodNanos;
            long add = periods * refillTokens;
            tokens = Math.min(capacity, tokens + add);
            lastRefillNanos += periods * refillPeriodNanos;
        }

        private long nanosUntilNextRefill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            long mod = elapsed % refillPeriodNanos;
            return refillPeriodNanos - mod;
        }

        record TryConsumeResult(boolean consumed, long remainingTokens, long retryAfterSeconds) {
        }
    }
}

