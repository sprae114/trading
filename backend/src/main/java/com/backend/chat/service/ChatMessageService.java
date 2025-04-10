package com.backend.chat.service;

import com.backend.chat.dto.ChatMessageSaveDto;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.backend.chat.dto.ChatMessageSaveDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveOne(ChatMessageSaveDto request){
        log.info("save one chat message : {}", request.content());
        return chatMessageRepository.save(toEntity(request));
    }

    public List<ChatMessage> saveList(List<ChatMessageSaveDto> request){
        log.info("save ChatMessage - List Size : {}", request.size());
        return chatMessageRepository.saveAll(
                request.stream()
                    .map(ChatMessageSaveDto::toEntity)
                    .toList());
    }

    public Page<ChatMessage> findAll(Pageable pageable, String chatRoomId){
        log.info("findAll chatRoomId: {}", chatRoomId);
        return chatMessageRepository.findAllByRoomId(chatRoomId, pageable);
    }
}
