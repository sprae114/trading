package com.backend.chat.controller;

import com.backend.chat.dto.ChatMessageSaveDto;
import com.backend.chat.dto.ChatRoomSaveDto;
import com.backend.chat.dto.ChatRoomDeleteDto;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.service.ChatMessageService;
import com.backend.chat.service.ChatRoomService;
import com.backend.common.service.KafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;


    /**
     * 사용자 채팅방 리스트 페이징 조회
     */
    @GetMapping("/rooms")
    public Page<ChatRoom> getRooms(@RequestParam Long senderId, Pageable pageable) {
        return chatRoomService.getList(senderId, pageable);
    }


    /**
     * 해당 채팅방 입장
     */
    @GetMapping("/rooms/{roomId}")
    public ChatRoom getRoom(@PathVariable String roomId) {
        return chatRoomService.getOne(roomId);
    }


    /**
     * 채팅하기
     */
    @MessageMapping("/send/{chatRoomId}")
    public void handleMessage(@Valid ChatMessageSaveDto messageDto,
                              @DestinationVariable("chatRoomId") String chatRoomId)
            throws JsonProcessingException {

        ChatMessage chatMessage = chatMessageService.saveOne(messageDto); // 메시지 저장
        kafkaProducer.sendMessage("chat-topic", objectMapper.writeValueAsString(chatMessage)); // kafka 전달
    }

    /**
     *  채팅방 생성
     */
    @PostMapping("/rooms")
    public ChatRoom createRoom(@RequestBody ChatRoomSaveDto chatRoomSaveDto) {
        return chatRoomService.makeChatRoom(chatRoomSaveDto);
    }


    /**
     * 채팅방 삭제
     */
    @DeleteMapping("/rooms")
    public void deleteRoom(@RequestBody ChatRoomDeleteDto customerInfo) {
        chatRoomService.delete(customerInfo);
    }


    /**
     * 해당 채팅방 내용 가져오기
     */
    @GetMapping("/rooms/{roomId}/messages")
    public Slice<ChatMessage> getMessages(
            @PathVariable String roomId,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        return chatMessageService.findAll(pageable, roomId);
    }

    /**
     * 테스트용
     */

    // 전체 채팅방 가져오기
    @GetMapping
    public List<ChatRoom> getAll(){
        return chatRoomService.getAll();
    }


    // 메시지 만들기
    @PostMapping("/rooms/{roomId}/messages")
    public ChatMessage createMessage(@RequestBody ChatMessageSaveDto chatMessageSaveDto,
                                     @PathVariable String roomId) {
        chatMessageService.saveOne(chatMessageSaveDto);
        return ChatMessageSaveDto.toEntity(chatMessageSaveDto);
    }

}