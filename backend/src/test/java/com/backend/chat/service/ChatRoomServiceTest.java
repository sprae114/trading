package com.backend.chat.service;

import com.backend.chat.dto.ChatRoomDeleteDto;
import com.backend.chat.dto.ChatRoomSaveDto;
import com.backend.chat.dto.RoomInfo;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.repository.ChatRoomRepository;
import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ChatRoomServiceTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        chatRoomRepository.deleteAll();

        chatRoom = createRoom("testRoom1", 1L);
    }


    @Test
    @DisplayName("채팅방 생성 : 생성")
    public void createRoomSuccess() {
        //given
        createRoom("testRoom2", 1L);

        //when
        List<ChatRoom> result = chatRoomRepository.findAll();

        //then
        assertEquals(2, result.size());
        assertEquals("testRoom1", result.get(0).getName());
        assertEquals("testRoom2", result.get(1).getName());
    }

    @Test
    @DisplayName("채팅방 생성 : 실패(존재하는 채팅방)")
    public void createRoomFail_ExistChatRoom() {
        //then
        CustomException exception = assertThrows(CustomException.class, () -> createRoom("testRoom1", 1L));
        assertEquals(ErrorCode.ALREADY_CHATROOM, exception.getErrorCode());
    }

    @Test
    @DisplayName("해당 채팅방 조회 : 성공")
    public void findChatRoomSuccess() {
        //when
        ChatRoom result = chatRoomService.getOne(chatRoom.getId());

        //then
        assertEquals(result.getId(), chatRoom.getId());
        assertEquals(result.getName(), chatRoom.getName());
    }

    @Test
    @DisplayName("해당 채팅방 조회 : 실패(채팅방X)")
    public void findChatRoomFail_NotExistChatRoom() {
        //then
        CustomException exception = assertThrows(CustomException.class, () -> chatRoomService.getOne("999"));
        assertEquals(ErrorCode.CHATROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("채팅방 리스트 조회 : 성공")
    public void findAllChatRoomSuccess() {
        //given
        createRoom("testRoom2", 2L);
        createRoom("testRoom3", 1L);
        Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<ChatRoom> result = chatRoomService.getList(1L, pageable);

        //then
        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals("testRoom1", result.getContent().get(0).getName());
        assertEquals("testRoom3", result.getContent().get(1).getName());
    }

    @Test
    @DisplayName("채팅방 삭제 : 성공(관리자)")
    public void deleteChatRoomSuccess() {
        //given
        ChatRoom chatRoom2 = createRoom("testRoom2", 2L);

        List<RoomInfo> chatRoomInfoList = createRoomList(chatRoom2);

        ChatRoomDeleteDto chatRoomDeleteRequest = ChatRoomDeleteDto
                .builder()
                .loginName("testUser000")
                .role(Role.ROLE_ADMIN)
                .roomInfo(chatRoomInfoList)
                .build();

        //when
        chatRoomService.delete(chatRoomDeleteRequest);

        //ten
        List<ChatRoom> result = chatRoomRepository.findAll();
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("채팅방 삭제 : 성공(해당 유저)")
    public void deleteChatRoomSuccessCustomer() {
        //given
        ChatRoom chatRoom2 = createRoom("testRoom2", 2L);

        List<RoomInfo> chatRoomInfoList = createRoomList(chatRoom2);

        ChatRoomDeleteDto chatRoomDeleteRequest = ChatRoomDeleteDto
                .builder()
                .loginName("testUser1")
                .role(Role.ROLE_CUSTOMER)
                .roomInfo(chatRoomInfoList)
                .build();

        //when
        chatRoomService.delete(chatRoomDeleteRequest);

        //ten
        List<ChatRoom> result = chatRoomRepository.findAll();
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("채팅방 삭제 : 성공(다른 유저)")
    public void deleteChatRoomSuccessNotCustomer() {
        //given
        ChatRoom chatRoom2 = createRoom("testRoom2", 2L);
        List<RoomInfo> chatRoomInfoList = createRoomList(chatRoom2);

        ChatRoomDeleteDto chatRoomDeleteRequest = ChatRoomDeleteDto
                .builder()
                .loginName("testUser999")
                .role(Role.ROLE_CUSTOMER)
                .roomInfo(chatRoomInfoList)
                .build();

        //when
        chatRoomService.delete(chatRoomDeleteRequest);

        //ten
        List<ChatRoom> result = chatRoomRepository.findAll();
        assertEquals(2, result.size());
    }

    
    @Test
    @DisplayName("채팅방 만들어서 새로운 채팅 시작 : 성공")
    public void startNewChatRoomSuccess() {
        makeRoom("testRoom2", 2L);

        //when
        List<ChatRoom> result = chatRoomRepository.findAll();

        //then
        assertEquals(2, result.size());
        assertEquals("testRoom1", result.get(0).getName());
        assertEquals("testRoom2", result.get(1).getName());
    }

    @Test
    @DisplayName("채팅방 만들어서 새로운 채팅 시작 : 성공(이미 존재하면 방ID 리턴)")
    public void startNewChatRoomSuccessExistChatRoom() {
        makeRoom("testRoom1", 1L);

        //when
        List<ChatRoom> result = chatRoomRepository.findAll();

        //then
        assertEquals(1, result.size());
        assertEquals("testRoom1", result.get(0).getName());
        assertEquals(chatRoom.getId(), result.get(0).getId());
    }


    private ChatRoom createRoom(String roomName, Long sendId) {
        ChatRoomSaveDto chatRoom = ChatRoomSaveDto.builder()
                .name(roomName)
                .senderId(sendId)
                .sender("testUser1")
                .receiver("testUser2")
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomService.create(chatRoom);
    }


    private ChatRoom makeRoom(String roomName, Long sendId) {
        ChatRoomSaveDto chatRoom = ChatRoomSaveDto.builder()
                .name(roomName)
                .senderId(sendId)
                .sender("testUser1")
                .receiver("testUser2")
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomService.makeChatRoom(chatRoom);
    }

    private List<RoomInfo> createRoomList(ChatRoom chatRoom2) {
        return List.of(
                RoomInfo.builder()
                        .roomId(chatRoom.getId())
                        .createCustomer("testUser1")
                        .build(),
                RoomInfo.builder()
                        .roomId(chatRoom2.getId())
                        .createCustomer("testUser1")
                        .build());
    }
}