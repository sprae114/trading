package com.backend.post.repository;

import com.backend.post.model.entity.Likes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikesRepository extends JpaRepository<Likes, Long> {

    Optional<Likes> findByPostIdAndCustomerId(Long postId, Long customerId);

    Long countByPostId(Long postId);

    Page<Likes> findAllByCustomerId(Long customerId, Pageable pageable);

    List<Likes> findAllByCustomerId(Long customerId);


    @Modifying
    void deleteByPostIdAndCustomerId(Long postId, Long customerId);

    @Modifying
    void deleteAllByCustomerId(Long customerId);

    @Modifying
    void deleteAllByPostId(Long postId);

    List<Likes> findAllByPostId(Long postId);

    Boolean existsByPostIdAndCustomerId(Long postId, Long customerId);
}
