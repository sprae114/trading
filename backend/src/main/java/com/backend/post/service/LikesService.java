package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.post.model.entity.Likes;
import com.backend.post.repository.LikesRepository;
import com.backend.user.model.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;

    public Likes create(Long postId, Long customerId){
        likesRepository.findByPostIdAndCustomerId(postId, customerId).ifPresent(
                it -> {
                    throw new CustomException(ErrorCode.ALREADY_LIKED_POST,
                            String.format("postId : %s, customerId : %s", postId, customerId));
                }
        );

        return likesRepository.save(Likes.builder()
                .postId(postId)
                .customerId(customerId)
                .build());
    }

    public Likes getOne(Long postId, Long customerId){
        return likesRepository.findByPostIdAndCustomerId(postId, customerId).orElseThrow(
                () -> new CustomException(ErrorCode.LIKES_NOT_FOUND,
                        String.format("postId : %s, customerId : %s", postId, customerId))
        );
    }

    public Page<Likes> getList(Long customerId, Pageable pageable){
        return likesRepository.findAllByCustomerId(customerId, pageable);
    }

    public void deleteOne(Long postId, Long customerId){
        Likes likes = likesRepository.findByPostIdAndCustomerId(postId, customerId).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_MATCH, customerId.toString())
        );

        likesRepository.deleteByPostIdAndCustomerId(likes.getPostId(), customerId);
    }

    public void deleteList(Long customerId, Authentication authentication){
        System.out.println(">>> Role : " + authentication.getAuthorities());
        System.out.println(">>> boolean : " + authentication.getAuthorities().stream()
                .anyMatch(role ->
                        role.getAuthority().equals(Role.ROLE_ADMIN.toString())));

        if (authentication.getAuthorities().stream()
                .anyMatch(role ->
                        role.getAuthority().equals(Role.ROLE_ADMIN.toString()))) { // 관리자인 경우에만 가능하도록 설정
            likesRepository.deleteAllByCustomerId(customerId);
        }
    }
}
