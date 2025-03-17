package com.backend.post.controller;

import com.backend.common.model.RedisRequest;
import com.backend.common.service.RedisService;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.model.entity.Likes;
import com.backend.post.service.LikesService;
import com.backend.post.service.PostService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final LikesService likesService;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;


    @GetMapping
    public ResponseEntity<Page<PostResponseDto>> getPostList(Pageable pageable){
        return ResponseEntity
                .ok()
                .body(postService.getList(pageable).map(PostResponseDto::from));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody @Valid RegisterPostRequestDto requestDto){
        postService.create(requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponseDto> getPostOne(@PathVariable Long postId) throws JsonProcessingException {
        // redis
        PostResponseDto postResponseDto = PostResponseDto.from(postService.getOne(postId));
        String key = "post:" + postResponseDto.id();

        if (redisService.get(key) != null) {
            redisService.setKeyWithExpiration(key, makeRedisValue(postResponseDto), 6000L);
        }

        return ResponseEntity
                .ok()
                .body(PostResponseDto.from(postService.getOne(postId)));
    }


    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId,
                                           @RequestBody @Valid UpdateRequestDto request,
                                           Authentication authentication) throws Exception {
        postService.update(request, authentication);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId,
                                           Authentication authentication) throws Exception {
        postService.delete(postId, authentication);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Void> saveLike(@PathVariable Long postId, Long customerId) throws Exception {

        likesService.create(postId, customerId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Void> deleteLike(@PathVariable Long postId, Long customerId) throws Exception {

        likesService.deleteOne(postId, customerId);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/likes")
    public ResponseEntity<List<PostResponseDto>> getLikesPost(Pageable pageable, Long customerId) {
        List<Long> likeList = likesService.getList(customerId, pageable).stream()
                .map(Likes::getPostId)
                .toList();

        return ResponseEntity
                .ok()
                .body(postService.getPostsByIds(likeList)
                        .stream()
                        .map(PostResponseDto::from)
                        .toList());
    }


    private String makeRedisValue(PostResponseDto postResponseDto) throws JsonProcessingException {
        RedisRequest redisRequest = RedisRequest.builder()
                .id(postResponseDto.id())
                .views(postResponseDto.views()+1) // 조회 +1한 후, 저장
                .build();

        return objectMapper.writeValueAsString(redisRequest);
    }
}