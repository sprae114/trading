package com.backend.user.controller;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.util.ApplicationConstants;
import com.backend.user.dto.request.LoginCustomerRequest;
import com.backend.user.dto.request.RegisterCustomerRequest;
import com.backend.user.dto.request.UpdateCustomerRequest;
import com.backend.user.dto.response.CustomerDetailsDto;
import com.backend.user.dto.response.LoginResponseDto;
import com.backend.user.model.Role;
import com.backend.user.model.entity.Customer;
import com.backend.user.service.CustomerService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;


/**
 * 회원 관련 컨트롤러
 * 회원 가입, 로그인, 로그아웃 등의 기능을 제공
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/login")
public class CustomController {

    private final CustomerService customerService;
    private final AuthenticationManager authenticationManager;
    private final Environment env;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterCustomerRequest requestDto) {
        customerService.create(requestDto, Role.ROLE_CUSTOMER);
        return ResponseEntity.ok("성공적으로 회원이 등록되었습니다.");
    }


    // 로그인 처리(토큰 발급)
    @PostMapping("/home")
    public ResponseEntity<LoginResponseDto> apiLogin(@RequestBody LoginCustomerRequest requestDto) {

        Authentication authentication = new UsernamePasswordAuthenticationToken(requestDto.email(), requestDto.pwd());

        // 인증 정보를 인증 매니저에 전달하여 인증 처리
        Authentication authenticationResponse = authenticationManager.authenticate(authentication);

        // 인증 실패 시 예외 처리
        if (!authenticationResponse.isAuthenticated()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Customer findCustomer = customerService.findByEmail(requestDto.email());
        String findAuthorities = authenticationResponse.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String jwt = "";
        if (null != env) {
            String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                    ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);



            SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            jwt = Jwts.builder()
                    .issuer("Trading System")
                    .subject("JWT Token")
                    .claim("email", authenticationResponse.getName())
                    .claim("username", findCustomer.getName())
                    .claim("authorities", findAuthorities)
                    .issuedAt(new java.util.Date())
                    .expiration(new java.util.Date((new java.util.Date()).getTime() + 3000000)) // 토큰 만료 시간 설정 (50분)
                    .signWith(secretKey).compact();
        }

        // 사용자 정보 반환
        CustomerDetailsDto userInformation = CustomerDetailsDto.builder()
                .customer(findCustomer)
                .role(Role.valueOf(findAuthorities))
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(ApplicationConstants.JWT_HEADER, jwt)
                .body(new LoginResponseDto(jwt, userInformation));
    }


    // 비밀번호 수정
    @PostMapping("/find-pw")
    public ResponseEntity<String> findPassword(@RequestBody UpdateCustomerRequest requestDto) {
        customerService.update(requestDto);

        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
