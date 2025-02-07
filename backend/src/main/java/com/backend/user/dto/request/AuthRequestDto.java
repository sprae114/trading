package com.backend.user.dto.request;

public record AuthRequestDto(
        String email,
        String otp) {
}
