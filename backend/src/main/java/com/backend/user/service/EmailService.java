package com.backend.user.service;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.dto.request.EmailRequest;
import com.backend.user.util.EnvLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 이메일 서비스
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EnvLoader envLoader; // 환경 변수 로더

    // 이메일 전송
    @Async
    public void sendEmail(String email, String authNumber) {
        validateEmail(email);

        String subject = "인증 메일입니다.";
        String authBody = "인증 번호는 " + authNumber + " 입니다.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(loadEnv());
        message.setTo(email);
        message.setSubject(subject);
        message.setText(authBody);

        mailSender.send(message);
    }

    // 이메일 검증
    private void validateEmail(String email) {
        if (email == null || !email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE ,email);
        }
    }

    // 환경변수 로드
    private String loadEnv() {
        if (envLoader.get("NAVER_MAIL_ID") == null) {
            throw new CustomException(ErrorCode.ENVIRONMENT_VARIABLE_NOT_FOUND, "NAVER_MAIL_ID");
        }
        return envLoader.get("NAVER_MAIL_ID");
    }
}