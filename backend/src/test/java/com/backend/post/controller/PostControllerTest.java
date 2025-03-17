package com.backend.post.controller;

import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.model.PostCategory;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.LikesRepository;
import com.backend.post.repository.PostRepository;
import com.backend.post.service.LikesService;
import com.backend.post.service.PostService;
import com.backend.user.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
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
    private PostRepository postRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private LikesService likesService;


    private Post post1;
    private Post post2;


    @BeforeEach
    void setUp() {
        post1 = postService.create(makePostRequestDto("제목1"));
        post2 = postService.create(makePostRequestDto("제목2"));
    }

    @Test
    @DisplayName("게시글 생성 : 성공")
    @WithMockUser
    void createPost() throws Exception {
        String request = objectMapper.writeValueAsString(makePostRequestDto("제목3"));

        mockMvc.perform(post("/api/post")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 생성 : 실패 (제목 없음)")
    @WithMockUser
    void createPostFail() throws Exception {
        // 제목이 없는 DTO
        RegisterPostRequestDto requestDto = RegisterPostRequestDto.builder()
                .body("text")
                .customerId(1L)
                .category(PostCategory.DAILY)
                .customerName("김")
                .build();
        String request = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/api/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
                )
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

        UpdateRequestDto updateRequestDto = new UpdateRequestDto(post1.getId(), "수정된 제목", "수정된 내용");

        mockMvc.perform(put("/api/post/{postId}", post1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 : 성공(관리자)")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updatePostAdminTest() throws Exception {

        UpdateRequestDto updateRequestDto = new UpdateRequestDto(post1.getId(), "수정된 제목", "수정된 내용");

        mockMvc.perform(put("/api/post/{postId}", post1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 수정 : 실패 (권한 없음)")
    @WithMockUser(username = "권한없음", roles = "CUSTOMER") // 다른 사용자 이름 지정
    void updatePostFail() throws Exception {
        UpdateRequestDto updateRequestDto = new UpdateRequestDto(post1.getId(),"수정된 제목", "수정된 내용");

        mockMvc.perform(put("/api/post/{postId}", post1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDto))) // principal 설정은 유지
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("게시글 삭제 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void deletePostTest() throws Exception {
        mockMvc.perform(delete("/api/post/{postId}", post1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 : 성공(관리자)")
    @WithMockUser(username = "김", roles = "ADMIN")
    void deletePostAdminTest() throws Exception {
        mockMvc.perform(delete("/api/post/{postId}", post1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 : 실패 (권한 없음)")
    @WithMockUser(username = "권한없음", roles = "CUSTOMER")
    void deletePostFail() throws Exception {// 작성자와 다름

        mockMvc.perform(delete("/api/post/{postId}", post1.getId())) //여전히 필요
                .andExpect(status().isUnauthorized());
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
    @DisplayName("좋아요 삭제 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void deleteLikeTest() throws Exception {
        Long customerId = 1L;
        likesService.create(post1.getId(), customerId);

        mockMvc.perform(delete("/api/post/{postId}/like", post1.getId())
                        .param("customerId", customerId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("좋아요 게시글 목록 조회 : 성공")
    @WithMockUser(username = "김", roles = "CUSTOMER")
    void getLikesPostTest() throws Exception {
        Long customerId = 1L;
        likesService.create(post1.getId(), customerId);
        likesService.create(post2.getId(), customerId);

        mockMvc.perform(get("/api/post/likes")
                        .param("customerId", customerId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("제목1"))
                .andExpect(jsonPath("$[1].title").value("제목2"));
    }

    private RegisterPostRequestDto makePostRequestDto(String title) {
        return RegisterPostRequestDto.builder()
                .title(title)
                .body("text")
                .customerId(1L)
                .category(PostCategory.DAILY)
                .customerName("김")
                .build();
    }
}
