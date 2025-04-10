package com.backend.chat.dto;

import com.backend.chat.model.ChatRoom;
import com.backend.post.dto.response.PostSimpleResponseDto;
import lombok.Builder;

@Builder
public record ChatRoomInfoResponseDto(
        ChatRoom chatRoom,
        PostSimpleResponseDto postSimpleResponseDto
) {}