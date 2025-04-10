package com.backend.common.filter;

import com.backend.common.exception.ErrorCode;
import com.backend.common.util.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 토큰 검증 필터
 */
@Slf4j
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JWTTokenValidatorFilter doFilterInternal");
        String authHeader = request.getHeader(ApplicationConstants.JWT_HEADER); // "Authorization"

        String jwt = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7).trim(); // "Bearer " 제거 후 공백 정리
        }

        if(null != jwt) {
            try {
                Environment env = getEnvironment();

                if (null != env) {
                    // JWT 비밀키 설정
                    String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                            ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                    log.debug("JWTTokenValidatorFilter secret: {}, key: {}", secret, secretKey);

                    if(null !=secretKey) {
                        // JWT 토큰 검증
                        Claims claims = Jwts.parser().verifyWith(secretKey)
                                .build().parseSignedClaims(jwt).getPayload();
                        String email = String.valueOf(claims.get("email"));
                        String authorities = String.valueOf(claims.get("authorities"));
                        List<GrantedAuthority> authorityList = AuthorityUtils.commaSeparatedStringToAuthorityList(authorities);

                        // 인증 객체 생성
                        Authentication authentication = new UsernamePasswordAuthenticationToken(email,
                                null, authorityList);
                        log.info("JWTTokenValidatorFilter doFilterInternal: email: {} authorities: {}", authentication.getDetails() ,authentication.getAuthorities());

                        // 인증 객체를 SecurityContextHolder에 저장
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }

            } catch (Exception exception) {
                log.error("JWTTokenValidatorFilter doFilterInternal", exception);
                throw new BadCredentialsException(ErrorCode.INVALID_TOKEN.getMessage(), exception);
            }
        }
        filterChain.doFilter(request,response);
    }

    // JWT 토큰 검증 필터를 적용할 필요가 없는 URL을 설정
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getServletPath().equals("/login/**");
    }
}
