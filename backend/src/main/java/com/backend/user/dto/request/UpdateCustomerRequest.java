package com.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record UpdateCustomerRequest(
        @NotEmpty(message = "이름은 필수 입력 값입니다.")
        String name,

        @NotEmpty(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotEmpty(message = "비밀번호는 필수 입력 값입니다.")
        String pwd
) {}