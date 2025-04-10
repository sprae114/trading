package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;

public record LikeSearchPostRequestDto(
        Long customerId,
        String title,
        PostCategory postCategory
) {}