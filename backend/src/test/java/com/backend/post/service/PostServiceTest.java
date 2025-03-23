package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.model.RedisRequest;
import com.backend.common.service.RedisService;
import com.backend.common.service.S3Service;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
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
import org.springframework.web.multipart.MultipartFile;

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
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        MockMultipartFile file1 = new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test image".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "another test image".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "another test image3".getBytes());

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

        // 등록 Dto
        RegisterPostRequestDto registerDto = RegisterPostRequestDto.builder()
                .title("New Post")
                .body("New Body")
                .customerId(1L)
                .customerName("New User")
                .category(PostCategory.ELECTRONICS)
                .build();

        MultipartFile[] registerFiles = new MultipartFile[]{file1, file2};

        registerRequest = RegisterPostRequestDto.from(registerDto, registerFiles);


        // 수정 Dto
        UpdateRequestDto updateDto = UpdateRequestDto.builder()
                .id(1L)
                .title("Updated Title")
                .body("Updated Body")
                .tradeStatus(TradeStatus.HIDDEN)
                .category(PostCategory.ELECTRONICS)
                .build();

        MultipartFile[] updateFiles = new MultipartFile[]{file1, file3};

        updateRequest = UpdateRequestDto.from(updateDto, updateFiles);

        pageable = Pageable.unpaged();
    }

    // create 메서드 테스트
    @Test
    @DisplayName("게시글 생성 : 성공")
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
        when(postRepository.findAll(pageable)).thenReturn(postPage);
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(likesService.countLikesWithRedis(1L)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");

        // When
        Page<PostListResponseDto> result = postService.getList(pageable);

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

    // 성공 케이스
    @Test
    @DisplayName("카테고리로만 검색 : 성공")
    void searchByCategory_success() throws JsonProcessingException {
        // Given
        SearchPostRequestDto request = new SearchPostRequestDto("", PostCategory.ELECTRONICS);
        Page<Post> postPage = new PageImpl<>(List.of(defaultPost), pageable, 1);
        when(postRepository.searchByCategory(PostCategory.ELECTRONICS, pageable)).thenReturn(postPage);
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(likesService.countLikes(1L)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");
        when(likesService.countLikesWithRedis(1L)).thenReturn(5L);

        // When
        Page<PostListResponseDto> result = postService.searchByTitleAndCategory(request, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1L, result.getContent().get(0).views()); // 조회수 증가
        assertEquals(5L, result.getContent().get(0).likesCount());
        verify(postRepository).searchByCategory(PostCategory.ELECTRONICS, pageable);
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }


    @Test
    @DisplayName("제목만 검색 : 성공")
    void searchByTitle_success() throws JsonProcessingException {
        // Given
        SearchPostRequestDto request = new SearchPostRequestDto("Test", PostCategory.ALL);
        Page<Post> postPage = new PageImpl<>(List.of(defaultPost), pageable, 1);
        when(postRepository.searchByTitle("Test", pageable)).thenReturn(postPage);
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(likesService.countLikes(1L)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");
        when(likesService.countLikesWithRedis(1L)).thenReturn(5L);

        // When
        Page<PostListResponseDto> result = postService.searchByTitleAndCategory(request, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Post", result.getContent().get(0).title());
        verify(postRepository).searchByTitle("Test", pageable);
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }


    @Test
    @DisplayName("제목과 카테고리로 검색 : 성공")
    void searchByTitleAndCategory_success() throws JsonProcessingException {
        // Given
        SearchPostRequestDto request = new SearchPostRequestDto("Test", PostCategory.ELECTRONICS);
        Page<Post> postPage = new PageImpl<>(List.of(defaultPost), pageable, 1);
        when(postRepository.searchByTitleAndCategory("Test", PostCategory.ELECTRONICS, pageable)).thenReturn(postPage);
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(likesService.countLikes(1L)).thenReturn(5L);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenReturn("mockedJson");
        when(likesService.countLikesWithRedis(1L)).thenReturn(5L);

        // When
        Page<PostListResponseDto> result = postService.searchByTitleAndCategory(request, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Post", result.getContent().get(0).title());
        assertEquals(PostCategory.ELECTRONICS, result.getContent().get(0).category());
        verify(postRepository).searchByTitleAndCategory("Test", PostCategory.ELECTRONICS, pageable);
        verify(redisService).setKeyWithExpiration(eq("post:1"), anyString(), eq(6000L));
    }


    @Test
    @DisplayName("제목 + 카테고리 검색 : 성공(검색 결과가 없는 경우 빈 페이지 반환)")
    void searchByTitleAndCategory_noResults() {
        // Given
        SearchPostRequestDto request = new SearchPostRequestDto("Nonexistent", PostCategory.ELECTRONICS);
        Page<Post> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(postRepository.searchByTitleAndCategory("Nonexistent", PostCategory.ELECTRONICS, pageable)).thenReturn(emptyPage);

        // When
        Page<PostListResponseDto> result = postService.searchByTitleAndCategory(request, pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(postRepository).searchByTitleAndCategory("Nonexistent", PostCategory.ELECTRONICS, pageable);
        verify(s3Service, never()).downloadFiles(anyList());
    }


    @Test
    @DisplayName("제목 + 카테고리 검색 : 실패(JSON 처리 오류 발생)")
    void searchByTitleAndCategory_jsonProcessingError() throws JsonProcessingException {
        // Given
        SearchPostRequestDto request = new SearchPostRequestDto("Test", PostCategory.ELECTRONICS);
        Page<Post> postPage = new PageImpl<>(List.of(defaultPost), pageable, 1);
        when(postRepository.searchByTitleAndCategory("Test", PostCategory.ELECTRONICS, pageable)).thenReturn(postPage);
        when(s3Service.downloadFiles(anyList())).thenReturn(List.of(new byte[0]));
        when(redisService.get("post:1")).thenReturn(null);
        when(objectMapper.writeValueAsString(any(RedisRequest.class))).thenThrow(new JsonProcessingException("JSON error") {});

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.searchByTitleAndCategory(request, pageable));
        assertEquals(ErrorCode.JSON_PASSING_ERROR, exception.getErrorCode());
        verify(postRepository).searchByTitleAndCategory("Test", PostCategory.ELECTRONICS, pageable);
    }
}