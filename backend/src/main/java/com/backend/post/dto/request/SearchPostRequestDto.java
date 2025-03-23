package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;
import lombok.Builder;

@Builder
public record SearchPostRequestDto(
        String title,
        PostCategory postCategory
) {}