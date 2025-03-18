package com.backend.post.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Builder
@Getter
@Entity
@Table(
        name = "likes",
        indexes = {
                @Index(name = "idx_post_id_customer_id", columnList = "postId, customerId"), // 복합 인덱스
                @Index(name = "idx_customer_id", columnList = "customerId") // 단일 인덱스
        }
)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // @CreatedDate를 위해 추가
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
