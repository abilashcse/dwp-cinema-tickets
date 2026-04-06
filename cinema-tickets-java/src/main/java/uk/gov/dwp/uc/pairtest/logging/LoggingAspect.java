package uk.gov.dwp.uc.pairtest.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@ConditionalOnProperty(name = "feature.logging.enabled", havingValue = "true", matchIfMissing = true)
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("within(uk.gov.dwp.uc.pairtest..*) && (" +
            "@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)" +
            ")")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long startNanos = System.nanoTime();

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String declaringType = sig.getDeclaringType().getSimpleName();
        String method = sig.getName();

        try {
            Object result = pjp.proceed();
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            log.info("{}.{} succeeded in {}ms", declaringType, method, elapsedMs);
            return result;
        } catch (Throwable t) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            log.warn("{}.{} failed in {}ms: {}", declaringType, method, elapsedMs, t.getMessage(), t);
            throw t;
        }
    }
}
