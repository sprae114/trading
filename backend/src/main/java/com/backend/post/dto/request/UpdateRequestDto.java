package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
public record UpdateRequestDto(
        Long id,
        @NotEmpty(message = "제목을 입력해주세요.")
        String title,
        @NotEmpty(message = "내용을 입력해주세요.")
        String body,
        Long price,

        TradeStatus tradeStatus,
        PostCategory category,

        @JsonIgnore // JSON 직릴화 제외
        MultipartFile[] imageFiles
) {
        public static UpdateRequestDto from(UpdateRequestDto request, MultipartFile[] NonJsonImageFiles) {
                return UpdateRequestDto
                        .builder()
                        .id(request.id())
                        .title(request.title())
                        .body(request.body())
                        .price(request.price())
                        .tradeStatus(request.tradeStatus())
                        .category(request.category())
                        .imageFiles(NonJsonImageFiles)
                        .build();
        }

        public static UpdateRequestDto from(UpdateRequestDto request) {
                return UpdateRequestDto
                        .builder()
                        .id(request.id())
                        .title(request.title())
                        .body(request.body())
                        .price(request.price())
                        .tradeStatus(request.tradeStatus())
                        .category(request.category())
                        .build();
        }
}