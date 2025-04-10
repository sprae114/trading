package com.backend.post.dto.response;

import com.backend.post.model.entity.Likes;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
public record LikeResponseDto(
        Long customerId,
        Long postId
) {
    public static LikeResponseDto toDto(Likes likes) {
        return LikeResponseDto.builder()
                .customerId(likes.getCustomerId())
                .postId(likes.getPostId())
                .build();
    }
}