package com.backend.user.service;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.util.EnvLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EnvLoader envLoader;

    @InjectMocks
    private EmailService emailService;

    private String validEmail;
    private String invalidEmail;
    private String authNumber;
    private String mailId;

    @BeforeEach
    void setUp() {
        validEmail = "test@example.com";
        invalidEmail = "invalid-email";
        authNumber = "123456";
        mailId = "test_id";
    }

    @Test
    @DisplayName("이메일 전송 - 성공")
    void sendEmailSuccess() {
        // Given
        when(envLoader.get("NAVER_MAIL_ID")).thenReturn(mailId); // Mock EnvLoader 설정

        // When
        emailService.sendEmail(validEmail, authNumber);

        // Then
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class)); // send() 호출 여부 확인
    }

    @Test
    @DisplayName("이메일 전송 - 실패(유효하지 않은 이메일 주소)")
    void sendEmailFailInvalidEmail() {
        // Given, When, Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            emailService.sendEmail(invalidEmail, authNumber);
        });

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }


    @Test
    @DisplayName("이메일 전송 - 실패(환경 변수 로드)")
    void sendEmailFailEnvLoadFail() {
        //given
        when(envLoader.get("NAVER_MAIL_ID")).thenReturn(null); // Mock EnvLoader 설정 (null 반환)

        // When, Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            emailService.sendEmail(validEmail, authNumber);
        });

        assertEquals(ErrorCode.ENVIRONMENT_VARIABLE_NOT_FOUND, exception.getErrorCode());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이메일 주소 검증 - 실패(null)")
    void validateEmailFailNull() {
        // Given , When, Then
        CustomException exception = assertThrows(CustomException.class, () ->{
            emailService.sendEmail(null, authNumber); // validateEmail()이 내부적으로 호출.
        });

        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 주소 검증 - 실패(잘못된 형식)")
    void validateEmailFailInvalidFormat() {
        // Given , When, Then
        CustomException exception = assertThrows(CustomException.class, () ->{
            emailService.sendEmail(invalidEmail, authNumber); // validateEmail()이 내부적으로 호출.
        });
        assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    }
}
