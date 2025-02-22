package com.backend.chat.dto;

import com.backend.chat.model.ChatRoom;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomSaveDto(
        @NotEmpty(message = "채팅방 이름을 입력하세요.")
        String name,

        @NotEmpty(message = "보낸 사람 Id를 입력하세요.")
        Long senderId,

        @NotEmpty(message = "보낸 사람을 입력하세요.")
        String sender,

        @NotEmpty(message = "받는 사람을 입력하세요.")
        String receiver,

        LocalDateTime createdAt
) {
    public static ChatRoom toEntity(ChatRoomSaveDto chatRoomSaveDto){
        return ChatRoom.builder()
                .name(chatRoomSaveDto.name())
                .senderId(chatRoomSaveDto.senderId())
                .sender(chatRoomSaveDto.sender())
                .receiver(chatRoomSaveDto.receiver())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
