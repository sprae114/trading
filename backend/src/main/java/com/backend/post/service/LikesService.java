package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.model.RedisRequest;
import com.backend.common.service.RedisService;
import com.backend.post.dto.response.LikeResponseDto;
import com.backend.post.model.entity.Likes;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.LikesRepository;
import com.backend.post.repository.PostRepository;
import com.backend.user.model.Role;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;
    private final PostRepository postRepository;

    private static final String CACHE_KEY_PREFIX = "post:";

    @Transactional
    public LikeResponseDto create(Long postId, Long customerId) throws JsonProcessingException {
        log.info("Creating like for postId: {}, customerId: {}", postId, customerId);

        likesRepository.findByPostIdAndCustomerId(postId, customerId).ifPresent(it -> {
            log.warn("Post already liked by customer - postId: {}, customerId: {}", postId, customerId);
            throw new CustomException(ErrorCode.ALREADY_LIKED_POST,
                    String.format("postId: %s, customerId: %s", postId, customerId));
        });

        Likes likes = Likes.builder()
                .postId(postId)
                .customerId(customerId)
                .build();

        Likes savedLikes = likesRepository.save(likes);
        updateLikesWithRedis(postId, true);

        log.info("Successfully created like for postId: {}, customerId: {}", postId, customerId);
        return LikeResponseDto.toDto(savedLikes);
    }

    /**
     * 해당 좋아요 있는지 확인
     */
    @Transactional(readOnly = true)
    public LikeResponseDto getOne(Long postId, Long customerId) {
        log.debug("Fetching like for postId: {}, customerId: {}", postId, customerId);


        Likes likes = likesRepository.findByPostIdAndCustomerId(postId, customerId)
                .orElseThrow(() -> {
                    log.warn("Like not found for postId: {}, customerId: {}", postId, customerId);
                    return new CustomException(ErrorCode.LIKES_NOT_FOUND,
                            String.format("postId: %s, customerId: %s", postId, customerId));
                });

        return LikeResponseDto.toDto(likes);
    }


    /**
     * DB에 좋아요 수 조회
     */
    @Transactional(readOnly = true)
    public Long countLikes(Long postId) {
        return likesRepository.countByPostId(postId);
    }

    /**
     * 사용자의 좋아요 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<LikeResponseDto> getList(Long customerId, Pageable pageable){
        return likesRepository
                .findAllByCustomerId(customerId, pageable)
                .map(LikeResponseDto::toDto);
    }

    /**
     * 고객의 좋아요 삭제 (사용자용)
     */
    @Transactional
    public void deleteOne(Long postId, Long customerId) throws JsonProcessingException {
        log.info("Deleting like for postId: {}, customerId: {}", postId, customerId);

        Likes likes = likesRepository.findByPostIdAndCustomerId(postId, customerId)
                .orElseThrow(() -> {
                    log.warn("Like not found for postId: {}, customerId: {}", postId, customerId);
                    return new CustomException(ErrorCode.USER_NOT_MATCH, customerId.toString());
                });

        likesRepository.deleteByPostIdAndCustomerId(likes.getPostId(), customerId);
        updateLikesWithRedis(postId, false); // 좋아요 수 감소

        log.info("Successfully deleted like for postId: {}, customerId: {}", postId, customerId);
    }

    /**
     * 고객의 모든 좋아요 삭제 (관리자용)
     */
    @Transactional
    public void deleteList(Long customerId, Authentication authentication) throws JsonProcessingException {
        log.info("Deleting all likes for customerId: {}", customerId);
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()));

        if (isAdmin) {
            List<Likes> likesList = likesRepository.findAllByCustomerId(customerId, Pageable.unpaged()).getContent();
            if (!likesList.isEmpty()) {
                // 각 게시글의 좋아요 수 감소
                for (Likes like : likesList) {
                    updateLikesWithRedis(like.getPostId(), false);
                }
                likesRepository.deleteAllByCustomerId(customerId);
                log.info("Successfully deleted all likes for customerId: {}", customerId);
            } else {
                log.info("No likes found to delete for customerId: {}", customerId);
            }
        } else {
            log.warn("User {} is not authorized to delete likes for customerId: {}", authentication.getName(), customerId);
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED, "Admin access required");
        }
    }

    /**
     * redis로 저장한 후 like 갯수 가져오기
     */
    public Long countLikesWithRedis(Long postId) throws JsonProcessingException {
        String key = CACHE_KEY_PREFIX + postId;

        log.info("Starting countLikes for postId: {}", postId);

        // Redis에서 기존 값 가져오기
        Object cachedValue = redisService.get(key);

        if (cachedValue == null) {
            Post findPost = postRepository.findById(postId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString()));
            Long count = likesRepository.countByPostId(postId);

            RedisRequest redisRequest = RedisRequest.builder()
                    .id(postId)
                    .views(findPost.getViews())
                    .likeCount(count)
                    .build();

            log.debug("Initialized Redis views from DB for postId: {}", postId);

            String redisValue = objectMapper.writeValueAsString(redisRequest);
            redisService.setKeyWithExpiration(key, redisValue, 6000L);

            return count;
        } else {
            RedisRequest redisRequest = objectMapper.readValue(cachedValue.toString(), RedisRequest.class);
            log.debug("Retrieved Redis cache for postId: {}, LikeCount: {}", postId, redisRequest.likeCount());
            return redisRequest.likeCount();
        }
    }

    /**
     * redis로 조회후, like 갯수 업데이트하기
     */
    private void updateLikesWithRedis(Long postId, boolean increment) throws JsonProcessingException {
        String key = CACHE_KEY_PREFIX + postId;

        log.info("Starting updateLikes for postId: {}, increment: {}", postId, increment);

        // Redis에서 기존 값 가져오기
        Object cachedValue = redisService.get(key);
        RedisRequest redisRequest;

        if (cachedValue == null) {
            // Post 조회
            Post findPost = postRepository.findById(postId)
                    .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString()));
            Long count = likesRepository.countByPostId(postId);

            redisRequest = RedisRequest.builder()
                    .id(postId)
                    .views(findPost.getViews())
                    .likeCount(count)
                    .build();

            log.debug("No Redis cache found for postId: {}, initialized with LikeCount: {}", postId, count);
        } else {
            redisRequest = objectMapper.readValue(cachedValue.toString(), RedisRequest.class);
            log.debug("Retrieved Redis cache for postId: {}, current LikeCount: {}", postId, redisRequest.likeCount());
        }

        // 좋아요 수 증가 또는 감소
        Long currentLikes = redisRequest.likeCount();
        Long updatedLikes = increment ? currentLikes + 1 : Math.max(0, currentLikes - 1); // 감소 시 0 미만 방지
        redisRequest = RedisRequest.builder()
                .id(postId)
                .views(redisRequest.views()) // 기존 조회수 유지
                .likeCount(updatedLikes)
                .build();

        // 업데이트된 값 Redis에 저장
        String redisValue = objectMapper.writeValueAsString(redisRequest);
        redisService.setKeyWithExpiration(key, redisValue, 6000L);
        log.info("Updated LikeCount for postId: {} from {} to {}", postId, currentLikes, updatedLikes);
    }
}
