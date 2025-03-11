package com.backend.common.controller;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.service.S3Service;
import com.backend.post.model.entity.Post;
import com.backend.post.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private S3Service s3Service;

    @MockBean
    private PostService postService;

    private MockMultipartFile[] files;
    private String postIdJson;
    private String fileNamesJson;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 데이터 초기화
        files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test1.jpg", "image/jpeg", "test image content".getBytes()),
                new MockMultipartFile("files", "test2.png", "image/png", "test image content 2".getBytes())
        };

        postIdJson = "1"; // 단일 postId JSON
        fileNamesJson = "[\"test1.jpg\", \"test2.png\"]"; // 다중 파일 이름 JSON

        // Mock 데이터 설정
        List<String> mockUrls = Arrays.asList("https://s3.amazonaws.com/test1.jpg", "https://s3.amazonaws.com/test2.png");
        when(s3Service.uploadFiles(files)).thenReturn(mockUrls);

        Post mockPost = Post.builder().imageUrls(Arrays.asList("test1.jpg", "test2.png")).build();
        when(postService.getOne(1L)).thenReturn(mockPost);

        List<byte[]> mockContents = Arrays.asList("image content 1".getBytes(), "image content 2".getBytes());
        when(s3Service.downloadFiles(mockUrls)).thenReturn(mockContents);
    }

    @DisplayName("이미지 업로드 : 성공")
    @Test
    @WithMockUser
    public void uploadImageSuccess() throws Exception {
        // 요청
        ResultActions result = mockMvc.perform(multipart("/api/images/upload")
                .file(files[0])
                .file(files[1])
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("https://s3.amazonaws.com/test1.jpg"))
                .andExpect(jsonPath("$[1]").value("https://s3.amazonaws.com/test2.png"));

        verify(s3Service, times(1)).uploadFiles(files);
    }

    @DisplayName("이미지 업로드 : 실패(파일 누락)")
    @Test
    @WithMockUser
    public void uploadImageFailNoFiles() throws Exception {
        // 요청
        ResultActions result = mockMvc.perform(multipart("/api/images/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA));

        // 검증
        result.andExpect(status().isBadRequest()); // Spring 기본적으로 파일 없으면 400 반환

        verify(s3Service, never()).uploadFiles(any());
    }

    @DisplayName("이미지 다운로드(게시글) : 성공")
    @Test
    @WithMockUser
    public void downloadImageByPostSuccess() throws Exception {
        // 요청
        ResultActions result = mockMvc.perform(post("/api/images/download/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postIdJson));

        // 검증
        result.andExpect(status().isOk());

        verify(postService, times(1)).getOne(1L);
        verify(s3Service, times(1)).downloadFiles(anyList());
    }

    @DisplayName("이미지 다운로드(게시글) : 실패(존재하지 않는 게시글)")
    @Test
    @WithMockUser
    public void downloadImageByPostFailPostNotFound() throws Exception {
        // 설정
        when(postService.getOne(999L)).thenThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

        // 요청
        ResultActions result = mockMvc.perform(post("/api/images/download/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content("999"));

        // 검증
        result.andExpect(status().isNotFound());

        verify(postService, times(1)).getOne(999L);
        verify(s3Service, never()).downloadFiles(anyList());
    }

    @DisplayName("다중 이미지 다운로드 : 성공")
    @Test
    @WithMockUser
    public void downloadMultipleImagesSuccess() throws Exception {
        // 요청
        ResultActions result = mockMvc.perform(post("/api/images/download/multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(fileNamesJson));

        // 검증
        result.andExpect(status().isOk());
        verify(s3Service, times(1)).downloadFiles(anyList());
    }

    @DisplayName("다중 이미지 다운로드 : 실패(잘못된 파일 이름)")
    @Test
    @WithMockUser
    public void downloadMultipleImagesFailInvalidFileNames() throws Exception {
        // 설정
        String invalidFileNamesJson = "[\"invalid.jpg\"]";
        when(s3Service.downloadFiles(anyList())).thenReturn(null);

        // 요청
        ResultActions result = mockMvc.perform(post("/api/images/download/multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidFileNamesJson));

        // 검증
        verify(s3Service, times(1)).downloadFiles(anyList());
    }
}