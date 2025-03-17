package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.user.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.backend.post.dto.request.RegisterPostRequestDto.*;


@Transactional
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // ObjectMapper 주입

    private static final String CACHE_KEY_PREFIX = "post:";

    public Post create(RegisterPostRequestDto request){
        return postRepository.save(toEntity(request));
    }

    public Post getOne(Long postId){
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString())
        );
    }

    public Post getOneWithView(Long postId){
        String cacheKey = CACHE_KEY_PREFIX + postId;


        // 1. Redis에서 캐시 확인
        Object cachedData = redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return objectMapper.convertValue(cachedData, Post.class); // Object를 Post로 변환
        }

        // 2. 캐시 없으면 MySQL에서 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString()));

        // 3. Redis에 캐싱 만료 설정
        redisTemplate.opsForValue().set(cacheKey, post, 30, TimeUnit.MINUTES);

        return post;
    }

    public Page<Post> getList(Pageable pageable){
        return postRepository.findAll(pageable);
    }

    public Post update(UpdateRequestDto request, Authentication authentication) throws Exception{
        // 1. 해당 post 찾기
        Post post = postRepository.findById(request.id()).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND, request.id().toString())
        );

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()))
                || post.getCustomerName().equals(authentication.getName())) {

            Post updatePost = post.toBuilder()
                    .title(request.title())
                    .body(request.body())
                    .category(request.category())
                    .imageUrls(request.imageUrls())
                    .build();

            return postRepository.save(updatePost);

        } else {
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("post.getCustomerName() : %s, authentication.getName() : %s",
                            post.getCustomerName(), authentication.getName()));
        }
    }

    public void delete(Long postId, Authentication authentication){
        // 1. 해당 post 찾기
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString())
        );

        if (authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()))
                || post.getCustomerName().equals(authentication.getName())) {
            // 3. 캐시 정보 삭제
            String cacheKey = CACHE_KEY_PREFIX + post.getId();
            redisTemplate.delete(cacheKey);

            // 4. post 삭제
            postRepository.deleteById(postId);
        } else {
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("post.getCustomerName() : %s, authentication.getName() : %s",
                            post.getCustomerName(), authentication.getName()));
        }
    }

    /**
     * likes 이용한 조회
     */
    public List<Post> getPostsByIds(List<Long> postIdList) {
        return postRepository.findAllByIdIn(postIdList);
    }
}
