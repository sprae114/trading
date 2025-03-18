package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.model.RedisRequest;
import com.backend.common.service.RedisService;
import com.backend.common.service.S3Service;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private S3Service s3Service;

    @Mock
    private LikesService likesService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostService postService;

    private Post defaultPost;
    private RegisterPostRequestDto registerRequest;
    private UpdateRequestDto updateRequest;
    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        defaultPost = Post.builder()
                .id(1L)
                .title("Test Post")
                .body("Test Body")
                .customerId(1L)
                .customerName("Test User")
                .category(PostCategory.ELECTRONICS)
                .tradeStatus(TradeStatus.SALE)
                .views(0L)
                .imageUrls(List.of("image1.jpg"))
                .build();

        registerRequest = new RegisterPostRequestDto(
                "New Post",
                "New Body",
                1L,
                "Test User",
                PostCategory.ELECTRONICS,
                new MockMultipartFile[]{new MockMultipartFile("file", new byte[0])}
        );

        updateRequest = new UpdateRequestDto(
                1L,
                "Updated Title",
                "Updated Body",
                PostCategory.ELECTRONICS,
                new MockMultipartFile[]{new MockMultipartFile("file", new byte[0])}
        );

        defaultPageable = Pageable.unpaged();
    }

    // create 메서드 테스트
    @Test
    @DisplayName("게시글 생성 성공")
    void create_success() throws IOException {
        // Given
        when(s3Service.uploadFiles(any())).thenReturn(List.of("uploaded_image.jpg"));
        when(postRepository.save(any(Post.class))).thenReturn(defaultPost);

        // When
        Post createdPost = postService.create(registerRequest);

        // Then
        assertNotNull(createdPost);
        assertEquals(defaultPost.getId(), createdPost.getId());
        verify(s3Service).uploadFiles(any());
        verify(postRepository).save(any(Post.class));
    }

    // getOne 메서드 테스트
    @Test
    @DisplayName("게시글 조회 성공")
    void getOne_success() throws JsonProcessingException {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(likesService.countLikes(1L)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");
        when(likesService.countLikesWithRedis(1L)).thenReturn(5L);

        // When
        PostResponseDto response = postService.getOne(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.views()); // 조회수 1 증가
        assertEquals(5L, response.likeCount());
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }

    @Test
    @DisplayName("게시글을 찾을 수 없는 경우 예외 발생")
    void getOne_postNotFound() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService.getOne(1L));
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }

    // getList 메서드 테스트
    @Test
    @DisplayName("게시글 목록 조회 성공")
    void getList_success() throws JsonProcessingException {
        // Given
        Page<Post> postPage = new PageImpl<>(List.of(defaultPost));
        when(postRepository.findAll(defaultPageable)).thenReturn(postPage);
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(likesService.countLikesWithRedis(1L)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");

        // When
        Page<PostListResponseDto> result = postService.getList(defaultPageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).views()); // 조회수 1 증가
        assertEquals(5L, result.getContent().get(0).likesCount());
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }

    // update 메서드 테스트
    @Test
    @DisplayName("게시글 수정 성공 - 관리자")
    void update_success_admin() throws IOException {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(s3Service.uploadFiles(any())).thenReturn(List.of("new_image.jpg"));
        when(postRepository.save(any(Post.class))).thenReturn(defaultPost);

        // When
        postService.update(updateRequest, authentication);

        // Then
        verify(s3Service).deleteFiles(anyList());
        verify(s3Service).uploadFiles(any());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 수정 성공 - 소유자")
    void update_success_owner() throws IOException {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authentication.getName()).thenReturn("Test User");
        when(s3Service.uploadFiles(any())).thenReturn(List.of("new_image.jpg"));
        when(postRepository.save(any(Post.class))).thenReturn(defaultPost);

        // When
        postService.update(updateRequest, authentication);

        // Then
        verify(s3Service).deleteFiles(anyList());
        verify(s3Service).uploadFiles(any());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 권한 없음")
    void update_unauthorized() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authentication.getName()).thenReturn("Another User");

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService.update(updateRequest, authentication));
        assertEquals(ErrorCode.USER_NOT_AUTHORIZED, exception.getErrorCode());
    }

    // delete 메서드 테스트
    @Test
    @DisplayName("게시글 삭제 성공 - 관리자")
    void delete_success_admin() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // When
        postService.delete(1L, authentication);

        // Then
        verify(redisService).delete("post:1");
        verify(postRepository).deleteById(1L);
        verify(likesService).deleteAllByPostId(1L);
    }

    @Test
    @DisplayName("게시글 삭제 성공 - 소유자")
    void delete_success_owner() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authentication.getName()).thenReturn("Test User");

        // When
        postService.delete(1L, authentication);

        // Then
        verify(redisService).delete("post:1");
        verify(postRepository).deleteById(1L);
        verify(likesService).deleteAllByPostId(1L);
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    void delete_unauthorized() {
        // Given
        when(postRepository.findById(1L)).thenReturn(Optional.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authentication.getName()).thenReturn("Another User");

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService.delete(1L, authentication));
        assertEquals(ErrorCode.USER_NOT_AUTHORIZED, exception.getErrorCode());
    }

    // deleteList 메서드 테스트
    @Test
    @DisplayName("여러 게시글 삭제 성공 - 관리자")
    void deleteList_success_admin() {
        // Given
        List<Long> postIds = List.of(1L, 2L);
        Post post2 = Post.builder().id(2L).customerName("Another User").imageUrls(List.of("image2.jpg")).build();
        when(postRepository.findAllById(postIds)).thenReturn(List.of(defaultPost, post2));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        // When
        postService.deleteList(postIds, authentication);

        // Then
        verify(redisService, times(2)).delete(anyString());
        verify(s3Service).deleteFiles(anyList());
        verify(postRepository).deleteAllByIdIn(postIds);
        verify(likesService, times(2)).deleteAllByPostId(anyLong());
    }

    @Test
    @DisplayName("여러 게시글 삭제 성공 - 소유자")
    void deleteList_success_owner() {
        // Given
        List<Long> postIds = List.of(1L);
        when(postRepository.findAllById(postIds)).thenReturn(List.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authentication.getName()).thenReturn("Test User");

        // When
        postService.deleteList(postIds, authentication);

        // Then
        verify(redisService).delete("post:1");
        verify(s3Service).deleteFiles(anyList());
        verify(postRepository).deleteAllByIdIn(List.of(1L));
        verify(likesService).deleteAllByPostId(1L);
    }

    @Test
    @DisplayName("여러 게시글 삭제 - 권한 없는 게시글 제외")
    void deleteList_unauthorized() {
        // Given
        List<Long> postIds = List.of(1L);
        when(postRepository.findAllById(postIds)).thenReturn(List.of(defaultPost));
         when(authentication.getAuthorities())
                .thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authentication.getName()).thenReturn("Another User");

        // When
        postService.deleteList(postIds, authentication);

        // Then
        verify(redisService, never()).delete(anyString());
        verify(s3Service, never()).deleteFiles(anyList());
        verify(postRepository, never()).deleteAllByIdIn(anyList());
        verify(likesService, never()).deleteAllByPostId(anyLong());
    }

    @Test
    @DisplayName("빈 게시글 목록 삭제 시 아무 작업 안 함")
    void deleteList_emptyList() {
        // Given
        List<Long> postIds = List.of();

        // When
        postService.deleteList(postIds, authentication);

        // Then
        verify(postRepository, never()).findAllById(anyList());
        verify(redisService, never()).delete(anyString());
    }
}