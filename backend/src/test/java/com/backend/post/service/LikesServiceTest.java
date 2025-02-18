package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.post.model.entity.Likes;
import com.backend.post.repository.LikesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LikesServiceTest {

    @Autowired
    private LikesService likesService;

    @Autowired
    private LikesRepository likesRepository;

    @BeforeEach
    void setUp() {
        likesRepository.deleteAll();
    }

    @Test
    @DisplayName("좋아요 생성 : 성공")
    void createLikesSuccess() {
        // Given
        Long postId = 1L;
        Long customerId = 1L;

        // When
        Likes likes = likesService.create(postId, customerId);

        // Then
        assertNotNull(likes);
        assertNotNull(likes.getId()); // ID가 자동 생성되었는지 확인
        assertEquals(postId, likes.getPostId());
        assertEquals(customerId, likes.getCustomerId());
        assertTrue(likesRepository.findById(likes.getId()).isPresent()); // DB에 저장되었는지 확인
    }


    @Test
    @DisplayName("좋아요 생성 : 실패 (이미 좋아요한 게시물)")
    void createLikesFailureAlreadyLiked() {
        // Given
        Long postId = 1L;
        Long customerId = 1L;
        likesService.create(postId, customerId); // 이미 좋아요 생성

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.create(postId, customerId));
        assertEquals(ErrorCode.ALREADY_LIKED_POST, exception.getErrorCode());
    }


    @Test
    @DisplayName("좋아요 조회 성공")
    void getOneLikesSuccess() {
        // Given
        Long postId = 1L;
        Long customerId = 1L;
        Likes savedLikes = likesService.create(postId, customerId); // 좋아요 생성

        // When
        Likes foundLikes = likesService.getOne(postId, customerId);

        // Then
        assertNotNull(foundLikes);
        assertEquals(savedLikes.getId(), foundLikes.getId());
        assertEquals(postId, foundLikes.getPostId());
        assertEquals(customerId, foundLikes.getCustomerId());
    }


    @Test
    @DisplayName("좋아요 조회 실패 - 좋아요하지 않은 게시물")
    void getOneLikesFailureNotFound() {
        // Given
        Long postId = 1L;
        Long customerId = 1L;
        // 좋아요를 생성하지 않음

        // When & Then
        CustomException exception = assertThrows(CustomException.class, () -> likesService.getOne(postId, customerId));
        assertEquals(ErrorCode.LIKES_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("게시글 ID로 좋아요 삭제 : 성공")
    void deleteOneLikesSuccess() {
        // Given
        Long postId = 1L;
        Long customerId = 1L;
        likesService.create(postId, customerId); // 좋아요 생성

        // When
        likesService.deleteOne(postId, customerId);

        // Then
        assertFalse(likesRepository.findByPostIdAndCustomerId(postId, customerId).isPresent()); // 좋아요가 삭제되었는지 확인
    }


    @Test
    @DisplayName("게시글 ID로 좋아요 삭제 : 실패(다른 Id)")
    void deleteOneNotMatchUser() {
        //Given
        Long postId = 1L;
        Long customerId = 1L;
        likesService.create(postId, customerId);

        // When
        CustomException exception = assertThrows(CustomException.class, () -> likesService.deleteOne(postId, 999L));
        assertEquals(exception.getErrorCode(), ErrorCode.USER_NOT_MATCH);
    }


    @Test
    @DisplayName("사용자 ID로 좋아요 목록 삭제 : 성공")
    void deleteListLikesSuccess() {
        // Given
        Long postId1 = 1L;
        Long postId2 = 2L;
        Long customerId = 1L;
        likesService.create(postId1, customerId); // 좋아요 생성
        likesService.create(postId2, customerId); // 좋아요 생성

        // When
        likesService.deleteList(customerId);

        // Then
        assertFalse(likesRepository.findByPostIdAndCustomerId(postId1, customerId).isPresent());
        assertFalse(likesRepository.findByPostIdAndCustomerId(postId2, customerId).isPresent());
    }


    @Test
    @DisplayName("사용자 ID로 좋아요 목록 삭제 - 좋아요 없음")
    void deleteListLikesNotFound() {
        //Given
        Long customerId = 1L;

        // When: 존재하지 않는 좋아요 삭제 시도
        likesService.deleteList(customerId);

        // Then: 아무일도 일어나지 않음 (예외 발생 X)
    }
}