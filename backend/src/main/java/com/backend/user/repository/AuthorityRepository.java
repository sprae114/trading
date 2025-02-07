package com.backend.user.repository;


import com.backend.user.model.Role;
import com.backend.user.model.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    List<Authority> findAllByCustomerId(Long customerId);

    Optional<Authority> findByCustomerIdAndRole(Long customerId, Role role);
}
