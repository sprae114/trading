package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateRequestDto(
        Long id,
        @NotEmpty(message = "제목을 입력해주세요.")
        String title,
        @NotEmpty(message = "내용을 입력해주세요.")
        String body,
        PostCategory category,
        List<String> imageUrls
) {
    public UpdateRequestDto( Long id, String title, String body) {
        this(id, title, body, null, List.of());
    }
}