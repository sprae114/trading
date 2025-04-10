package com.backend.post.controller;

import com.backend.post.dto.request.LikeSearchPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
import com.backend.post.dto.response.LikeResponseDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.model.entity.Likes;
import com.backend.post.service.LikesService;
import com.backend.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/my")
@RequiredArgsConstructor
public class MyController {

    private final PostService postService;
    private final LikesService likesService;

    /**
     * 내가 좋아요한 글 목록 조회
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/likes")
    public ResponseEntity<Page<PostListResponseDto>> getLikesPost(Long customerId,
                                                              @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<LikeResponseDto> likePage = likesService.getList(customerId, pageable);

        List<Long> postIds = likePage.getContent()
                .stream()
                .map(LikeResponseDto::postId)
                .toList();

        List<PostListResponseDto> posts = postService.getPostsByIds(postIds);

        return ResponseEntity.ok(new PageImpl<>(posts, pageable, likePage.getTotalElements()));
    }

    /**
     * 내가 작성한 글 + 제목 검색 목록 조회
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/search")
    public ResponseEntity<Page<PostListResponseDto>> searchPosts(@RequestBody @Valid LikeSearchPostRequestDto request,
                                                                 @PageableDefault(page = 0, size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        // page가 아닌 리스트로
//        Page<LikeResponseDto> likePage = likesService.getList(request.customerId(), pageable);
        List<LikeResponseDto> allLikes = likesService.getAllLikes(request.customerId());

        List<Long> postIds = allLikes.stream()
                .map(LikeResponseDto::postId)
                .toList();

        // 2. 제목 검색 + 페이징 결과 조회
        Page<PostListResponseDto> postsPage = postService.getPostsByIdsAndTitle(postIds, request.title(), pageable);

        return ResponseEntity.ok(postsPage);
    }

}
