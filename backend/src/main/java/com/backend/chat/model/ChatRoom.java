package com.backend.chat.model;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Document(collection = "chat_rooms")
@Data
public class ChatRoom {

    @Id
    private String id;

    private String name;

    private Long senderId;

    private String sender;

    private String receiver;

    private LocalDateTime createdAt;
}