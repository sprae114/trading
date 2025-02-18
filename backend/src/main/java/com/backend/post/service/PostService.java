package com.backend.post.service;

import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.backend.post.dto.request.RegisterPostRequestDto.*;


@Transactional
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post create(RegisterPostRequestDto request){
        return postRepository.save(toEntity(request));
    }

    public Post getOne(Long postId){
        return postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString())
        );
    }

    public Page<Post> getList(Pageable pageable){
        return postRepository.findAll(pageable);
    }

    public Post update(UpdateRequestDto request, Authentication authentication) throws Exception{
        Post post = postRepository.findById(request.id()).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND, request.id().toString())
        );

        if(!post.getCustomerName().equals(authentication.getName())){
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("post.getCustomerName() : %s, authentication.getName() : %s",
                            post.getCustomerName(), authentication.getName()));
        }


        Post updatePost = post.toBuilder()
                .title(request.title())
                .body(request.body())
                .category(request.category())
                .imageUrls(request.imageUrls())
                .build();

        return postRepository.save(updatePost);
    }

    public void delete(Long postId, Authentication authentication){
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString())
        );

        if(!post.getCustomerName().equals(authentication.getName())){
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("post.getCustomerName() : %s, authentication.getName() : %s",
                            post.getCustomerName(), authentication.getName()));
        }

        postRepository.deleteById(postId);
    }

    /**
     * likes 이용한 조회
     */
    public List<Post> getPostsByIds(List<Long> postIdList) {
        return postRepository.findAllByIdIn(postIdList);
    }
}
