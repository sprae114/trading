package com.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record LoginCustomerRequest(
        @NotEmpty(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 아닙니다.")
        String email,

        @NotEmpty(message = "비밀번호를 입력해주세요.")
        String pwd
) {
}
