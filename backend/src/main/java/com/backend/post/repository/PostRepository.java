package com.backend.post.repository;

import com.backend.post.model.PostCategory;
import com.backend.post.model.entity.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAllByCustomerId(Long customerId, Pageable pageable);

    List<Post> findAllByIdIn(List<Long> postIds);

    Page<Post> findAllByIdIn(List<Long> postId, Pageable pageable);

    void deleteAllByIdIn(List<Long> postIds);

    // 제목만으로 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title%")
    Page<Post> searchByTitle(@Param("title") String title, Pageable pageable);

    // 제목과 카테고리로 검색
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title% AND p.category = :category")
    Page<Post> searchByTitleAndCategory(@Param("title") String title,
                                        @Param("category") PostCategory category,
                                        Pageable pageable);

    // 카테고리만으로 검색
    @Query("SELECT p FROM Post p WHERE p.category = :category")
    Page<Post> searchByCategory(@Param("category") PostCategory category, Pageable pageable);

    // 제목과 일치하는 검색
    Optional<Post> findByTitle(String title);

    @Query("SELECT p FROM Post p WHERE p.id IN :postIds AND p.title LIKE %:title%")
    Page<Post> findAllByIdInAndTitleContains(@Param("postIds") List<Long> postIds, @Param("title") String title, Pageable pageable);
}
