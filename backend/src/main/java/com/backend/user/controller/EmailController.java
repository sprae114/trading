package com.backend.user.controller;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.dto.request.AuthRequestDto;
import com.backend.user.repository.CustomerRepository;
import com.backend.user.service.AuthCodeService;
import com.backend.user.service.EmailService;
import com.backend.common.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class EmailController {

    private final CustomerRepository customerRepository;
    private final EmailService emailService;
    private final AuthCodeService authCodeService;
    private final RedisService redisService;

    @GetMapping("/register/auth")
    public String registerAuthEmail(@RequestParam("email") String email) {

        customerRepository.findByEmail(email).ifPresent(customer -> {
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL, email);
        });

        String authNumber = authCodeService.generateAuthCode();
        redisService.setKeyWithExpiration(email, authNumber); // 300초 동안 유효
        emailService.sendEmail(email, authNumber);

        return "이메일이 전송되었습니다.";
    }

    @GetMapping("/find-pw/auth")
    public String findPasswordAuthEmail(@RequestParam("email") String email) {

        customerRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND, email)
        );

        String authNumber = authCodeService.generateAuthCode();
        redisService.setKeyWithExpiration(email, authNumber); // 300초 동안 유효
        emailService.sendEmail(email, authNumber);

        return "이메일이 전송되었습니다.";
    }

    @PostMapping({"/register/auth", "/find-pw/auth"})
    public String checkRegisterAuthEmail(@RequestBody AuthRequestDto authRequestDto) {
        String email = authRequestDto.email();
        String auth = authRequestDto.otp();

        String authNumber = redisService.get(email).toString();
        if (authNumber == null || !authNumber.equals(auth)) {
            throw new CustomException(ErrorCode.INVALID_OTP, auth);
        }

        return "인증이 완료되었습니다.";
    }
}
