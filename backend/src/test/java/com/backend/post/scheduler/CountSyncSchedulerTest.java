package com.backend.post.scheduler;

import com.backend.common.model.RedisRequest;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.post.service.LikesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountSyncSchedulerTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikesService likesService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CountSyncScheduler countSyncScheduler;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Redis와 DB의 조회수를 성공적으로 동기화")
    void syncViewCounts_success() throws JsonProcessingException {
        // Given
        Set<String> redisKeys = Set.of("post:1", "post:2");
        when(redisTemplate.keys("post:*")).thenReturn(redisKeys);

        RedisRequest redisRequest1 = new RedisRequest(1L, 50L, 10L);
        RedisRequest redisRequest2 = new RedisRequest(2L, 75L, 10L);
        when(valueOperations.get("post:1")).thenReturn("{\"id\":1,\"views\":50}");
        when(valueOperations.get("post:2")).thenReturn("{\"id\":2,\"views\":75}");
        when(objectMapper.readValue("{\"id\":1,\"views\":50}", RedisRequest.class)).thenReturn(redisRequest1);
        when(objectMapper.readValue("{\"id\":2,\"views\":75}", RedisRequest.class)).thenReturn(redisRequest2);

        Post dbPost1 = Post.builder().id(1L).views(10L).build();
        Post dbPost2 = Post.builder().id(2L).views(20L).build();
        when(postRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(dbPost1, dbPost2));

        Post updatedPost1 = dbPost1.toBuilder().views(50L).build();
        Post updatedPost2 = dbPost2.toBuilder().views(75L).build();
        when(postRepository.saveAll(List.of(updatedPost1, updatedPost2)))
                .thenReturn(List.of(updatedPost1, updatedPost2));

        // When
        countSyncScheduler.syncViewCounts();

        // Then
        verify(postRepository, times(1)).saveAll(anyList());
        verify(redisTemplate, times(1)).keys("post:*");
        verify(valueOperations, times(2)).get(anyString());
        verify(objectMapper, times(2)).readValue(anyString(), eq(RedisRequest.class));
    }

    @Test
    @DisplayName("Redis 데이터 파싱 에러 발생 시 동기화 진행")
    void syncViewCounts_jsonParsingError() throws JsonProcessingException {
        // Given
        Set<String> redisKeys = Set.of("post:1");
        when(redisTemplate.keys("post:*")).thenReturn(redisKeys);
        when(valueOperations.get("post:1")).thenReturn("invalid_json");
        when(objectMapper.readValue("invalid_json", RedisRequest.class))
                .thenThrow(new JsonProcessingException("Parsing error") {});

        // When
        countSyncScheduler.syncViewCounts();

        // Then
        verify(redisTemplate, times(1)).keys("post:*");
        verify(valueOperations, times(1)).get("post:1");
        verify(objectMapper, times(1)).readValue("invalid_json", RedisRequest.class);
    }

    @Test
    @DisplayName("DB와 Redis 조회수가 동일할 때 업데이트 없음")
    void syncViewCounts_noUpdatesNeeded() throws JsonProcessingException {
        // Given
        Set<String> redisKeys = Set.of("post:1");
        when(redisTemplate.keys("post:*")).thenReturn(redisKeys);

        RedisRequest redisRequest = new RedisRequest(1L, 50L, 10L);
        when(valueOperations.get("post:1")).thenReturn("{\"id\":1,\"views\":50}");
        when(objectMapper.readValue("{\"id\":1,\"views\":50}", RedisRequest.class)).thenReturn(redisRequest);

        Post dbPost = Post.builder().id(1L).views(50L).build();
        when(postRepository.findAllById(List.of(1L))).thenReturn(List.of(dbPost));

        // When
        countSyncScheduler.syncViewCounts();

        // Then
        verify(postRepository, times(1)).saveAll(anyList());
        verify(redisTemplate, times(1)).keys("post:*");
        verify(valueOperations, times(1)).get("post:1");
        verify(objectMapper, times(1)).readValue(anyString(), eq(RedisRequest.class));
    }

    @Test
    @DisplayName("Redis와 DB의 좋아요 수를 성공적으로 동기화")
    void syncLikeCount_success() throws JsonProcessingException {
        // Given
        Set<String> redisKeys = Set.of("post:1", "post:2");
        when(redisTemplate.keys("post:*")).thenReturn(redisKeys);

        RedisRequest redisRequest1 = new RedisRequest(1L, 50L, 5L); // Redis: 5 likes
        RedisRequest redisRequest2 = new RedisRequest(2L, 75L, 8L); // Redis: 8 likes
        when(valueOperations.get("post:1")).thenReturn("{\"id\":1,\"views\":50,\"likeCount\":5}");
        when(valueOperations.get("post:2")).thenReturn("{\"id\":2,\"views\":75,\"likeCount\":8}");
        when(objectMapper.readValue("{\"id\":1,\"views\":50,\"likeCount\":5}", RedisRequest.class)).thenReturn(redisRequest1);
        when(objectMapper.readValue("{\"id\":2,\"views\":75,\"likeCount\":8}", RedisRequest.class)).thenReturn(redisRequest2);

        when(likesService.countLikes(1L)).thenReturn(10L); // DB: 10 likes
        when(likesService.countLikes(2L)).thenReturn(3L);  // DB: 3 likes

        RedisRequest updatedRequest1 = new RedisRequest(1L, 50L, 10L);
        RedisRequest updatedRequest2 = new RedisRequest(2L, 75L, 3L);
        when(objectMapper.writeValueAsString(updatedRequest1)).thenReturn("{\"id\":1,\"views\":50,\"likeCount\":10}");
        when(objectMapper.writeValueAsString(updatedRequest2)).thenReturn("{\"id\":2,\"views\":75,\"likeCount\":3}");

        // When
        countSyncScheduler.syncLikeCount();

        // Then
        verify(redisTemplate, times(1)).keys("post:*");
        verify(valueOperations, times(2)).get(anyString());
        verify(likesService, times(1)).countLikes(1L);
        verify(likesService, times(1)).countLikes(2L);
        verify(objectMapper, times(2)).readValue(anyString(), eq(RedisRequest.class));
        verify(objectMapper, times(2)).writeValueAsString(any(RedisRequest.class));
        verify(valueOperations, times(1)).set(eq("post:1"), eq("{\"id\":1,\"views\":50,\"likeCount\":10}"), eq(6000L), eq(TimeUnit.SECONDS));
        verify(valueOperations, times(1)).set(eq("post:2"), eq("{\"id\":2,\"views\":75,\"likeCount\":3}"), eq(6000L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Redis 데이터 파싱 에러 발생 시 동기화 진행")
    void syncLikeCount_jsonParsingError() throws JsonProcessingException {
        // Given
        Set<String> redisKeys = Set.of("post:1");
        when(redisTemplate.keys("post:*")).thenReturn(redisKeys);
        when(valueOperations.get("post:1")).thenReturn("invalid_json");
        when(objectMapper.readValue("invalid_json", RedisRequest.class))
                .thenThrow(new JsonProcessingException("Parsing error") {});

        // When
        countSyncScheduler.syncLikeCount();

        // Then
        verify(redisTemplate, times(1)).keys("post:*");
        verify(valueOperations, times(1)).get("post:1");
        verify(objectMapper, times(1)).readValue("invalid_json", RedisRequest.class);
        verify(likesService, never()).countLikes(anyLong());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("DB와 Redis 좋아요 수가 동일할 때 업데이트 없음")
    void syncLikeCount_noUpdatesNeeded() throws JsonProcessingException {
        // Given
        Set<String> redisKeys = Set.of("post:1");
        when(redisTemplate.keys("post:*")).thenReturn(redisKeys);

        RedisRequest redisRequest = new RedisRequest(1L, 50L, 10L);
        when(valueOperations.get("post:1")).thenReturn("{\"id\":1,\"views\":50,\"likeCount\":10}");
        when(objectMapper.readValue("{\"id\":1,\"views\":50,\"likeCount\":10}", RedisRequest.class)).thenReturn(redisRequest);

        when(likesService.countLikes(1L)).thenReturn(10L); // DB와 Redis 동일

        // When
        countSyncScheduler.syncLikeCount();

        // Then
        verify(redisTemplate, times(1)).keys("post:*");
        verify(valueOperations, times(1)).get("post:1");
        verify(likesService, times(1)).countLikes(1L);
        verify(objectMapper, times(1)).readValue(anyString(), eq(RedisRequest.class));
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }
}