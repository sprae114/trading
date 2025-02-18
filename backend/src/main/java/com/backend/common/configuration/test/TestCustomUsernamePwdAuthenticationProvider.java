package com.backend.common.configuration.test;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * JWT 인증 처리를 위한 Provider
 * test 환경 : 로그인 처리함
 */
@Component
@Profile("test")
@RequiredArgsConstructor
public class TestCustomUsernamePwdAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;


    /**
     * UsernamePasswordAuthenticationToken을 지원하는지 확인
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    /**
     * 사용자 이름과 비밀번호를 사용한 인증 처리
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String pwd = authentication.getCredentials().toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!passwordEncoder.matches(pwd, userDetails.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD, email);
        }

        return new UsernamePasswordAuthenticationToken(email, pwd, userDetails.getAuthorities());
    }
}
