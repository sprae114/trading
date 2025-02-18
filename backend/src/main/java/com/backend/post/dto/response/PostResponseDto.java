package com.backend.post.dto.response;

import com.backend.post.model.PostCategory;
import com.backend.post.model.entity.Post;
import lombok.Builder;

import java.util.List;

@Builder
public record PostResponseDto(
        Long id,
        String title,
        String body,
        Long customerId,
        String customerName,
        PostCategory category,
        List<String> imageUrls
) {
    public static PostResponseDto from(Post post){
        return PostResponseDto
                .builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .customerId(post.getCustomerId())
                .customerName(post.getCustomerName())
                .category(post.getCategory())
                .imageUrls(post.getImageUrls())
                .build();
    }
}
