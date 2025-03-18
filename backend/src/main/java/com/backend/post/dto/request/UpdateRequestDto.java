package com.backend.post.dto.request;

import com.backend.post.model.PostCategory;
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
        PostCategory category,
        MultipartFile[] imageFiles
) {
    public UpdateRequestDto(Long id, String title, String body) {
        this(id, title, body, null, new MultipartFile[0]);
    }
}