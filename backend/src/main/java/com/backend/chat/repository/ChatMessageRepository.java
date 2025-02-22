package com.backend.chat.repository;

import com.backend.chat.model.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    Slice<ChatMessage> findAllByRoomId(String roomId, Pageable pageable);

}
