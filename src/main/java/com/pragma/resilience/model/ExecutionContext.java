package com.pragma.resilience.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecutionContext {
    private String correlationId;
    private String serviceName;
    private int maxAttempts;
}
