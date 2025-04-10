package com.backend.chat.dto;

import com.backend.chat.model.ChatRoom;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomSaveDto(
        @NotEmpty(message = "채팅방 이름을 입력하세요.")
        String name,

        @NotNull(message = "보낸 사람 Id를 입력하세요.")
        Long senderId,

        @NotEmpty(message = "보낸 사람을 입력하세요.")
        String sender,

        Long receiverId,

        @NotEmpty(message = "받는 사람을 입력하세요.")
        String receiver,

        LocalDateTime createdAt
) {
    public static ChatRoom toEntity(ChatRoomSaveDto chatRoomSaveDto){
        return ChatRoom.builder()
                .name(chatRoomSaveDto.name())
                .senderId(chatRoomSaveDto.senderId())
                .sender(chatRoomSaveDto.sender())
                .receiverId(chatRoomSaveDto.receiverId())
                .receiver(chatRoomSaveDto.receiver())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
