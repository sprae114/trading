package com.backend.chat.service;

import com.backend.chat.dto.ChatRoomSaveDto;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.repository.ChatRoomRepository;
import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    // todo : test코드 작성
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

    public void delete(String id){
        chatRoomRepository.deleteById(id);
    }

    public void deleteList(List<String> ids){
        chatRoomRepository.deleteAllById(ids);
    }
}
