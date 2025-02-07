package com.backend.user.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.dto.request.RegisterCustomerRequest;
import com.backend.user.dto.request.UpdateCustomerRequest;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
public class CustomerServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthorityService authorityService;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    private CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    private RegisterCustomerRequest registerRequest;
    private UpdateCustomerRequest updateRequest;


    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        registerRequest = RegisterCustomerRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .pwd("password")
                .build();

        updateRequest = UpdateCustomerRequest.builder()
                .name("Updated Name")
                .email("test@example.com") // 기존 이메일 사용
                .pwd("newpassword")
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void createCustomer_Success() {
        // When
        Customer createdCustomer = customerService.create(registerRequest, Role.ROLE_CUSTOMER);

        // Then
        assertNotNull(createdCustomer);
        assertNotNull(createdCustomer.getId());  // ID가 생성되었는지 확인
        assertEquals(registerRequest.email(), createdCustomer.getEmail());
        assertTrue(passwordEncoder.matches(registerRequest.pwd(), createdCustomer.getPwd())); // 암호화 확인

        // 권한 확인
        List<Authority> authorities = authorityService.find(createdCustomer.getId());
        assertEquals(1, authorities.size());
        assertEquals(Role.ROLE_CUSTOMER, authorities.get(0).getRole());
    }

    @Test
    @DisplayName("회원 가입 실패 - 중복 이메일")
    void createCustomer_Failure_DuplicateEmail() {
        // Given: 이미 존재하는 사용자 생성
        customerService.create(registerRequest, Role.ROLE_CUSTOMER);

        // When, Then: 중복 이메일로 다시 가입 시도
        RegisterCustomerRequest duplicateRequest = RegisterCustomerRequest.builder()
                .name("Another User")
                .email("test@example.com") // 중복 이메일
                .pwd("password2")
                .build();

        CustomException exception = assertThrows(CustomException.class, () -> customerService.create(duplicateRequest, Role.ROLE_CUSTOMER));
        assertEquals(ErrorCode.DUPLICATED_EMAIL, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("test@example.com"));
    }

    @Test
    @DisplayName("회원 정보 업데이트 성공")
    void updateCustomer_Success() {
        // Given: 기존 사용자 생성
        Customer createdCustomer = customerService.create(registerRequest, Role.ROLE_CUSTOMER);
        Long customerId = createdCustomer.getId();

        // When: 정보 업데이트
        customerService.update(updateRequest);

        // Then: 업데이트된 정보 확인
        Customer updatedCustomer = customerRepository.findById(customerId).orElseThrow(); // DB에서 다시 가져옴
        assertEquals("Updated Name", updatedCustomer.getName());
        assertTrue(passwordEncoder.matches("newpassword", updatedCustomer.getPwd()));
    }



    @Test
    @DisplayName("이메일로 회원 찾기 성공")
    void findByEmail_Success() {
        // Given
        Customer createdCustomer = customerService.create(registerRequest, Role.ROLE_CUSTOMER);

        // When
        Customer foundCustomer = customerService.findByEmail(registerRequest.email());

        // Then
        assertNotNull(foundCustomer);
        assertEquals(createdCustomer.getId(), foundCustomer.getId());
        assertEquals(registerRequest.email(), foundCustomer.getEmail());
    }

    @Test
    @DisplayName("이메일로 회원 찾기 실패 - 사용자 없음")
    void findByEmail_Failure_UserNotFound() {
        // When, Then
        CustomException exception = assertThrows(CustomException.class,
                () -> customerService.findByEmail("nonexistent@example.com"));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        assertTrue(exception.getMessage().contains("nonexistent@example.com"));
    }


    @Test
    @DisplayName("loadUserByUsername 성공")
    void loadUserByUsername_Success(){
        // Given
        Customer createdCustomer = customerService.create(registerRequest, Role.ROLE_CUSTOMER);

        // When
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername(registerRequest.email());

        // then
        assertNotNull(userDetails);
        assertEquals(registerRequest.email(), userDetails.getUsername());
        assertTrue(passwordEncoder.matches(registerRequest.pwd(), userDetails.getPassword())); // 비밀번호 확인
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(Role.ROLE_CUSTOMER.toString()))); // 권한 확인

    }

    @Test
    @DisplayName("loadUserByUsername 실패 - 사용자 없음")
    void loadUserByUsername_Failure_UserNotFound(){
        // When, Then
        assertThrows(CustomException.class, () -> customerUserDetailsService.loadUserByUsername("nonexistent@example.com"));
    }
}
