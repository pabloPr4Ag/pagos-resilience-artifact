package com.pragma.resilience.impl;

import com.pragma.resilience.api.ResilienceOrchestrator;
import com.pragma.resilience.model.ExecutionContext;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResilienceOrchestratorImpl<R> implements ResilienceOrchestrator<R> {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Override
    public R execute(Supplier<R> action, ExecutionContext context) {
        log.info("Executing resilient action for service: {} with correlationId: {}", 
                 context.getServiceName(), context.getCorrelationId());

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(context.getServiceName());
        Retry retry = retryRegistry.retry(context.getServiceName());

        return CircuitBreaker.decorateSupplier(circuitBreaker, 
                                  Retry.decorateSupplier(retry, action)).get();
    }
}
