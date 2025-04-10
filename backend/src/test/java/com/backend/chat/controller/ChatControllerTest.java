package com.backend.chat.controller;

import com.backend.chat.dto.*;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.repository.ChatMessageRepository;
import com.backend.chat.repository.ChatRoomRepository;
import com.backend.chat.service.ChatMessageService;
import com.backend.chat.service.ChatRoomService;
import com.backend.post.dto.response.PostSimpleResponseDto;
import com.backend.post.service.PostService;
import com.backend.user.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatRoomService chatRoomService;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private ChatRoom chatRoom;

    private ChatRoomSaveDto chatRoomSaveDto;

    @BeforeEach
    public void setUp() throws Exception {
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();

        chatRoom = makeChatRoom("testRoom1", 1L);
        when(postService.getPostForChat("testRoom1")).thenReturn(PostSimpleResponseDto.builder().title("testRoom1").build());

        makeChatMessage(chatRoom.getId(), "testMessage1");
        makeChatMessage(chatRoom.getId(), "testMessage2");
    }

    @Test
    @DisplayName("해당 유저의 채팅방 목록 조회 - 성공")
    @WithMockUser(username = "User1")
    void getRooms_success() throws Exception {
        // Given
        ChatRoomSearchRequestDto requestDto = new ChatRoomSearchRequestDto(1L, "");

        // When & Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .param("page", "0")
                        .param("size", "7")
                        .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.[*].id").isNotEmpty())
                .andExpect(jsonPath("$.content.[*].name").isNotEmpty())
                .andExpect(jsonPath("$.content.[*].createdAt").isNotEmpty());
    }


    @Test
    @DisplayName("해당 유저의 채팅방 목록 조회 - 실패(로그인 X)")
    void getRooms_fail_not_Customer() throws Exception {
        // Given
        ChatRoomSearchRequestDto requestDto = new ChatRoomSearchRequestDto(1L, null);

        // When & Then
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .param("page", "0")
                        .param("size", "7")
                        .param("sort", "id,asc"))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("채팅방 입장 - 성공")
    @WithMockUser(username = "User1")
    void getRoom_success() throws Exception {
        mockMvc.perform(get("/api/chat/rooms/{roomId}", chatRoom.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatRoom.id").isNotEmpty())
                .andExpect(jsonPath("$.chatRoom.name").value("testRoom1"))
                .andExpect(jsonPath("$.postSimpleResponseDto").exists()) // postSimpleResponseDto 필드 존재 여부 확인
                .andExpect(jsonPath("$.postSimpleResponseDto.title").value("testRoom1")); // PostSimpleResponseDto의 title 검증
    }

    @Test
    @DisplayName("채팅방 입장 - 실패(로그인 X)")
    void getRoom_fail_not_Customer() throws Exception {
        mockMvc.perform(get("/api/chat/rooms/{roomId}", chatRoom.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("채팅방 입장 - 실패(채팅방 존재 X)")
    @WithMockUser(username = "User1")
    void getRoom_fail_not_ExistChatRoom() throws Exception {
        mockMvc.perform(get("/api/chat/rooms/{roomId}", "9999999"))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("채팅방 생성 - 성공(기존 채팅방 리턴)")
    @WithMockUser(username = "User1")
    void createRoom_success_already_room() throws Exception {
        mockMvc.perform(post("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomSaveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(chatRoomSaveDto.name()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("채팅방 생성 - 성공(새로운 채팅방)")
    @WithMockUser(username = "User1")
    void createRoom_success_new_room() throws Exception {
        ChatRoomSaveDto saveDto = ChatRoomSaveDto.builder()
                .name("newRoom")
                .senderId(2L)
                .sender("User1")
                .receiver("User2")
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(saveDto.name()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    @DisplayName("채팅방 생성 - 실패(로그인 X)")
    void createRoom_fail_not_Customer() throws Exception {
        mockMvc.perform(post("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomSaveDto)))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("채팅방 삭제 - 성공(해당 유저)")
    @WithMockUser(username = "User1")
    void deleteRoom_success() throws Exception {
        ChatRoomDeleteDto chatRoomDeleteDto = createChatRoomDeleteDto("User1", Role.ROLE_CUSTOMER);

        mockMvc.perform(delete("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomDeleteDto)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("채팅방 삭제 - 성공(관리자)")
    @WithMockUser(username = "User1")
    void deleteRoom_success_admin() throws Exception {
        ChatRoomDeleteDto chatRoomDeleteDto = createChatRoomDeleteDto("Admin", Role.ROLE_ADMIN);

        mockMvc.perform(delete("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomDeleteDto)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("채팅방 삭제 - 성공(해당 유저)")
    @WithMockUser(username = "User1")
    void deleteRoom_success_other_customer() throws Exception {
        ChatRoomDeleteDto chatRoomDeleteDto = createChatRoomDeleteDto("User999", Role.ROLE_CUSTOMER);

        mockMvc.perform(delete("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomDeleteDto)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("채팅방 삭제 - 실패(로그인 X)")
    void deleteRoom_fail() throws Exception {
        ChatRoomDeleteDto chatRoomDeleteDto = createChatRoomDeleteDto("User999", Role.ROLE_CUSTOMER);

        mockMvc.perform(delete("/api/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRoomDeleteDto)))
                .andExpect(status().isForbidden());
    }


    @Test
    @DisplayName("채팅방 내용 가져오기 - 성공")
    @WithMockUser(username = "User1")
    void get_room_message_success() throws Exception {
        mockMvc.perform(get("/api/chat/rooms/{roomId}/messages", chatRoom.getId())
                        .param("page", "0"))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("채팅방 내용 가져오기 - 실패(로그인 x)")
    void get_room_message_fail() throws Exception {
        mockMvc.perform(get("/api/chat/rooms/{roomId}/messages", chatRoom.getId())
                        .param("page", "0"))
                .andExpect(status().isForbidden());
    }


    private ChatRoom makeChatRoom(String roomName, Long sendId) {
        chatRoomSaveDto = ChatRoomSaveDto.builder()
                .name(roomName)
                .senderId(sendId)
                .sender("User1")
                .receiver("User2")
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomService.create(chatRoomSaveDto);
    }

    private ChatMessage makeChatMessage(String roomId, String content) {
        ChatMessageSaveDto saveDto = ChatMessageSaveDto.builder()
                .roomId(roomId)
                .sender("User1")
                .content(content)
                .build();

        return chatMessageService.saveOne(saveDto);
    }

    private ChatRoomDeleteDto createChatRoomDeleteDto(String name, Role role) {

        return ChatRoomDeleteDto
                .builder()
                .loginName(name)
                .role(role)
                .roomInfo(List.of(
                        RoomInfo.builder()
                                .roomId(chatRoom.getId())
                                .createCustomer("testUser1")
                                .build())
                )
                .build();
    }
}