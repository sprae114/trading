package com.backend.user.service;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.dto.request.LoginCustomerRequest;
import com.backend.user.dto.request.RegisterCustomerRequest;
import com.backend.user.dto.request.UpdateCustomerRequest;
import com.backend.user.model.Role;
import com.backend.user.model.entity.Customer;
import com.backend.user.repository.CustomerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthorityService authorityService;
    private final PasswordEncoder passwordEncoder;


    public Customer create(RegisterCustomerRequest request, Role role) {
        if (customerRepository.findByEmail(request.email()).isPresent()) {// 이미 존재하는 이메일인 경우
            throw new CustomException(ErrorCode.DUPLICATED_EMAIL, request.email());
        }

        Customer saveCustomer = customerRepository.save(Customer.builder()
                .email(request.email())
                .name(request.name())
                .pwd(passwordEncoder.encode(request.pwd())) // 비밀번호 암호화
                .build()
        );

        authorityService.create(saveCustomer.getId(), role);

        return saveCustomer;
    }

    public void update(UpdateCustomerRequest request) {
        Customer customer = findByEmail(request.email()); //변수명 변경

        // 변경사항 저장 - toBuilder 사용 + 비밀번호 null 체크
        Customer updatedCustomer = customer.toBuilder()
                .name(request.name())
                .pwd(request.pwd() != null ? passwordEncoder.encode(request.pwd()) : customer.getPwd())
                .build();

        customerRepository.save(updatedCustomer);
    }

    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND, email)
        );
    }


}
