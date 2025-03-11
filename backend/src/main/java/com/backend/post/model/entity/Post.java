package com.backend.post.model.entity;

import com.backend.post.model.PostCategory;
import com.backend.common.model.BaseEntity;
import com.backend.post.model.TradeStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.List;

@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(callSuper = true)
public class Post extends BaseEntity {

    @Id
    @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    private Long customerId;

    private String customerName;

    @Enumerated(EnumType.STRING)
    private PostCategory category;

    @Enumerated(EnumType.STRING)
    private TradeStatus tradeStatus;

    private Long views;

    @Type(JsonType.class)
    @Column(name = "image_urls", columnDefinition = "json")
    private List<String> imageUrls;
}
