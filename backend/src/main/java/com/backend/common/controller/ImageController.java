package com.backend.common.controller;

import com.backend.common.service.S3Service;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.model.entity.Post;
import com.backend.post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * s3 이미지 업로드
 */
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final S3Service s3Service;
    private final PostService postService;

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadImage(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> fileUrls = s3Service.uploadFiles(files);
        return ResponseEntity.ok(fileUrls);
    }

    /**
     * postId 에 대한 모든 파일 다운로드
     */
    @PostMapping("/download/post")
    public ResponseEntity<PostResponseDto> downloadImage(@RequestBody String postId) throws JsonProcessingException {

        return ResponseEntity
                .ok()
                .body(postService.getOne(Long.valueOf(postId)));
    }

    /**
     * 해당 파일 직접 다운로드
     */
    @PostMapping("/download/multiple")
    public ResponseEntity<List<byte[]>> downloadMultipleImages(@RequestBody List<String> fileNames) {
        List<byte[]> fileContents = s3Service.downloadFiles(fileNames);
        return ResponseEntity.ok(fileContents);
    }
}