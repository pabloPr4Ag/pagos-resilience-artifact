package com.pragma.resilience.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public record ExecutionContext(
    String correlationId,
    String serviceName,
    int maxAttempts
) {}
