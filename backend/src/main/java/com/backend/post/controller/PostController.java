package com.backend.post.controller;

import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.service.LikesService;
import com.backend.post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 중고거래 글 API
 */
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikesService likesService;

    /**
     * 중고거래 글 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<PostListResponseDto>> getPostList(Pageable pageable){
        return ResponseEntity.ok(postService.getList(pageable));
    }

    /**
     * 중고 거래 상세글 생성
     */
    @PostMapping
    public ResponseEntity<Void> create(@RequestPart @Valid RegisterPostRequestDto requestDto,
                                       @RequestPart(value = "imageFiles", required = false) MultipartFile[] nonJsonImageFiles) throws IOException {

        postService.create(RegisterPostRequestDto.from(requestDto, nonJsonImageFiles));
        return ResponseEntity.ok().build();
    }


    /**
     * 중고 거래 상세글 조회
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostOne(@PathVariable Long postId) throws JsonProcessingException {

        return ResponseEntity
                .ok()
                .body(postService.getOne(postId));
    }


    /**
     * 중고거래 상세글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId,
                                           @RequestPart @Valid UpdateRequestDto requestDto,
                                           @RequestPart(value = "imageFiles", required = false) MultipartFile[] nonJsonImageFiles,
                                           Authentication authentication) throws Exception {

        postService.update(UpdateRequestDto.from(requestDto, nonJsonImageFiles), authentication);

        return ResponseEntity.ok().build();
    }


    /**
     * 중고거래 상세글 삭제
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteAll(@RequestBody List<Long> postIds,
                                          Authentication authentication){
        postService.deleteList(postIds, authentication);
        return ResponseEntity.ok().build();
    }


    /**
     * 제목과 카테로리로 검색
     */
    @PostMapping("/search")
    public ResponseEntity<Page<PostListResponseDto>> searchPosts(@RequestBody @Valid SearchPostRequestDto request,
                                                                 Pageable pageable) {
        Page<PostListResponseDto> result = postService.searchByTitleAndCategory(request, pageable);

        return ResponseEntity
                .ok()
                .body(result);
    }


    /**
     * 카테고리 버튼을 위한 검색
     */
    @PostMapping("/category")
    public ResponseEntity<Page<PostListResponseDto>> searchCategory(@RequestBody @Valid SearchPostRequestDto request,
                                                                    Pageable pageable) {
        Page<PostListResponseDto> result = postService.searchByCategory(request, pageable);

        return ResponseEntity
                .ok()
                .body(result);
    }


    /**
     * 중고거래 상세글 좋아요 저장
     */
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> saveLike(@PathVariable Long postId, Long customerId) throws Exception {

        likesService.create(postId, customerId);
        return ResponseEntity.ok().build();
    }


    /**
     * 중고거래 상세글 좋아요 삭제
     */
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> deleteLike(@PathVariable Long postId, Long customerId) throws Exception {

        likesService.deleteOne(postId, customerId);
        return ResponseEntity.ok().build();
    }
}