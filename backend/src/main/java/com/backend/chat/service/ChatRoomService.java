package com.backend.chat.service;

import com.backend.chat.dto.ChatRoomDeleteDto;
import com.backend.chat.dto.ChatRoomSaveDto;
import com.backend.chat.dto.ChatRoomSearchRequestDto;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.repository.ChatMessageRepository;
import com.backend.chat.repository.ChatRoomRepository;
import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.backend.chat.dto.ChatRoomSaveDto.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 새로운 채팅방을 생성합니다.
     */
    public ChatRoom create(ChatRoomSaveDto chatRoomSaveDto) {
        log.info("Creating new chat room with name: {}", chatRoomSaveDto.name());

        chatRoomRepository.findByName(chatRoomSaveDto.name())
                .ifPresent(existingRoom -> {
                    log.warn("Chat room with name {} already exists", existingRoom.getName());
                    throw new CustomException(ErrorCode.ALREADY_CHATROOM, existingRoom.getName());
                });

        return chatRoomRepository.save(toEntity(chatRoomSaveDto));
    }

    /**
     * ID로 단일 채팅방을 조회합니다.
     */
    public ChatRoom getOne(String id) {
        log.info("Fetching chat room with ID: {}", id);
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Chat room not found with ID: {}", id);
                    return new CustomException(ErrorCode.CHATROOM_NOT_FOUND, id);
                });
    }

    /**
     * 채팅방이 존재하면 반환하고, 없으면 새로 생성합니다.
     */
    public ChatRoom makeChatRoom(ChatRoomSaveDto chatRoomSaveDto) {
        log.info("Making chat room for senderId: {} with name: {}",
                chatRoomSaveDto.senderId(), chatRoomSaveDto.name());

        return chatRoomRepository.findBySenderIdAndName(chatRoomSaveDto.senderId(), chatRoomSaveDto.name())
                .orElseGet(() -> {
                    log.info("Chat room not found, creating new one");
                    return chatRoomRepository.save(ChatRoomSaveDto.toEntity(chatRoomSaveDto));
                });
    }

    /**
     * senderId로 채팅방 목록을 페이지네이션하여 조회합니다.
     */
    public Page<ChatRoom> getList(Long senderId, Pageable pageable) {
        log.info("Fetching chat room list for senderId: {}", senderId);
        return chatRoomRepository.findAllBySenderId(senderId, pageable);
    }

    /**
     * 채팅방을 검색 조건에 따라 페이지네이션하여 조회합니다.
     */
    public Page<ChatRoom> searchChatRoomList(ChatRoomSearchRequestDto request, Pageable pageable) {
        log.info("Searching chat rooms for senderId: {} with searchText: {}",
                request.senderId(), request.searchText());

        if (request.searchText().isEmpty()) {
            return chatRoomRepository.findBySenderIdOrReceiverId(request.senderId(), pageable);
        }
        return chatRoomRepository.findBySenderIdOrReceiverIdAndName(request.senderId(), request.searchText(), pageable);
    }

    /**
     * 모든 채팅방 목록을 조회합니다.
     */
    public List<ChatRoom> getAll() {
        log.info("Fetching all chat rooms");
        return chatRoomRepository.findAll();
    }

    /**
     * 채팅방을 삭제합니다. 관리자 권한에 따라 삭제 범위가 달라집니다.
     */
    public void delete(ChatRoomDeleteDto chatRoomDeleteDto) {
        log.info("Deleting chat rooms for user: {} with role: {}",
                chatRoomDeleteDto.loginName(), chatRoomDeleteDto.role());

        List<String> roomIdsToDelete;
        if (chatRoomDeleteDto.role() == Role.ROLE_ADMIN) {
            log.info("Admin deleting rooms: {}", chatRoomDeleteDto.roomInfo());
            roomIdsToDelete = chatRoomDeleteDto.roomInfo().stream()
                    .map(roomInfo -> roomInfo.roomId)
                    .toList();
        } else {
            log.info("Non-admin deleting own rooms");
            roomIdsToDelete = chatRoomDeleteDto.roomInfo().stream()
                    .filter(roomInfo -> roomInfo.createCustomer.equals(chatRoomDeleteDto.loginName()))
                    .map(roomInfo -> roomInfo.roomId)
                    .toList();
        }

        if (!roomIdsToDelete.isEmpty()) {
            log.info("Deleting chat rooms with IDs: {}", roomIdsToDelete);
            chatRoomRepository.deleteAllById(roomIdsToDelete);
        }
    }

    /**
     * 채팅방 이름을 업데이트합니다.
     */
    public void updateChatRoomAndMessagesByName(String oldName, String newName) {
        log.info("Updating chat room name from {} to {}", oldName, newName);
        chatRoomRepository.findByName(oldName).ifPresent(existingRoom -> {
            existingRoom.setName(newName);
            chatRoomRepository.save(existingRoom);
        });
    }

    /**
     * 채팅방과 관련 메시지를 이름으로 삭제합니다.
     */
    public void deleteChatRoomAndMessagesByName(String roomName) {
        log.info("Deleting chat room and messages for roomName: {}", roomName);
        chatRoomRepository.findByName(roomName).ifPresent(findChatRoom -> {
            chatMessageRepository.deleteByRoomId(findChatRoom.getId());
            chatRoomRepository.deleteByName(roomName);
        });
    }
}
