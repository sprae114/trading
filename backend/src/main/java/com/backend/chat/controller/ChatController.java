package com.backend.chat.controller;

import com.backend.chat.dto.*;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.service.ChatMessageService;
import com.backend.chat.service.ChatRoomService;
import com.backend.common.service.KafkaProducer;
import com.backend.post.dto.response.PostSimpleResponseDto;
import com.backend.post.service.PostService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final KafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;
    private final PostService postService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public Page<ChatRoom> getRooms(@RequestBody ChatRoomSearchRequestDto requestDto,
                                   @PageableDefault(page = 0, size = 7, sort = "id", direction = Sort.Direction.ASC)
                                   Pageable pageable) {

        return chatRoomService.searchChatRoomList(requestDto, pageable);
    }


    /**
     * 해당 채팅방 입장
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/rooms/{roomId}")
    public ChatRoomInfoResponseDto getRoom(@PathVariable String roomId) {
        ChatRoom findChatRoom = chatRoomService.getOne(roomId);
        PostSimpleResponseDto postForChat = postService.getPostForChat(findChatRoom.getName());

        return ChatRoomInfoResponseDto.builder()
                .chatRoom(findChatRoom)
                .postSimpleResponseDto(postForChat)
                .build();
    }


    /**
     * 채팅하기
     */
    @MessageMapping("/send/{chatRoomId}")
    public void handleMessage(@Valid ChatMessageSaveDto messageDto,
                              @DestinationVariable("chatRoomId") String chatRoomId)
            throws JsonProcessingException {
        ChatMessageSaveDto saveDto = messageDto.toBuilder().timestamp(LocalDateTime.now()).build();

        ChatMessage chatMessage = chatMessageService.saveOne(saveDto); // 메시지 저장
        kafkaProducer.sendMessage("chat-topic", objectMapper.writeValueAsString(chatMessage)); // kafka 전달
    }

    /**
     *  채팅방 생성
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/rooms")
    public ChatRoom createRoom(@RequestBody ChatRoomSaveDto chatRoomSaveDto) {
        return chatRoomService.makeChatRoom(chatRoomSaveDto);
    }


    /**
     * 채팅방 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/rooms")
    public void deleteRoom(@RequestBody ChatRoomDeleteDto customerInfo) {
        chatRoomService.delete(customerInfo);
    }


    /**
     * 해당 채팅방 내용 가져오기
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/rooms/{roomId}/messages")
    public Page<ChatMessage> getMessages(
            @PathVariable String roomId,
            @PageableDefault(sort = "timestamp", page = 0, size = 10, direction = Sort.Direction.DESC) Pageable pageable) {
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