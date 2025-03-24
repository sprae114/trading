package com.backend.post.util;

import com.backend.common.model.RedisRequest;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.post.service.LikesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 스케쥴링을 통해
 * redis -> DB로 조회수 반영
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CountSyncScheduler {

    private final PostRepository postRepository;
    private final LikesService likesService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "post:";


    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void syncViewCounts() throws JsonProcessingException {

        // 1. redisTemplate에서 "post:"로 시작하는 모든 키 가져오기
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("No post data found in Redis.");
            return;
        }

        // 2. Redis에서 가져온 Post 데이터 맵으로 변환
        Map<Long, RedisRequest> redisPostMap = keys.stream()
                .map(key -> {
                    Object value = redisTemplate.opsForValue().get(key);
                    try {
                        return objectMapper.readValue(value.toString(), RedisRequest.class);
                    } catch (JsonProcessingException e) {
                        log.error("Redis parsing error, key: {}", key, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(RedisRequest::id, RedisRequest -> RedisRequest));


        // 3. DB에서 기존 Post 목록 조회
        List<Long> RedisRequestIds = redisPostMap.keySet().stream().toList();
        List<Post> dbPosts = postRepository.findAllById(RedisRequestIds);


        // 4. view 값 비교 후 수정
        List<Post> postsToUpdate = dbPosts.stream()
                .map(dbPost ->{
                    RedisRequest redisRequest = redisPostMap.get(dbPost.getId());
                    return dbPost.toBuilder()
                            .views(redisRequest.views())
                            .build();
                })
                .collect(Collectors.toList());

        // 5. 변경된 내용 DB에 반영
        if (!postsToUpdate.isEmpty()) {
            postRepository.saveAll(postsToUpdate);
            log.info("Successfully synchronized view counts for {} posts.", postsToUpdate.size());
        } else {
            log.info("No view count updates needed.");
        }

        // 6. 스케줄링 완료 로그
        log.info("View count synchronization completed at {}.", LocalDateTime.now());
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void syncLikeCount() throws JsonProcessingException {
        // 1. redisTemplate에서 "post:"로 시작하는 모든 키 가져오기
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("No like data found in Redis.");
            return;
        }

        // 2. Redis에서 가져온 데이터 처리
        int updatedCount = 0;
        for (String key : keys) {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                continue;
            }

            try {
                RedisRequest redisRequest = objectMapper.readValue(value.toString(), RedisRequest.class);
                Long postId = redisRequest.id();
                Long redisLikeCount = redisRequest.likeCount();

                // 3. DB에서 실제 좋아요 수 조회
                Long dbLikeCount = likesService.countLikes(postId);

                // 4. Redis와 DB 값이 다르면 Redis를 DB 값으로 갱신
                if (!redisLikeCount.equals(dbLikeCount)) {
                    log.warn("Like count mismatch - postId: {}, DB: {}, Redis: {}", postId, dbLikeCount, redisLikeCount);
                    redisRequest = RedisRequest.builder()
                            .id(postId)
                            .views(redisRequest.views()) // 기존 조회수 유지
                            .likeCount(dbLikeCount)// DB 값으로 갱신
                            .build();

                    String updatedRedisValue = objectMapper.writeValueAsString(redisRequest);
                    redisTemplate.opsForValue().set(key, updatedRedisValue, 6000, TimeUnit.SECONDS);
                    updatedCount++;
                }
            } catch (JsonProcessingException e) {
                log.error("Redis parsing error, key: {}", key, e);
            }
        }

        // 5. 결과 로깅
        if (updatedCount > 0) {
            log.info("Successfully synchronized like counts for {} posts.", updatedCount);
        } else {
            log.info("No like count updates needed.");
        }

        log.info("Like count synchronization completed at {}.", LocalDateTime.now());
    }
}