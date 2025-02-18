package com.backend.post.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;


@Builder
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Likes {

    @Id
    @Column(name = "likes_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private Long postId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
