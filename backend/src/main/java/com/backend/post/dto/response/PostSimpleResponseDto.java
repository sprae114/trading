package com.backend.post.dto.response;

import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Post;
import lombok.Builder;

@Builder
public record PostSimpleResponseDto(
        Long postId,
        String title,
        Long price,
        PostCategory category,
        TradeStatus tradeStatus,
        String createdAt
) {
    public static PostSimpleResponseDto fromPost(Post post) {
        return PostSimpleResponseDto.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .price(post.getPrice())
                .category(post.getCategory())
                .tradeStatus(post.getTradeStatus())
                .createdAt(post.getCreatedAt().toString())
                .build();
    }
}
