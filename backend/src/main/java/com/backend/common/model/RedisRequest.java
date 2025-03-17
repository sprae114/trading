package com.backend.common.model;

import lombok.Builder;

@Builder
public record RedisRequest(
        Long id,
        Long views
) {}
