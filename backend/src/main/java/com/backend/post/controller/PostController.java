package com.backend.post.controller;

import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.LikeResponseDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.service.LikesService;
import com.backend.post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
@Slf4j
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
    public ResponseEntity<Page<PostListResponseDto>> getPostList(
            @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(postService.getList(pageable));
    }

    /**
     * 중고 거래 상세글 생성
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<Void> create(@RequestPart @Valid RegisterPostRequestDto requestDto,
                                       @RequestPart(value = "imageFiles", required = false) MultipartFile[] nonJsonImageFiles) throws IOException {

        postService.create(RegisterPostRequestDto.from(requestDto, nonJsonImageFiles));
        return ResponseEntity.ok().build();
    }


    /**
     * 중고 거래 상세글 조회
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostOne(@PathVariable Long postId, Authentication authentication) throws JsonProcessingException {
        PostResponseDto postResponseDto = postService.getOne(postId);
        boolean liked = likesService.isLiked(postId, authentication);

        return ResponseEntity
                .ok()
                .body(postResponseDto.toBuilder().isLiked(liked).build());
    }


    /**
     * 중고거래 상세글 수정
     */
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId,
                                           @RequestBody @Valid UpdateRequestDto updateRequestDto,
                                           Authentication authentication) throws Exception {

        postService.update(UpdateRequestDto.from(updateRequestDto), authentication);

        return ResponseEntity.ok().build();
    }


    /**
     * 중고거래 상세글 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam Long postId, Authentication authentication){
        postService.delete(postId, authentication);
        return ResponseEntity.ok().build();
    }


    /**
     * 제목과 카테로리로 검색
     */
    @PostMapping("/search")
    public ResponseEntity<Page<PostListResponseDto>> searchPosts(@RequestBody @Valid SearchPostRequestDto request,
                                                                 @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<PostListResponseDto> result = postService.searchByTitleAndCategory(request, pageable);

        return ResponseEntity
                .ok()
                .body(result);
    }

    /**
     * 중고거래 상세글 좋아요 저장
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> saveLike(@PathVariable Long postId, @RequestParam Long customerId) throws Exception {

        likesService.create(postId, customerId);
        return ResponseEntity.ok().build();
    }


    /**
     * 중고거래 상세글 좋아요 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> deleteLike(@PathVariable Long postId, @RequestParam Long customerId) throws Exception {

        likesService.deleteOne(postId, customerId);
        return ResponseEntity.ok().build();
    }
}