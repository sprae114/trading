package com.backend.chat.service;

import com.backend.chat.dto.ChatMessageSaveDto;
import com.backend.chat.model.ChatMessage;
import com.backend.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.backend.chat.dto.ChatMessageSaveDto.*;


@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveOne(ChatMessageSaveDto request){
        return chatMessageRepository.save(toEntity(request));
    }

    public List<ChatMessage> saveList(List<ChatMessageSaveDto> request){
        return chatMessageRepository.saveAll(
                request.stream()
                    .map(ChatMessageSaveDto::toEntity)
                    .toList());
    }

    public Slice<ChatMessage> findAll(Pageable pageable, String chatRoomId){
        return chatMessageRepository.findAllByRoomId(chatRoomId, pageable);
    }
}
