package com.backend.chat.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
@Data
public class ChatMessage implements Serializable {
    @Id
    private String id;

    @NotEmpty(message = "채팅방을 입력하세요.")
    private String roomId;


    private String sender;
    private String content;
    private LocalDateTime timestamp;

    // Jackson이 역직렬화를 위해 필요
    @JsonCreator
    public ChatMessage(
            @JsonProperty("sender") String sender,
            @JsonProperty("content") String content,
            @JsonProperty("timestamp") LocalDateTime timestamp
    ) {
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }
}