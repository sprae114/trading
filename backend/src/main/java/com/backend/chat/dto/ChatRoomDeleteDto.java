package com.backend.chat.dto;

import com.backend.user.model.Role;
import lombok.Builder;

import java.util.List;

@Builder
public record ChatRoomDeleteDto(
        String loginName,
        Role role,
        List<RoomInfo> roomInfo
) {}
