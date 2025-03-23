package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Post;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

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

    @JsonIgnore // JSON 직릴화 제외
    MultipartFile[] imageFiles
) {
    public static RegisterPostRequestDto from(RegisterPostRequestDto request, MultipartFile[] NonJsonImageFiles) {
        return RegisterPostRequestDto
                .builder()
                .title(request.title())
                .body(request.body())
                .customerId(request.customerId())
                .customerName(request.customerName())
                .category(request.category())
                .imageFiles(NonJsonImageFiles)
                .build();
    }

    public static Post toEntity(RegisterPostRequestDto request, List<String> uploadKeys) {
        return Post.builder()
                .title(request.title())
                .body(request.body())
                .customerId(request.customerId())
                .customerName(request.customerName())
                .views(0L)
                .category(request.category())
                .tradeStatus(TradeStatus.SALE)
                .views(0L)
                .imageUrls(uploadKeys)
                .build();
    }
}