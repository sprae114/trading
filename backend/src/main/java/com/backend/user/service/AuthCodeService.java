package com.backend.user.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * OTP 랜덤 숫자 생성
 */
@Transactional
@Service
public class AuthCodeService {

    private final Random random = new Random();

    public String generateAuthCode() {
        // 5자리의 랜덤한 숫자를 생성
        int authCode = 100000 + random.nextInt(900000); // 100000부터 999999까지
        return String.valueOf(authCode);
    }
}