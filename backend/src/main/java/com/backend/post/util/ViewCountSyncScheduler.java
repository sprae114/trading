package com.backend.post.util;

import com.backend.common.model.RedisRequest;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
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
import java.util.stream.Collectors;

/**
 * 스케쥴링을 통해
 * redis -> DB로 조호수 반영
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountSyncScheduler {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "post:";


    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void syncViewCounts() throws JsonProcessingException {

        // 1. redisTemplate에서 "post:"로 시작하는 모든 키 가져오기
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            log.info("Redis에 post 데이터가 없습니다.");
            return;
        }

        // 2. Redis에서 가져온 Post 데이터 맵으로 변환
        Map<Long, RedisRequest> redisPostMap = keys.stream()
                .map(key -> {
                    Object value = redisTemplate.opsForValue().get(key);
                    try {
                        return objectMapper.readValue(value.toString(), RedisRequest.class);
                    } catch (JsonProcessingException e) {
                        log.error("레디스 파싱 에러, key: {}", key, e);
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
            log.info("{}개의 게시물 조회수를 성공적으로 동기화했습니다.", postsToUpdate.size());
        } else {
            log.info("조회수 업데이트가 필요 없습니다.");
        }

        // 6. 스케줄링 완료 로그
        log.info("조회수 동기화 스케줄링이 {}에 완료되었습니다.", LocalDateTime.now());
    }
}