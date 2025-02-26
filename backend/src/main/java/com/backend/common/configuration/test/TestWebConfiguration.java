package com.backend.common.configuration.test;

import com.backend.common.filter.JWTTokenValidatorFilter;
import com.backend.user.service.CustomerUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@Profile("test")
public class TestWebConfiguration {

    /**
     * 보안 설정
     */
    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/login/**").permitAll()
                        .requestMatchers("/api/post/**").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        .anyRequest().permitAll());

        http.sessionManagement(sessionConfig ->
                sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JWTTokenValidatorFilter(), BasicAuthenticationFilter.class); // JWT 토큰 검증 필터 추가
        return http.build();
    }

    /**
     * 패스워드 인코더 빈 등록
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }


    /**
     * AuthenticationManager 빈 등록(인증 처리를 위한 매니저)
     */
    @Bean
    public AuthenticationManager authenticationManager(CustomerUserDetailsService userService,
                                                       PasswordEncoder passwordEncoder) {

        TestCustomUsernamePwdAuthenticationProvider CustomProvider =
                new TestCustomUsernamePwdAuthenticationProvider(userService, passwordEncoder);

        ProviderManager providerManager = new ProviderManager(CustomProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);

        return providerManager;
    }
}
