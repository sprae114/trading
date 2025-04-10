package com.backend.chat.dto;

import com.backend.chat.model.ChatMessage;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record ChatMessageSaveDto(
        @NotNull(message = "채팅방을 입력하세요.")
        String roomId,

        @NotEmpty(message = "보낸 사람을 입력하세요.")
        String sender,

        @NotEmpty(message = "채팅을 입력하세요.")
        String content,

        LocalDateTime timestamp
) {


    public static ChatMessage toEntity(ChatMessageSaveDto chatMessageSaveDto){
        return ChatMessage.builder()
                .roomId(chatMessageSaveDto.roomId())
                .sender(chatMessageSaveDto.sender())
                .content(chatMessageSaveDto.content())
                .timestamp(chatMessageSaveDto.timestamp())
                .build();
    }

    public static ChatMessage toEntityTime(ChatMessageSaveDto chatMessageSaveDto){
        return ChatMessage.builder()
                .roomId(chatMessageSaveDto.roomId())
                .sender(chatMessageSaveDto.sender())
                .content(chatMessageSaveDto.content())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
