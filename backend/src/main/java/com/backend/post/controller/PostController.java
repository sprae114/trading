package com.backend.post.controller;

import com.backend.common.service.S3Service;
import com.backend.post.dto.request.RegisterPostRequestDto;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> create(@RequestBody @Valid RegisterPostRequestDto requestDto) throws IOException {
        postService.create(requestDto);
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
                                           @RequestBody @Valid UpdateRequestDto request,
                                           Authentication authentication) throws Exception {
        postService.update(request, authentication);
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