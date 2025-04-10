package com.backend.post.dto.response;

import com.backend.post.model.PostCategory;
import com.backend.post.model.entity.Post;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record PostResponseDto(
        Long id,
        String title,
        String body,
        Long customerId,
        String customerName,
        Long price,
        PostCategory category,
        Long likeCount,
        Boolean isLiked,
        Long views,
        List<byte[]> images
) {
    public static PostResponseDto from(Post post, Long postViews, Long likeCount, Boolean isLiked, List<byte[]> downImages) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .customerId(post.getCustomerId())
                .customerName(post.getCustomerName())
                .price(post.getPrice())
                .category(post.getCategory())
                .isLiked(isLiked)
                .views(postViews)
                .likeCount(likeCount)
                .images(downImages)
                .build();
    }

    public static PostResponseDto from(Post post, Long postViews, Long likeCount, List<byte[]> downImages) {
        return PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .customerId(post.getCustomerId())
                .customerName(post.getCustomerName())
                .price(post.getPrice())
                .category(post.getCategory())
                .views(postViews)
                .likeCount(likeCount)
                .images(downImages)
                .build();
    }
}
