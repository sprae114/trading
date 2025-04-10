package com.backend.chat.repository;

import com.backend.chat.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {

    Optional<ChatRoom> findByName(String name);

    Page<ChatRoom> findAllBySenderId(Long senderId, Pageable pageable);

    Optional<ChatRoom> findBySenderIdAndName(Long senderId, String name);

    @Query("{$or: [{senderId: ?0}, {receiverId: ?0}]}")
    Page<ChatRoom> findBySenderIdOrReceiverId(Long id, Pageable pageable);

    @Query("{$and: [{$or: [{senderId: ?0}, {receiverId: ?0}]}, {name: ?1}]}")
    Page<ChatRoom> findBySenderIdOrReceiverIdAndName(Long id, String name, Pageable pageable);

    void deleteByName(String name);
}
