package com.backend.common.service;

import com.backend.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msgBody = new String(message.getBody(), StandardCharsets.UTF_8);
            log.info("Received from Redis: {}", msgBody);
            ChatMessage chatMessage = objectMapper.readValue(msgBody, ChatMessage.class);
            messagingTemplate.convertAndSend("/topic/chat-room/" + chatMessage.getRoomId(), chatMessage);
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}
