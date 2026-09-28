package com.pragma.resilience.api;

import com.pragma.resilience.model.ExecutionContext;
import java.util.function.Supplier;

public interface ResilienceOrchestrator<R> {
    R execute(Supplier<R> action, ExecutionContext context);
}
