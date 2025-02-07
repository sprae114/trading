package com.backend.user.controller;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.util.ApplicationConstants;
import com.backend.user.dto.request.LoginCustomerRequest;
import com.backend.user.dto.request.RegisterCustomerRequest;
import com.backend.user.dto.request.UpdateCustomerRequest;
import com.backend.user.dto.response.LoginResponseDto;
import com.backend.user.model.Role;
import com.backend.user.model.entity.Customer;
import com.backend.user.service.CustomerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;



import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc // MockMvc 주입
@Transactional
class CustomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // JSON 변환을 위한 ObjectMapper

    @Autowired
    private CustomerService customerService;


    private String registerRequest;
    private String loginRequest;
    private String updateRequest;
    private Customer customer;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // 테스트 데이터
        RegisterCustomerRequest user = RegisterCustomerRequest.builder()
                .name("Test User")
                .email("aaaa@example.com")
                .pwd("password")
                .build();

        registerRequest = makeRegisterRequest("test@test.com");
        loginRequest = makeLoginRequest("aaaa@example.com");
        updateRequest = makeUpdateRequest("aaaa@example.com");

        customer = customerService.create(user, Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("회원 가입 - 성공")
    void registerUser_Success() throws Exception {
        // When
        mockMvc.perform(post("/login/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerRequest)
                )

                .andExpect(status().isOk())
                .andExpect(content().string("성공적으로 회원이 등록되었습니다."));
    }

    @Test
    @DisplayName("회원 가입 - 실패(중복 이메일)")
    void registerUser_Fail_DuplicatedEmail() throws Exception {
        String duplicateEmail = makeRegisterRequest("aaaa@example.com");

        mockMvc.perform(post("/login/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(duplicateEmail)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value(ErrorCode.DUPLICATED_EMAIL.toString()));
    }

    @Test
    @DisplayName("회원 가입 - 실패(이메일 형식 오류)")
    void registerUser_Fail_InvalidEmail() throws Exception {
        String invalidEmail = makeRegisterRequest("invalidemail");

        mockMvc.perform(post("/login/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEmail)
                )

                .andExpect(status().isBadRequest());
    }

    @DisplayName("로그인 - 성공")
    @Test
    public void testLoginUser_Success() throws Exception {

        mockMvc.perform(post("/login/home")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest)
                )

                .andExpect(status().isOk())
                .andExpect(header().exists(ApplicationConstants.JWT_HEADER))
                .andExpect(header().string(ApplicationConstants.JWT_HEADER, notNullValue()));
    }

    @DisplayName("로그인 - 실패(존재하지 않는 이메일)")
    @Test
    public void testLoginUser_Fail_NotExistEmail() throws Exception {
        String notExistEmail = makeLoginRequest("1111@example.com");

        mockMvc.perform(post("/login/home")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notExistEmail)
                )

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(ErrorCode.USER_NOT_FOUND.toString()));
    }


    @DisplayName("로그인 - 실패(비밀번호 불일치)")
    @Test
    public void testLoginUser_Fail_WrongPassword() throws Exception {
        String wrongPassword = objectMapper.writeValueAsString(LoginCustomerRequest.builder()
                .email("aaaa@example.com")
                .pwd("1111")
                .build());

        mockMvc.perform(post("/login/home")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPassword)
                )


                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.resultCode").value(ErrorCode.INVALID_PASSWORD.toString()));
    }

    @DisplayName("비밀번호 수정 - 성공")
    @Test
    void findPassword_Success() throws Exception {
        // When
       mockMvc.perform(post("/login/find-pw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequest)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("비밀번호가 성공적으로 변경되었습니다."));
    }

    @Test
    @DisplayName("비밀번호 수정 - 실패(존재하지 않는 이메일)")
    void findPassword_Fail_NotExistEmail() throws Exception {
        String notExistEmail = makeUpdateRequest("1111@example.com");

        mockMvc.perform(post("/login/find-pw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notExistEmail)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value(ErrorCode.USER_NOT_FOUND.toString()));
    }

    private String makeRegisterRequest(String email) throws JsonProcessingException {
        return objectMapper.writeValueAsString(RegisterCustomerRequest.builder()
                .name("Test User")
                .email(email)
                .pwd("password")
                .build());
    }

    private String makeLoginRequest(String email) throws JsonProcessingException {
        return objectMapper.writeValueAsString(LoginCustomerRequest.builder()
                .email(email)
                .pwd("password")
                .build());
    }

    private String makeUpdateRequest(String email) throws JsonProcessingException {
        return objectMapper.writeValueAsString(UpdateCustomerRequest.builder()
                .name("Updated Name")
                .email(email)
                .pwd("newpassword")
                .build());
    }


}