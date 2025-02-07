package com.backend.user.dto.response;

public record LoginResponseDto(
        String jwtToken,
        CustomerDetailsDto customerDetails) {
}
