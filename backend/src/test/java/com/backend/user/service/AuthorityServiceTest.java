package com.backend.user.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.dto.request.RegisterCustomerRequest;
import com.backend.user.model.Role;
import com.backend.user.model.entity.Authority;
import com.backend.user.model.entity.Customer;
import com.backend.user.repository.AuthorityRepository;
import com.backend.user.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class AuthorityServiceTest {
    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer customer;

    @BeforeEach
    void setUp() {
        // Given : 테스트 유저 생성
        RegisterCustomerRequest registerRequest = RegisterCustomerRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .pwd("password")
                .build();

        // When
        customer = customerService.create(registerRequest, Role.ROLE_CUSTOMER);
    }

    @Test
    @DisplayName("권한 생성 성공")
    void createAuthority_Success() {
        // Then
        Optional<Authority> authority = authorityRepository.findByCustomerIdAndRole(customer.getId(), Role.ROLE_CUSTOMER);
        assertTrue(authority.isPresent()); // 권한 존재 확인
        assertEquals(Role.ROLE_CUSTOMER, authority.get().getRole());
    }

    @Test
    @DisplayName("권한 생성 실패 - 중복 권한")
    void createAuthority_Failure_Duplicate() {
        // when
        authorityService.create(customer.getId(), Role.ROLE_ADMIN); // 이미 권한 생성

        // Then
        CustomException exception = assertThrows(CustomException.class,
                () -> authorityService.create(customer.getId(), Role.ROLE_ADMIN)); // 같은 권한 다시 생성

        assertEquals(ErrorCode.DUPLICATED_AUTHORITY, exception.getErrorCode());
    }

    @Test
    @DisplayName("권한 삭제 성공")
    void deleteAuthority_Success() {
        // When
        authorityService.delete(customer.getId(), Role.ROLE_CUSTOMER);

        // Then
        Optional<Authority> authority = authorityRepository.findByCustomerIdAndRole(customer.getId(), Role.ROLE_ADMIN);
        assertFalse(authority.isPresent()); // 권한 삭제 확인
    }

    @Test
    @DisplayName("권한 삭제 - 권한 없음")
    void deleteAuthority_NotFound() {
        // When: 존재하지 않는 권한 삭제 시도
        authorityService.delete(customer.getId(), Role.ROLE_ADMIN);

        // Then: 아무일도 일어나지 않음 (예외 발생 X)
    }


    @Test
    @DisplayName("고객 ID로 권한 목록 찾기")
    void findAuthoritiesByCustomerId() {
        // Given
        authorityService.create(customer.getId(), Role.ROLE_ADMIN);

        // When
        List<Authority> authorities = authorityService.find(customer.getId());

        // Then
        assertEquals(2, authorities.size()); // 기본 권한 + 추가한 권한
        assertTrue(authorities.stream().anyMatch(a -> a.getRole() == Role.ROLE_CUSTOMER));
        assertTrue(authorities.stream().anyMatch(a -> a.getRole() == Role.ROLE_ADMIN));
    }
}
