package com.backend.post.service;

import java.util.List;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest
@Transactional
class PostServiceTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostService postService;


    private RegisterPostRequestDto registerRequest;
    private Post post;

    @Mock
    private Authentication authentication;

    private RegisterPostRequestDto request;


    @BeforeEach
    void setUp() {
        // 필요한 경우 초기화 로직 추가 (현재는 PostRepository 직접 사용하므로 필요 없음)
        postRepository.deleteAll();
        request = new RegisterPostRequestDto(
                "Title1",
                "Test Body",
                1L,
                "Test User",
                PostCategory.DAILY,
                List.of("url1", "url2")
        );

    }


    @Test
    @DisplayName("게시글 생성 : 성공")
    void createPostTest() {
        // When
        Post createdPost = postService.create(request);

        // Then
        assertNotNull(createdPost.getId());
        assertEquals(request.title(), createdPost.getTitle());
        assertEquals(TradeStatus.SALE, createdPost.getTradeStatus());
        assertTrue(postRepository.findById(createdPost.getId()).isPresent()); // DB에 저장되었는지 확인
    }


    @Test
    @DisplayName("상세 게시글 조회 : 성공")
    void getOnePostTest() {
        // Given
        Post savedPost = postRepository.save(RegisterPostRequestDto.toEntity(request));

        // When
        Post foundPost = postService.getOne(savedPost.getId());

        // Then
        assertNotNull(foundPost);
        assertEquals(savedPost.getId(), foundPost.getId());
        assertEquals(savedPost.getTitle(), foundPost.getTitle());
    }


    @Test
    @DisplayName("상세 게시글 조회 : 실패(존재하지 않는 ID)")
    void getOnePostNotFoundTest() {
        // Given
        Long nonExistingPostId = 999L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> postService.getOne(nonExistingPostId));
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("게시글 리스트 조회 : 성공")
    void getListTest() {
        // Given
        postService.create(request);
        RegisterPostRequestDto request2 = new RegisterPostRequestDto("Title2", "Body2", 2L, "User2", PostCategory.FOOD, List.of());
        postRepository.save(RegisterPostRequestDto.toEntity(request2));

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Post> postPage = postService.getList(pageable);

        // Then
        assertEquals(2, postPage.getTotalElements());
        assertEquals(1, postPage.getTotalPages());
        assertEquals("Title1", postPage.getContent().get(0).getTitle());
        assertEquals("Title2", postPage.getContent().get(1).getTitle());
    }


    @Test
    @DisplayName("게시글 수정 : 성공")
    void updatePostTest() throws Exception{
        // Given
        Post savedPost = postService.create(request);

        String expectedUsername = "Test User";
        when(authentication.getName()).thenReturn(expectedUsername);

        UpdateRequestDto updateRequest = new UpdateRequestDto(
                savedPost.getId(),
                "Updated Title",
                "Updated Body",
                PostCategory.FOOD,
                List.of("newUrl1", "newUrl2")
        );

        // When
        Post updatedPost = postService.update(updateRequest, authentication);

        // Then
        assertEquals(updateRequest.id(), updatedPost.getId());
        assertEquals(updateRequest.title(), updatedPost.getTitle());
        assertEquals(updateRequest.body(), updatedPost.getBody());
        assertEquals(updateRequest.category(), updatedPost.getCategory());
        assertEquals(updateRequest.imageUrls(), updatedPost.getImageUrls());
    }

    @Test
    @DisplayName("게시글 수정 : 실패(다른 아이디)")
    void updatePostNotMatchCustomer() throws Exception {
        // Given
        Post savedPost = postService.create(request);

        String expectedUsername = "Test!!!";
        when(authentication.getName()).thenReturn(expectedUsername);

        UpdateRequestDto updateRequest = new UpdateRequestDto(
                savedPost.getId(),
                "Updated Title",
                "Updated Body",
                PostCategory.FOOD,
                List.of("newUrl1", "newUrl2")
        );

        // When
        CustomException exception = assertThrows(CustomException.class, () -> postService.update(updateRequest, authentication));
        assertEquals(ErrorCode.USER_NOT_AUTHORIZED, exception.getErrorCode());
    }


    @Test
    @DisplayName("게시글 수정 : 실패(존재하지 않는 게시물)")
    void updatePostNotFoundTest() {
        // Given
        UpdateRequestDto updateRequestDto = new UpdateRequestDto(
                999L, // Non-existing ID
                "Updated Title",
                "Updated Body",
                PostCategory.DAILY,
                List.of("url2", "url3")
        );

        // When & Then
        CustomException exception = assertThrows(CustomException.class, ()-> postService.update(updateRequestDto, authentication));
        assertEquals(ErrorCode.POST_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("게시글 삭제 : 성공")
    void deletePostTest() {
        // Given
        Post savedPost = postService.create(request);
        String expectedUsername = "Test User";
        when(authentication.getName()).thenReturn(expectedUsername);

        // When
        postService.delete(savedPost.getId(), authentication);

        // Then
        assertFalse(postRepository.findById(savedPost.getId()).isPresent());
    }

    @Test
    @DisplayName("게시글 삭제 : 실패(다른 아이디)")
    void deleteNotMatchCustomer(){
        // Given
        Post savedPost = postService.create(request);
        String expectedUsername = "Test!!";
        when(authentication.getName()).thenReturn(expectedUsername);

        // When
        CustomException exception = assertThrows(CustomException.class, () -> postService.delete(savedPost.getId(), authentication));
        assertEquals(ErrorCode.USER_NOT_AUTHORIZED, exception.getErrorCode());
    }

    @Test
    @DisplayName("likes 이용한 게시글 조회 : 성공")
    void getListByLikes(){
        //given
        Post post1 = postService.create(request);
        RegisterPostRequestDto request2 = new RegisterPostRequestDto("Title2", "Body2", 1L, "User2", PostCategory.FOOD, List.of());
        Post post2 = postRepository.save(RegisterPostRequestDto.toEntity(request2));

        //when
        List<Post> result = postService.getPostsByIds(List.of(post1.getId(), post2.getId()));

        //then
        assertEquals(result.size(), 2);
    }
}