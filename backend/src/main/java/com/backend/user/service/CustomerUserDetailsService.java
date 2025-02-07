package com.backend.user.service;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.model.entity.Customer;
import com.backend.user.repository.AuthorityRepository;
import com.backend.user.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final AuthorityRepository authorityRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, email));

        // 권한 가져와 저장하기
        Set<SimpleGrantedAuthority> authorities = authorityRepository
                .findAllByCustomerId(customer.getId())
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getRole().toString()))
                .collect(Collectors.toSet());

        // 시큐리티 User 객체 생성하여 처리
        return new User(customer.getEmail(), customer.getPwd(), authorities);
    }
}
