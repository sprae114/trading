package com.backend.post.controller;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.LikeResponseDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
import com.backend.post.model.entity.Likes;
import com.backend.post.model.entity.Post;
import com.backend.post.service.LikesService;
import com.backend.post.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private LikesService likesService;

    private Post post1;
    private RegisterPostRequestDto registerRequest;
    private UpdateRequestDto updateRequest;
    private SearchPostRequestDto searchRequest;

    private MockMultipartFile file1;
    private MockMultipartFile file2;
    private MockMultipartFile file3;

    private Pageable pageable;
    private Page<PostListResponseDto> mockPage;

    @BeforeEach
    void setUp() throws IOException {
        file1 = new MockMultipartFile("imageFiles", "test1.jpg", "image/jpeg", "test image".getBytes());
        file2 = new MockMultipartFile("imageFiles", "test2.jpg", "image/jpeg", "another test image".getBytes());
        file3 = new MockMultipartFile("imageFiles", "test3.jpg", "image/jpeg", "another test image3".getBytes());

        post1 = Post.builder()
                .id(1L)
                .title("제목1")
                .body("text")
                .customerId(1L)
                .customerName("test@example.com")
                .category(PostCategory.ELECTRONICS)
                .tradeStatus(TradeStatus.SALE)
                .views(0L)
                .imageUrls(List.of("test1.jpg"))
                .build();

        // 등록 Dto
        registerRequest = RegisterPostRequestDto.builder()
                .title("Test Title")
                .body("Test Body")
                .customerId(1L)
                .customerName("test@example.com")
                .category(PostCategory.ELECTRONICS)
                .build();

        // 수정 Dto
        updateRequest = UpdateRequestDto.builder()
                .id(1L)
                .title("Updated Title")
                .body("Updated Body")
                .category(PostCategory.ELECTRONICS)
                .build();

        // 검색 DTO
        searchRequest = new SearchPostRequestDto("제목", PostCategory.ELECTRONICS);

        // Pageable 초기화
        pageable = PageRequest.of(0, 5, Sort.by("id").ascending());

        // Mock 페이지 설정
        PostListResponseDto postListResponse = PostListResponseDto.from(post1, 0L, 0L, List.of("test1.jpg".getBytes()));
        mockPage = new PageImpl<>(List.of(postListResponse), pageable, 1);
    }

    @Test
    @DisplayName("게시글 생성 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void createPost() throws Exception {
        when(postService.create(any(RegisterPostRequestDto.class))).thenReturn(post1);

        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", "", "application/json",
                objectMapper.writeValueAsBytes(registerRequest));

        mockMvc.perform(multipart("/api/post")
                        .file(jsonPart)
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 생성 : 실패 (제목 없음)")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void createPostFail() throws Exception {
        RegisterPostRequestDto noTitleRequestDto = RegisterPostRequestDto.builder()
                .body("text")
                .customerId(1L)
                .customerName("test@example.com")
                .category(PostCategory.ELECTRONICS)
                .build();

        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", "", "application/json",
                objectMapper.writeValueAsBytes(noTitleRequestDto));

        mockMvc.perform(multipart("/api/post")
                        .file(jsonPart)
                        .file(file1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 목록 조회 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void getPostListTest() throws Exception {
        when(postService.getList(pageable)).thenReturn(mockPage);

        mockMvc.perform(get("/api/post")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("제목1"));
    }

    @Test
    @DisplayName("게시글 목록 조회 : 실패(로그인 안함)")
    void getPostListNotUser() throws Exception {
        mockMvc.perform(get("/api/post")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("게시글 단건 조회 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void getPostOneTest() throws Exception {
        PostResponseDto responseDto = PostResponseDto.from(post1, 1L, 0L, List.of("test1.jpg".getBytes()));
        when(postService.getOne(1L)).thenReturn(responseDto);
        when(likesService.isLiked(eq(1L), any(Authentication.class))).thenReturn(false); // 올바름

        mockMvc.perform(get("/api/post/{postId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("제목1"))
                .andExpect(jsonPath("$.isLiked").value(false));
    }

    @Test
    @DisplayName("게시글 단건 조회 : 실패 (없는 게시글)")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void getPostOneFail() throws Exception {
        when(postService.getOne(eq(9999L))).thenThrow(new CustomException(ErrorCode.POST_NOT_FOUND, "9999"));

        mockMvc.perform(get("/api/post/{postId}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("게시글 수정 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void updatePostTest() throws Exception {
        doNothing().when(postService).update(any(UpdateRequestDto.class), any(Authentication.class));

        mockMvc.perform(put("/api/post/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 : 성공(관리자)")
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updatePostAdminTest() throws Exception {
        doNothing().when(postService).update(any(UpdateRequestDto.class), any(Authentication.class));

        mockMvc.perform(put("/api/post/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 : 실패 (권한 없음)")
    @WithMockUser(username = "another@example.com", roles = "CUSTOMER")
    void updatePostFail() throws Exception {
        doThrow(new CustomException(ErrorCode.USER_NOT_AUTHORIZED, "Unauthorized"))
                .when(postService).update(any(UpdateRequestDto.class), any(Authentication.class));

        mockMvc.perform(put("/api/post/{postId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 삭제 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void deletePostTest() throws Exception {
        doNothing().when(postService).delete(eq(1L), any(Authentication.class));

        mockMvc.perform(delete("/api/post")
                        .param("postId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 : 성공(관리자)")
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void deletePostAdminTest() throws Exception {
        doNothing().when(postService).delete(eq(1L), any(Authentication.class));

        mockMvc.perform(delete("/api/post")
                        .param("postId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 : 실패 (권한 없음)")
    @WithMockUser(username = "another@example.com", roles = "CUSTOMER")
    void deletePostFail() throws Exception {
        doThrow(new CustomException(ErrorCode.USER_NOT_AUTHORIZED, "Unauthorized"))
                .when(postService).delete(eq(1L), any(Authentication.class));

        mockMvc.perform(delete("/api/post")
                        .param("postId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("좋아요 추가 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void saveLikeTest() throws Exception {
        LikeResponseDto mockResponse = new LikeResponseDto(1L, 1L); // 실제 DTO에 맞게 생성
        when(likesService.create(eq(1L), eq(1L))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/post/{postId}/like", 1L)
                        .param("customerId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 및 제목 검색 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void searchPostsSuccess() throws Exception {
        when(postService.searchByTitleAndCategory(searchRequest, pageable)).thenReturn(mockPage);

        mockMvc.perform(post("/api/post/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest))
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("제목1"));
    }

    @Test
    @DisplayName("카테고리 및 제목 검색 : 실패 (인증 없음)")
    void searchPostsFailNoAuth() throws Exception {
        mockMvc.perform(post("/api/post/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchRequest))
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "id,asc"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("좋아요 삭제 : 성공")
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void deleteLikeTest() throws Exception {
        doNothing().when(likesService).deleteOne(1L, 1L);

        mockMvc.perform(delete("/api/post/{postId}/like", 1L)
                        .param("customerId", "1"))
                .andExpect(status().isOk());
    }
}