package com.backend.chat.repository;

import com.backend.chat.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findByName(String name);

    Page<ChatRoom> findAllBySenderId(Long senderId, Pageable pageable);
}
