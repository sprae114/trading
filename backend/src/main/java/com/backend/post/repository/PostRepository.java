package com.backend.post.repository;

import com.backend.post.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByCustomerId(Long customerId, Pageable pageable);

    List<Post> findAllByIdIn(List<Long> postId);

}
