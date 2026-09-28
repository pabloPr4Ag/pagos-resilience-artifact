package com.pragma.resilience;

import com.pragma.resilience.api.ResilienceOrchestrator;
import com.pragma.resilience.impl.ResilienceOrchestratorImpl;
import com.pragma.resilience.model.ExecutionContext;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResilienceOrchestratorTest {

    private ResilienceOrchestrator<String> orchestrator;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RetryRegistry retryRegistry;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry = Mockito.mock(CircuitBreakerRegistry.class);
        retryRegistry = Mockito.mock(RetryRegistry.class);
        
        // Mocking the registries to return actual Resilience4j objects to avoid deep mocking
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("testService");
        io.github.resilience4j.retry.Retry retry = io.github.resilience4j.retry.Retry.ofDefaults("testService");
        
        when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(cb);
        when(retryRegistry.retry(anyString())).thenReturn(retry);

        orchestrator = new ResilienceOrchestratorImpl<>(circuitBreakerRegistry, retryRegistry);
    }

    @Test
    void execute_ShouldReturnSuccess_WhenActionSucceeds() {
        ExecutionContext context = ExecutionContext.builder()
                .serviceName("testService")
                .correlationId("corr-123")
                .build();

        Supplier<String> action = () -> "Success";

        String result = orchestrator.execute(action, context);

        assertEquals("Success", result);
    }

    @Test
    void execute_ShouldRetry_WhenActionFailsTransiently() {
        ExecutionContext context = ExecutionContext.builder()
                .serviceName("testService")
                .correlationId("corr-456")
                .build();

        AtomicInteger attempts = new AtomicInteger(0);
        Supplier<String> action = () -> {
            attempts.incrementAndGet();
            if (attempts.get() < 2) {
                throw new RuntimeException("Transient Error");
            }
            return "Recovered";
        };

        String result = orchestrator.execute(action, context);

        assertEquals("Recovered", result);
        assertEquals(2, attempts.get());
    }

    @Test
    void execute_ShouldThrowException_WhenAllRetriesFail() {
        ExecutionContext context = ExecutionContext.builder()
                .serviceName("testService")
                .correlationId("corr-789")
                .build();

        Supplier<String> action = () -> {
            throw new RuntimeException("Permanent Error");
        };

        assertThrows(RuntimeException.class, () -> orchestrator.execute(action, context));
    }
}
