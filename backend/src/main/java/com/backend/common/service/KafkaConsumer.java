package com.backend.common.service;

import com.backend.chat.model.ChatMessage;
import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.util.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate; // websocket 연결
    private final SseEmitterRegistry emitters; // sse 알람

    @KafkaListener(topics = "chat-topic", groupId = "chat-group")
    public void listen(String message) throws JsonProcessingException {
        ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
        messagingTemplate.convertAndSend("/topic/chat-room/" + chatMessage.getRoomId(), chatMessage);
    }


    @KafkaListener(topics = "alarmTopic")
    public void listenAlarmTopic(String message) {
        log.info("Kafka로부터 알람 메시지 수신: {}", message);
        for (SseEmitter emitter : emitters.getEmitters()) {
            try {
                emitter.send(SseEmitter.event().name("alarm").data(message));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
                throw new CustomException(ErrorCode.SSE_ERROR, e.getMessage());
            }
        }
    }
}