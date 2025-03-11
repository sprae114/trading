package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Post;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

@Builder
public record RegisterPostRequestDto(
    @NotEmpty(message = "제목을 입력해주세요.")
    String title,

    @NotEmpty(message = "내용을 입력해주세요.")
    String body,
    Long customerId,
    String customerName,
    PostCategory category,
    List<String> imageUrls
) {
    public static Post toEntity(RegisterPostRequestDto request){
        return Post.builder()
                .title(request.title())
                .body(request.body())
                .customerId(request.customerId())
                .customerName(request.customerName())
                .views(0L)
                .category(request.category())
                .tradeStatus(TradeStatus.SALE)
                .imageUrls(request.imageUrls())
                .build();
    }
}