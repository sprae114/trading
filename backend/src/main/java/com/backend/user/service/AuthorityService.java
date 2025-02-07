package com.backend.user.service;


import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.dto.request.RegisterCustomerRequest;
import com.backend.user.model.Role;
import com.backend.user.model.entity.Authority;
import com.backend.user.model.entity.Customer;
import com.backend.user.repository.AuthorityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    public void create(Long CustomerId, Role role) {

        // 이미 존재하는 권한인지 확인
        authorityRepository.findByCustomerIdAndRole(CustomerId, role).ifPresent(
                auth -> {
                    throw new CustomException(ErrorCode.DUPLICATED_AUTHORITY, auth.getRole().toString());
                }
        );

        Authority authority = Authority.builder()
                .customerId(CustomerId)
                .role(role)
                .build();

        authorityRepository.save(authority);
    }

    public void delete(Long CustomerId, Role role) {
        authorityRepository.findByCustomerIdAndRole(CustomerId, role).ifPresent(
                authority -> authorityRepository.deleteById(authority.getId())
        );
    }

    public List<Authority> find(Long customerId) {
        return authorityRepository.findAllByCustomerId(customerId);
    }
}
