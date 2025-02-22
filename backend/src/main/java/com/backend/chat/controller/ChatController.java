package com.backend.chat.controller;

import com.backend.chat.dto.ChatMessageSaveDto;
import com.backend.chat.dto.ChatRoomSaveDto;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.service.ChatMessageService;
import com.backend.chat.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;


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
    public ChatRoom getRoom(@PathVariable String roomId, ChatRoomSaveDto chatRoomSaveDto) {
        return chatRoomService.makeChatRoom(chatRoomSaveDto);
    }


    /**
     * 채팅하기
     */
    @MessageMapping("/send/{chatRoomId}")
    @SendTo("/topic/chat-room/{chatRoomId}")
    public ChatMessage handleMessage(ChatMessageSaveDto messageDto,
                                     @DestinationVariable("chatRoomId") String chatRoomId) {

        return chatMessageService.saveOne(messageDto);
    }


    /**
     * 특정 채팅방 삭제
     */
    @DeleteMapping("/rooms/{roomId}")
    public void deleteRoom(@PathVariable String roomId) {
        chatRoomService.delete(roomId);
    }


    /**
     * 다수의 채팅방 삭제
     */
    @DeleteMapping("/rooms/delete")
    public void deleteRooms(@RequestBody List<String> roomIds) {
        chatRoomService.deleteList(roomIds);
    }


    // 전체 채팅방 가져오기
    @GetMapping
    public List<ChatRoom> getAll(){
        return chatRoomService.getAll();
    }


    // 채팅방 생성
    @PostMapping("/rooms")
    public ChatRoom createRoom(@RequestBody ChatRoomSaveDto chatRoomSaveDto) {
        return chatRoomService.create(chatRoomSaveDto);
    }


    // 메시지 만들기
    @PostMapping("/rooms/{roomId}/messages")
    public ChatMessage createMessage(@RequestBody ChatMessageSaveDto chatMessageSaveDto,
                                     @PathVariable String roomId) {
        chatMessageService.saveOne(chatMessageSaveDto);
        return ChatMessageSaveDto.toEntity(chatMessageSaveDto);
    }
}