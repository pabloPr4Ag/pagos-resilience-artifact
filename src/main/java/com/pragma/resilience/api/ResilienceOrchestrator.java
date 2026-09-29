package com.pragma.resilience.api;

import com.pragma.resilience.model.ExecutionContext;
import java.util.function.Supplier;

public interface ResilienceOrchestrator<R> {
    /**
     * Executes a supplier action wrapped with resilience patterns (Circuit Breaker and Retry).
     * 
     * @param action The logic to be executed.
     * @param context Context containing service name and correlation ID for tracking.
     * @return The result of the action.
     * @throws RuntimeException if the action fails after all retry attempts or if the circuit is open.
     */
    R execute(Supplier<R> action, ExecutionContext context);
}
