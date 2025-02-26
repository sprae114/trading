package com.backend.chat.service;

import com.backend.chat.dto.ChatRoomDeleteDto;
import com.backend.chat.dto.ChatRoomSaveDto;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.repository.ChatRoomRepository;
import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.user.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.backend.chat.dto.ChatRoomSaveDto.*;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoom create(ChatRoomSaveDto chatRoomSaveDto){

        chatRoomRepository.findByName(chatRoomSaveDto.name())
            .ifPresent(existingRoom -> {
                throw new CustomException(ErrorCode.ALREADY_CHATROOM, existingRoom.getName());
            });

        return chatRoomRepository.save(toEntity(chatRoomSaveDto));
    }

    public ChatRoom getOne(String id){
        return chatRoomRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND, id)
        );
    }


    public ChatRoom makeChatRoom(ChatRoomSaveDto chatRoomSaveDto){
        return chatRoomRepository.findByName(chatRoomSaveDto.name()).orElseGet(
                () -> chatRoomRepository.save(ChatRoomSaveDto.toEntity(chatRoomSaveDto))
        );
    }

    public Page<ChatRoom> getList(Long senderId ,Pageable pageable){
        return chatRoomRepository.findAllBySenderId(senderId, pageable);
    }

    public List<ChatRoom> getAll(){
        return chatRoomRepository.findAll();
    }

    public void delete(ChatRoomDeleteDto chatRoomDeleteDto){
        List<String> roomIdsToDelete;

        if (chatRoomDeleteDto.role() == Role.ROLE_ADMIN) { // 관리자일때
            roomIdsToDelete = chatRoomDeleteDto.roomInfo().stream()
                    .map(roomInfo -> roomInfo.roomId)
                    .toList();
        } else { // 관리자가 아닐때
            roomIdsToDelete = chatRoomDeleteDto.roomInfo().stream()
                    .filter(roomInfo -> roomInfo.createCustomer.equals(chatRoomDeleteDto.loginName()))
                    .map(roomInfo -> roomInfo.roomId)
                    .toList();
        }

        if (!roomIdsToDelete.isEmpty()) {
            chatRoomRepository.deleteAllById(roomIdsToDelete);
        }
    }
}
