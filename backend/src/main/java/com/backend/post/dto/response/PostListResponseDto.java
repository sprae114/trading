package com.backend.post.dto.response;

import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Post;
import lombok.Builder;

import java.util.List;


@Builder
public record PostListResponseDto(
        Long id,
        String title,
        String body,
        Long customerId,
        String customerName,
        TradeStatus tradeStatus,
        PostCategory category,
        Long views,
        Long likesCount,
        List<byte[]> images
) {
    public static PostListResponseDto from(Post post, Long RedisPostViews, Long RedisLikeCount, List<byte[]> downImages) {
        return PostListResponseDto
                .builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .customerId(post.getCustomerId())
                .customerName(post.getCustomerName())
                .tradeStatus(post.getTradeStatus())
                .category(post.getCategory())
                .views(RedisPostViews)
                .likesCount(RedisLikeCount)
                .images(downImages)
                .build();
    }
}
