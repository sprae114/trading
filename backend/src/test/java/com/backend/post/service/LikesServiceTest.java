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
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikesServiceTest {

    @Mock
    private LikesRepository likesRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private PostRepository postRepository;

    @Mock
    Authentication authentication;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private LikesService likesService;


    private Long defaultPostId;
    private Long defaultCustomerId;
    private Likes defaultLike;
    private Post defaultPost;

    private static final Pageable DEFAULT_PAGEABLE = Pageable.unpaged();
    private static final String CACHE_KEY_PREFIX = "post:";

    @BeforeEach
    void setUp() {
        defaultPostId = 1L;
        defaultCustomerId = 1L;

        defaultLike = Likes.builder()
                .id(1L)
                .postId(defaultPostId)
                .customerId(defaultCustomerId)
                .build();

        defaultPost = Post.builder()
                .id(defaultPostId)
                .views(0L)
                .build();
    }

    @Test
    @DisplayName("좋아요 생성 : 성공")
    void create_Success() throws JsonProcessingException {
        // given
        when(likesRepository.findByPostIdAndCustomerId(defaultPostId, defaultCustomerId)).thenReturn(Optional.empty());
        when(likesRepository.save(any(Likes.class))).thenReturn(defaultLike);
        when(postRepository.findById(defaultPostId)).thenReturn(Optional.of(defaultPost));
        when(likesRepository.countByPostId(defaultPostId)).thenReturn(1L);
        when(redisService.get(anyString())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson"); // objectMapper 모킹

        // when
        LikeResponseDto response = likesService.create(defaultPostId, defaultCustomerId);

        // then
        assertNotNull(response);
        assertEquals(defaultPostId, response.postId());
        assertEquals(defaultCustomerId, response.customerId());
        verify(likesRepository).save(any(Likes.class));
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }


    @Test
    @DisplayName("좋아요 생성 : 실패(이미 좋아요한 경우)")
    void create_AlreadyLiked_ThrowsException() {
        // given
        when(likesRepository.findByPostIdAndCustomerId(defaultPostId, defaultCustomerId)).thenReturn(Optional.of(defaultLike));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.create(defaultPostId, defaultCustomerId));
        assertEquals(ErrorCode.ALREADY_LIKED_POST, exception.getErrorCode());
        verify(likesRepository, never()).save(any(Likes.class));
    }


    @Test
    @DisplayName("좋아요 조회 : 성공")
    void getOne_Success() {
        // given
        when(likesRepository.findByPostIdAndCustomerId(defaultPostId, defaultCustomerId)).thenReturn(Optional.of(defaultLike));

        // when
        LikeResponseDto response = likesService.getOne(defaultPostId, defaultCustomerId);

        // then
        assertNotNull(response);
        assertEquals(defaultPostId, response.postId());
        assertEquals(defaultCustomerId, response.customerId());
    }

    @Test
    @DisplayName("좋아요 조회 : 실패(존재하지 않는 좋아요)")
    void getOne_NotFound_ThrowsException() {
        // given
        when(likesRepository.findByPostIdAndCustomerId(defaultPostId, defaultCustomerId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.getOne(defaultPostId, defaultCustomerId));
        assertEquals(ErrorCode.LIKES_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("DB에 좋아요 수 조회 : 성공")
    void countLikes_Success() {
        // given
        when(likesRepository.countByPostId(defaultPostId)).thenReturn(5L);

        // when
        Long count = likesService.countLikes(defaultPostId);

        // then
        assertEquals(5L, count);
    }

    @Test
    @DisplayName("좋아요 목록 조회 : 성공")
    void getList_Success() {
        // given
        Pageable pageable = Pageable.unpaged();
        Page<Likes> likesPage = new PageImpl<>(List.of(defaultLike));
        when(likesRepository.findAllByCustomerId(defaultCustomerId, pageable)).thenReturn(likesPage);

        // when
        Page<LikeResponseDto> result = likesService.getList(defaultCustomerId, pageable);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals(defaultCustomerId, result.getContent().get(0).customerId());
    }

    @Test
    @DisplayName("좋아요 목록 조회 : 실패(좋아요가 없는 경우)")
    void getList_EmptyList() {
        // given
        Pageable pageable = Pageable.unpaged();
        Page<Likes> emptyPage = new PageImpl<>(List.of());
        when(likesRepository.findAllByCustomerId(defaultCustomerId, pageable)).thenReturn(emptyPage);

        // when
        Page<LikeResponseDto> result = likesService.getList(defaultCustomerId, pageable);

        // then
        assertEquals(0, result.getTotalElements());
    }

    @Test
    @DisplayName("좋아요 삭제 : 성공")
    void deleteOne_Success() throws JsonProcessingException {
        // given
        when(likesRepository.findByPostIdAndCustomerId(defaultPostId, defaultCustomerId)).thenReturn(Optional.of(defaultLike));
        when(postRepository.findById(defaultPostId)).thenReturn(Optional.of(defaultPost));
        when(likesRepository.countByPostId(defaultPostId)).thenReturn(0L);
        when(redisService.get(anyString())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");

        // when
        likesService.deleteOne(defaultPostId, defaultCustomerId);

        // then
        verify(likesRepository).deleteByPostIdAndCustomerId(defaultPostId, defaultCustomerId);
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }

    @Test
    @DisplayName("좋아요 삭제 : 실패(좋아요가 없는 경우)")
    void deleteOne_NotFound_ThrowsException() {
        // given
        when(likesRepository.findByPostIdAndCustomerId(defaultPostId, defaultCustomerId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.deleteOne(defaultPostId, defaultCustomerId));
        assertEquals(ErrorCode.USER_NOT_MATCH, exception.getErrorCode());
        verify(likesRepository, never()).deleteByPostIdAndCustomerId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("모든 좋아요 삭제 : 성공(관리자)")
    void deleteList_Admin_Success() throws JsonProcessingException {
        // given
        when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        Page<Likes> likesPage = new PageImpl<>(List.of(defaultLike));
        when(likesRepository.findAllByCustomerId(defaultCustomerId, Pageable.unpaged())).thenReturn(likesPage);
        when(postRepository.findById(defaultPostId)).thenReturn(Optional.of(defaultPost));
        when(likesRepository.countByPostId(defaultPostId)).thenReturn(0L);
        when(redisService.get(anyString())).thenReturn(null);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");

        // when
        likesService.deleteList(defaultCustomerId, authentication);

        // then
        verify(likesRepository).deleteAllByCustomerId(defaultCustomerId);
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }

    @Test
    @DisplayName("모든 좋아요 삭제 : 실패(관리자 권한 없음)")
    void deleteList_NotAdmin_ThrowsException() {
        // given
        String expectedUsername = "Test User";
        when(authentication.getName()).thenReturn(expectedUsername);
        when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.deleteList(defaultCustomerId, authentication));
        assertEquals(ErrorCode.USER_NOT_AUTHORIZED, exception.getErrorCode());
        verify(likesRepository, never()).deleteAllByCustomerId(anyLong());
    }

    @Test
    @DisplayName("모든 좋아요 삭제 : 실패(삭제할 좋아요 없음)")
    void deleteList_NoLikesToDelete() throws JsonProcessingException {
        // given
        when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        Page<Likes> emptyPage = new PageImpl<>(List.of());
        when(likesRepository.findAllByCustomerId(defaultCustomerId, Pageable.unpaged())).thenReturn(emptyPage);

        // when
        likesService.deleteList(defaultCustomerId, authentication);

        // then
        verify(likesRepository, never()).deleteAllByCustomerId(anyLong());
        verify(redisService, never()).setKeyWithExpiration(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Redis로 좋아요 수 조회 : 성공(캐시 없음)")
    void countLikesWithRedis_Success_NoCache() throws JsonProcessingException {
        // given
        String key = CACHE_KEY_PREFIX + defaultPostId;
        when(redisService.get(key)).thenReturn(null);
        when(postRepository.findById(defaultPostId)).thenReturn(Optional.of(defaultPost));
        when(likesRepository.countByPostId(defaultPostId)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");

        // when
        Long count = likesService.countLikesWithRedis(defaultPostId);

        // then
        assertEquals(5L, count);
        verify(redisService).setKeyWithExpiration(eq(key), anyString(), eq(6000L));
    }

    @Test
    @DisplayName("Redis로 좋아요 수 조회 : 성공(캐시 있음)")
    void countLikesWithRedis_Success_WithCache() throws JsonProcessingException {
        // given
        String key = CACHE_KEY_PREFIX + defaultPostId;
        RedisRequest cachedRequest = RedisRequest.builder().id(defaultPostId).views(0L).likeCount(10L).build();
        when(redisService.get(key)).thenReturn("cachedJson");
        when(objectMapper.readValue("cachedJson", RedisRequest.class)).thenReturn(cachedRequest);

        // when
        Long count = likesService.countLikesWithRedis(defaultPostId);

        // then
        assertEquals(10L, count);
        verify(redisService, never()).setKeyWithExpiration(anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Redis로 좋아요 수 조회 : 실패(게시글 없음)")
    void countLikesWithRedis_PostNotFound_ThrowsException() {
        // given
        String key = CACHE_KEY_PREFIX + defaultPostId;
        when(redisService.get(key)).thenReturn(null);
        when(postRepository.findById(defaultPostId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.countLikesWithRedis(defaultPostId));
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }
}