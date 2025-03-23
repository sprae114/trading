package com.backend.post.controller;

import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.model.PostCategory;
import com.backend.post.model.TradeStatus;
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
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 주입
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    @Autowired
    private LikesService likesService;


    private Post post1;
    private Post post2;
    private PostListResponseDto postListResponse;
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
        file1 = new MockMultipartFile("file1", "test1.jpg", "image/jpeg", "test image".getBytes());
        file2 = new MockMultipartFile("file2", "test2.jpg", "image/jpeg", "another test image".getBytes());
        file3 = new MockMultipartFile("file3", "test3.jpg", "image/jpeg", "another test image3".getBytes());


        // 데이터 2개 넣기
        post1 = postService.create(makePostRequestDto("제목1", new MultipartFile[] {file1, file2}));
        post2 = postService.create(makePostRequestDto("제목2", new MultipartFile[] {file2, file3}));

        // 등록 Dto
        registerRequest = RegisterPostRequestDto.builder()
                .title("Test Title")
                .body("Test Body")
                .customerId(1L)
                .customerName("Test User")
                .category(PostCategory.ELECTRONICS)
                .build();

        // 수정 Dto
        updateRequest = UpdateRequestDto.builder()
                .id(post1.getId())
                .title("Updated Title")
                .body("Updated Body")
                .tradeStatus(TradeStatus.HIDDEN)
                .category(PostCategory.ELECTRONICS)
                .build();

        // 검색 DTO
        searchRequest = SearchPostRequestDto.builder()
                .title("제목")
                .postCategory(PostCategory.ELECTRONICS)
                .build();

        // Pageable 초기화
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("게시글 생성 : 성공")
    @WithMockUser
    void createPost() throws Exception {
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
    @WithMockUser
    void createPostFail() throws Exception {
        // 제목이 없는 DTO
        RegisterPostRequestDto noTitleRequestDto = RegisterPostRequestDto.builder()
                .body("text")
                .customerId(1L)
                .category(PostCategory.BOOKS)
                .customerName("김")
                .build();

        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", "", "application/json",
                objectMapper.writeValueAsBytes(noTitleRequestDto));


        mockMvc.perform(multipart("/api/post")
                        .file(jsonPart)
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("게시글 목록 조회 : 성공")
    @WithMockUser
    void getPostListTest() throws Exception {
        // when, then
        mockMvc.perform(get("/api/post")
                        .content(String.valueOf(PageRequest.of(0, 10)))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("제목1"))
                .andExpect(jsonPath("$.content[1].title").value("제목2"));
    }

    @Test
    @DisplayName("게시글 목록 조회 : 실패(로그인 안함)")
    void getPostListNotUser() throws Exception {

        mockMvc.perform(get("/api/post")
                .content(String.valueOf(PageRequest.of(0, 10)))
        )
        .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("게시글 단건 조회 : 성공")
    @WithMockUser
    void getPostOneTest() throws Exception {

        mockMvc.perform(get("/api/post/{postId}", post1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("제목1"));
    }

    @Test
    @DisplayName("게시글 단건 조회 : 실패 (없는 게시글)")
    @WithMockUser
    void getPostOneFail() throws Exception {

        mockMvc.perform(get("/api/post/{postId}", 9999L)) // 존재하지 않는 ID
                .andExpect(status().isNotFound()); // 404 Not Found 예상
    }

    @Test
    @DisplayName("게시글 수정 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void updatePostTest() throws Exception {
        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", "", "application/json",
                objectMapper.writeValueAsBytes(updateRequest));

        mockMvc.perform(multipart("/api/post/{postId}", post1.getId())
                        .file(jsonPart)
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT"); // PUT 메서드로 설정
                            return request;
                        })
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 : 성공(관리자)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updatePostAdminTest() throws Exception {
        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", "", "application/json",
                objectMapper.writeValueAsBytes(updateRequest));

        mockMvc.perform(multipart("/api/post/{postId}", post1.getId())
                        .file(jsonPart)
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT"); // PUT 메서드로 설정
                            return request;
                        })
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 : 실패 (권한 없음)")
    @WithMockUser(username = "권한없음", roles = "CUSTOMER") // 다른 사용자 이름 지정
    void updatePostFail() throws Exception {
        MockMultipartFile jsonPart = new MockMultipartFile(
                "requestDto", "", "application/json",
                objectMapper.writeValueAsBytes(updateRequest));

        mockMvc.perform(multipart("/api/post/{postId}", post1.getId())
                        .file(jsonPart)
                        .file(file1)
                        .file(file2)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(request -> {
                            request.setMethod("PUT"); // PUT 메서드로 설정
                            return request;
                        })
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 삭제 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void deletePostTest() throws Exception {
        List<Long> postIds = List.of(post1.getId(), post2.getId());

        mockMvc.perform(delete("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postIds))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 : 성공(관리자)")
    @WithMockUser(username = "김", roles = "ADMIN")
    void deletePostAdminTest() throws Exception {
        List<Long> postIds = List.of(post1.getId(), post2.getId());

        mockMvc.perform(delete("/api/post")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(postIds))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 : 실패 (권한 없음)")
    @WithMockUser(username = "권한없음", roles = "CUSTOMER")
    void deletePostFail() throws Exception {// 작성자와 다름
        List<Long> postIds = List.of(post1.getId(), post2.getId());

        mockMvc.perform(delete("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postIds))
                )
                .andExpect(status().isOk()); // 권한 있는 것만 삭제
    }


    @Test
    @DisplayName("좋아요 추가 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void saveLikeTest() throws Exception {
        Long customerId = 1L;

        mockMvc.perform(post("/api/post/{postId}/like", post1.getId())
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 검색 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void searchCategorySuccess() throws Exception {
        SearchPostRequestDto categoryRequest = SearchPostRequestDto.builder()
                .postCategory(PostCategory.ELECTRONICS)
                .build();
        String requestJson = objectMapper.writeValueAsString(categoryRequest);

        mockMvc.perform(post("/api/post/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("카테고리 검색 : 실패 (인증 없음)")
    void searchCategoryFailNoAuth() throws Exception {
        String requestJson = objectMapper.writeValueAsString(searchRequest);

        mockMvc.perform(post("/api/post/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("좋아요 삭제 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void deleteLikeTest() throws Exception {
        Long customerId = 1L;
        likesService.create(post1.getId(), customerId);

        mockMvc.perform(delete("/api/post/{postId}/like", post1.getId())
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk());
    }

    private RegisterPostRequestDto makePostRequestDto(String title, MultipartFile[] registerFiles) {
        return RegisterPostRequestDto.builder()
                .title(title)
                .body("text")
                .customerId(1L)
                .customerName("김")
                .category(PostCategory.BOOKS)
                .imageFiles(registerFiles)
                .build();
    }
}
