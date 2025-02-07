package com.backend.user.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.backend.user.dto.request.AuthRequestDto;
import com.backend.user.service.AuthCodeService;
import com.backend.user.service.EmailService;
import com.backend.user.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Jackson ObjectMapper

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private AuthCodeService authCodeService;

    @MockBean
    private RedisService redisService;

    private String jsonRequest;

    @BeforeEach
    public void setUp() throws Exception {
        jsonRequest = objectMapper.writeValueAsString(new AuthRequestDto("test@example.com", "123456"));
    }


    @DisplayName("회원가입 인증 이메일 전송 - 성공")
    @Test
    public void testRegisterAuthEmail() throws Exception {
        //given
        String email = "test@example.com";
        String authNumber = "123456";

        when(authCodeService.generateAuthCode()).thenReturn(authNumber);
        doNothing().when(redisService).setKeyWithExpiration(anyString(), anyString());

        //when
        mockMvc.perform(get("/login/register/auth")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isOk())
                .andExpect(content().string("이메일이 전송되었습니다."));

        //then
        verify(redisService, times(1)).setKeyWithExpiration(email, authNumber);
    }

    @DisplayName("회원가입 인증 이메일 전송 - 실패(이메일 형식 X)")
    @Test
    public void testRegisterAuthEmail_Failure() throws Exception {
        //given
        String email = "test";

        when(redisService.get(email)).thenReturn("11111");

        //when
        mockMvc.perform(get("/login/register/auth")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                .andExpect(status().isBadRequest()); // 예외 발생 시 적절한 상태 코드 확인
    }

    @DisplayName("회원가입 인증 완료 - 성공")
    @Test
    public void testCheckRegisterAuthEmail_Success() throws Exception {
        //given
        String email = "test@example.com";
        String otp = "123456";

        when(redisService.get(email)).thenReturn(otp);

        mockMvc.perform(post("/login/register/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("인증이 완료되었습니다."));

        verify(redisService, times(1)).get(email);
    }


    @DisplayName("회원가입 인증 완료 - 실패(잘못된 인증 번호)")
    @Test
    public void testCheckRegisterAuthEmail_Failure_WrongAuth() throws Exception {
        String email = "test@example.com";
        String auth = "123456";

        when(redisService.get(email)).thenReturn("11111");

        mockMvc.perform(post("/login/register/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());

        verify(redisService, times(1)).get(email);
    }


    @DisplayName("비밀번호 찾기 인증 전송 - 성공")
    @Test
    public void testFindPasswordAuthEmail() throws Exception {
        String email = "test@example.com";
        String authNumber = "654321";

        when(authCodeService.generateAuthCode()).thenReturn(authNumber);
        doNothing().when(redisService).setKeyWithExpiration(anyString(), anyString());

        mockMvc.perform(get("/login/find-pw/auth")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("이메일이 전송되었습니다."));

        verify(redisService, times(1)).setKeyWithExpiration(email, authNumber);
    }

    @DisplayName("비밀번호 찾기 이메일 전송 - 실패(이메일 형식 X)")
    @Test
    public void testFindPasswordAuthEmail_Failure() throws Exception {
        String email = "test";

        when(redisService.get(email)).thenReturn("11111");

        mockMvc.perform(get("/login/find-pw/auth")
                        .param("email", email)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("비밀번호 찾기 인증 - 성공")
    @Test
    public void testCheckFindPasswordAuthEmail_Success() throws Exception {
        String email = "test@example.com";
        String auth = "123456";

        when(redisService.get(email)).thenReturn(auth);

        mockMvc.perform(post("/login/find-pw/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("인증이 완료되었습니다."));
    }

    @DisplayName("비밀번호 찾기 인증 - 실패 (잘못된 인증 번호)")
    @Test
    public void testCheckFindPasswordAuthEmail_Failure_WrongAuth() throws Exception {
        String email = "test@example.com";
        String auth = "123456";

        when(redisService.get(email)).thenReturn("11111");

        mockMvc.perform(post("/login/find-pw/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }
}