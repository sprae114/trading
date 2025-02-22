package com.backend.chat.service;

import com.backend.chat.dto.ChatMessageSaveDto;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ChatMessageServiceTest {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    private ChatMessageSaveDto request1;


    @BeforeEach
    void setUp() {
        chatMessageRepository.deleteAll();

        request1 = ChatMessageSaveDto.builder()
                .roomId("testRoom")
                .sender("testUser1")
                .content("test message 1")
                .build();

        chatMessageService.saveOne(request1);

        ChatMessageSaveDto request2 = ChatMessageSaveDto.builder()
                .roomId("testRoom")
                .sender("testUser2")
                .content("test message 2")
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessageSaveDto request3 = ChatMessageSaveDto.builder()
                .roomId("testRoom")
                .sender("testUser3")
                .content("test message 3")
                .timestamp(LocalDateTime.now().plusSeconds(1))
                .build();

        chatMessageService.saveList(List.of(request2, request3));
    }

    @Test
    @DisplayName("메시지 한개 저장 : 성공")
    void saveMessageSuccess() {
        // When

        // Then
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertEquals(3, messages.size());
        assertEquals("testRoom", messages.get(0).getRoomId());
        assertEquals("testUser1", messages.get(0).getSender());
        assertEquals("test message 1", messages.get(0).getContent());
    }


    @Test
    @DisplayName("메시지 여러개 저장 :  성공")
    void saveMessagesSuccess() {
        // Then
        List<ChatMessage> messages = chatMessageRepository.findAll();
        assertEquals(3, messages.size());
        assertEquals("testRoom", messages.get(2).getRoomId());
        assertEquals("testUser3", messages.get(2).getSender());
        assertEquals("test message 3", messages.get(2).getContent());
    }


    @Test
    @DisplayName("메시지 목록 조회 : 성공")
    void findAllMessagesSuccess() {
        // Give
        Pageable pageable = PageRequest.of(0, 10, Sort.by("timestamp").descending());

        // When
        Slice<ChatMessage> result = chatMessageService.findAll(pageable, "testRoom");

        // Then
        assertEquals(3, result.getContent().size());
        assertEquals("testUser3", result.getContent().get(0).getSender()); // 최신 메시지가 먼저 나와야 함
    }

    @Test
    @DisplayName("메시지 목록 조회 : 실패(chatRoomId 존재X)")
    void findAllMessagesFail_InvalidChatRoomId() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When, Then
        Slice<ChatMessage> result = chatMessageService.findAll(pageable, "no");
        assertEquals(0, result.getContent().size());
    }
}