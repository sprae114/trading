package com.backend.chat.dto;

import lombok.Builder;

@Builder
public record ChatRoomSearchRequestDto(
        Long senderId,
        String searchText
) {}
