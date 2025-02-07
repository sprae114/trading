package com.backend.user.dto.response;

import com.backend.user.model.Role;
import com.backend.user.model.entity.Customer;
import lombok.Builder;

@Builder
public record CustomerDetailsDto(
        Customer customer,
        Role role
) {
}
