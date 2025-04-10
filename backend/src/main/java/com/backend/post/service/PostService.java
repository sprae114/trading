package com.backend.post.service;

import com.backend.chat.service.ChatRoomService;
import com.backend.common.exception.CustomException;
import com.backend.common.exception.ErrorCode;
import com.backend.common.model.RedisRequest;
import com.backend.common.service.RedisService;
import com.backend.common.service.S3Service;
import com.backend.post.dto.request.RegisterPostRequestDto;
import com.backend.post.dto.request.SearchPostRequestDto;
import com.backend.post.dto.request.UpdateRequestDto;
import com.backend.post.dto.response.PostListResponseDto;
import com.backend.post.dto.response.PostResponseDto;
import com.backend.post.dto.response.PostSimpleResponseDto;
import com.backend.post.model.PostCategory;
import com.backend.post.model.entity.Post;
import com.backend.post.repository.PostRepository;
import com.backend.user.model.Role;
import com.backend.user.model.entity.Customer;
import com.backend.user.repository.CustomerRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.backend.post.dto.request.RegisterPostRequestDto.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final RedisService redisService;
    private final S3Service s3Service;
    private final LikesService likesService;
    private final ChatRoomService chatRoomService;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "post:";
    private final CustomerRepository customerRepository;

    /**
     * 중고 거래글 생성
     */
    @Transactional
    public Post create(RegisterPostRequestDto request) throws IOException {
        log.info("Creating new post");

        // 1. 이미지 멉로드
        List<String> uploadKeys = s3Service.uploadFiles(request.imageFiles());
        log.info("Upload files: {}", uploadKeys);

        // 2. 저장
        Post post = postRepository.save(toEntity(request, uploadKeys));

        log.info("Successfully created post with id: {}", post.getId());
        return post;
    }


    /**
     * 해당 게시글에 정보 가져오기
     * redis(조회수 및 좋아요) + s3(이미지) + db
     */
    @Transactional(readOnly = true)
    public PostResponseDto getOne(Long postId) throws JsonProcessingException {
        log.info("Retrieving post with id: {}", postId);

        // 1. id로 post 찾기
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found for id: {}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString());
                });

        // 2. post에 해당하는 이미지 다운
        List<byte[]> downloadFiles = s3Service.downloadFiles(findPost.getImageUrls());
        log.debug("Download files: {}", downloadFiles);

        // 3. redis에서 조회수 가져오기
        Long redisView = getIncreacseRedisView(findPost);

        // 4. redis에서 like 갯수 가져오기
        Long likeCount = likesService.countLikesWithRedis(postId);
        

        log.debug("Download files: {}, view count {} , Like count: {}",downloadFiles, redisView, likeCount);
        log.info("Successfully retrieved post with id: {}", postId);
        return PostResponseDto.from(findPost, redisView, likeCount, downloadFiles);
    }

    /**
     * 해당 게시글에 해당하는 정보 가져오기
     * s3(이미지) + db
     */
    @Transactional(readOnly = true)
    public PostResponseDto getOneForTest(Long postId) throws JsonProcessingException {
        log.info("Retrieving post with id: {}", postId);

        // 1. id로 post 찾기
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found for id: {}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString());
                });

        // 2. post에 해당하는 이미지 다운
        List<byte[]> downloadFiles = s3Service.downloadFiles(findPost.getImageUrls());
        log.debug("Download files: {}", downloadFiles);

        // 3. DB에서 조회수 가져오기
        Long likeCount = likesService.countLikes(postId);

        // 4. db에서 like 갯수 가져오기
        Long redisView = likesService.countLikes(postId);


        log.debug("Download files: {}, view count {} , Like count: {}",downloadFiles, redisView, likeCount);
        log.info("Successfully retrieved post with id: {}", postId);
        return PostResponseDto.from(findPost, redisView, likeCount, downloadFiles);
    }

    /**
     * 해당 게시글에 해당하는 간단한 정보 가져오기
     */
    @Transactional(readOnly = true)
    public PostSimpleResponseDto getPostForChat(String postName) {
        Post post = postRepository.findByTitle(postName)
                .orElseThrow(() -> {
                    log.warn("Post not found for name: {}", postName);
                    return new CustomException(ErrorCode.POST_NOT_FOUND, postName);
                });
        return PostSimpleResponseDto.fromPost(post);
    }


    /**
     * 모든 글 목록 가져오기
     */
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getList(Pageable pageable) {
        log.info("Fetching post list with pageable: {}", pageable);

        Page<PostListResponseDto> result = postRepository.findAll(pageable).map(post -> {
            log.debug("Processing post id: {}", post.getId());

            List<byte[]> downloadFiles = null;
            if (!post.getImageUrls().isEmpty()) {
                log.debug("Downloading images for post id: {}", post.getId());
                downloadFiles = s3Service.downloadFiles(post.getImageUrls());
            }

            try {
                Long redisView = getRedisView(post);
                Long likeCount = likesService.countLikesWithRedis(post.getId());
                log.debug("view count {} , Like count: {}", redisView, likeCount);
                return PostListResponseDto.from(post, redisView, likeCount, downloadFiles);

            } catch (JsonProcessingException e) {
                log.error("Failed to process JSON for post id: {}", post.getId(), e);
                throw new CustomException(ErrorCode.JSON_PASSING_ERROR, e.getMessage());
            }
        });

        log.info("Successfully retrieved post list with size: {}", result.getTotalElements());
        return result;
    }

    /**
     * 내가 좋아요한 글 목록 가져오기
     */
    @Transactional(readOnly = true)
    public List<PostListResponseDto> getPostsByIds(List<Long> postIds) {
        log.info("Starting getPostsByIds posts by ids: {}", postIds);
        List<Post> posts = postRepository.findAllByIdIn(postIds);

        return posts.stream()
                .map(this::mapToPostListResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 내가 좋아요한 글 + 제목 검색 가져오기
     */
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> getPostsByIdsAndTitle(List<Long> postIds, String title, Pageable pageable) {
        log.info("Fetching posts by IDs: {} and title: {}", postIds, title);

        if (postIds == null || postIds.isEmpty()) {
            log.warn("Post IDs list is empty or null");
            return new PageImpl<>(List.of(), pageable, 0); // 빈 페이지 반환
        }

        // 1. 제목 검색
        Page<Post> posts = postRepository.findAllByIdInAndTitleContains(postIds, title, pageable);

        // Post -> PostListResponseDto 변환
        List<PostListResponseDto> result = posts.getContent()
                .stream()
                .map(this::mapToPostListResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(result, pageable, posts.getTotalElements());
    }

    /**
     * 전체 검색
     */
    @Transactional(readOnly = true)
    public Page<PostListResponseDto> searchByTitleAndCategory(SearchPostRequestDto request, Pageable pageable) {
        log.info("Starting search - title: {}, category: {}, pageable: {}",
                request.title(), request.postCategory(), pageable);

        // 1. 입력 검증
        String title = request.title() != null ? request.title().trim() : "";
        PostCategory category = request.postCategory();

        if (title.isEmpty() && category == null) {
            log.warn("Both title and category are empty or null");
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 검색 조건에 따른 쿼리 실행
        Page<Post> posts;

        if (title.isEmpty() && category.equals(PostCategory.ALL)) { //
            log.debug("searching All");
            posts = postRepository.findAll(pageable);

        } else if (category.equals(PostCategory.ALL)) {
            log.debug("Category is ALL, searching by title: {}", title);
            posts = postRepository.searchByTitle(title, pageable);

        } else if (title.isEmpty() && !category.equals(PostCategory.ALL)) {
            log.debug("Title is empty, searching by category: {}", category);
            posts = postRepository.searchByCategory(category, pageable);
        }
        else {
            log.debug("Searching by title: {} and category: {}", title, category);
            posts = postRepository.searchByTitleAndCategory(title, category, pageable);
        }

        // 검색 결과 매핑
        return posts.map(this::mapToPostListResponseDto);
    }


    /**
     * 게시글을 DTO로 변환하는 공통 메서드
     */
    private PostListResponseDto mapToPostListResponseDto(Post post) {
        log.debug("Processing post id: {}", post.getId());

        try {
            // 1. 이미지 다운로드
            List<byte[]> downloadFiles = post.getImageUrls().isEmpty()
                    ? null
                    : s3Service.downloadFiles(post.getImageUrls());

            // 2. redis에서 view 가져오기
            Long redisView = getRedisView(post);

            // 3. redis에서 like 갯수 가져오기
            Long likeCount = likesService.countLikesWithRedis(post.getId());

            log.debug("Post id: {} - Views: {}, Likes: {}", post.getId(), redisView, likeCount);
            return PostListResponseDto.from(post, redisView, likeCount, downloadFiles);
        } catch (JsonProcessingException e) {

            log.error("Failed to process JSON for post id: {}", post.getId(), e);
            throw new CustomException(ErrorCode.JSON_PASSING_ERROR, e.getMessage());
        }
    }

    /**
     * 해당 글 수정하기 (이미지와 함께)
     */
    @Transactional
    public void updateWithImage(UpdateRequestDto request, Authentication authentication) throws IOException {
        log.info("Updating post with id: {}", request.id());

        // 1. post 가져오기
        Post post = postRepository.findById(request.id())
                .orElseThrow(() -> {
                    log.warn("Post not found for id: {}", request.id());
                    return new CustomException(ErrorCode.POST_NOT_FOUND, request.id().toString());
                });

        // 2. 권한 체크
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()));
        boolean isOwner = post.getCustomerName().equals(authentication.getName());

        if (isAdmin || isOwner) {
            log.debug("User {} is authorized to update post id: {}", authentication.getName(), request.id());

            // 3. 이미지 삭제 후, 재업로드
            s3Service.deleteFiles(post.getImageUrls());
            List<String> uploadFilesKey = s3Service.uploadFiles(request.imageFiles());

            // 4. 수정 post 저장
            Post updatePost = post.toBuilder()
                    .title(request.title())
                    .body(request.body())
                    .category(request.category())
                    .imageUrls(uploadFilesKey)
                    .build();

            postRepository.save(updatePost);

            log.info("Successfully updated post with id: {}", updatePost.getId());
        } else {
            log.warn("User {} is not authorized to update post id: {}", authentication.getName(), request.id());
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("post.getCustomerName(): %s, authentication.getName(): %s",
                            post.getCustomerName(), authentication.getName()));
        }
    }

    /**
     * 해당 글 수정하기 (이미지 x)
     */
    @Transactional
    public void update(UpdateRequestDto request, Authentication authentication) throws IOException {
        log.info("Updating post with id: {}", request.id());

        // 1. post 가져오기
        Post post = postRepository.findById(request.id())
                .orElseThrow(() -> {
                    log.warn("Post not found for id: {}", request.id());
                    return new CustomException(ErrorCode.POST_NOT_FOUND, request.id().toString());
                });

        Customer customer = customerRepository.findById(post.getCustomerId())
                .orElseThrow(() -> {
                            log.warn("Customer not found for id: {}", post.getCustomerId());
                            return new CustomException(ErrorCode.USER_NOT_FOUND, post.getCustomerId().toString());
                        }
                );

        // 2. 권한 체크
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()));
        boolean isOwner = customer.getEmail().equals(authentication.getName());

        if (isAdmin || isOwner) {
            log.debug("User {} is authorized to update post id: {}", authentication.getName(), request.id());

            // 4. 수정 post 저장
            Post updatePost = post.toBuilder()
                    .title(request.title())
                    .body(request.body())
                    .category(request.category())
                    .build();

            postRepository.save(updatePost);
            chatRoomService.updateChatRoomAndMessagesByName(post.getTitle(), updatePost.getTitle());

            log.info("Successfully updated post with id: {}", updatePost.getId());
        } else {
            log.warn("User {} is not authorized to update post id: {}", authentication.getName(), request.id());
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("customer.getEmail(): %s, authentication.getName(): %s",
                            customer.getEmail(), authentication.getName()));
        }
    }

    /**
     * 해당 글 삭제하기
     */
    @Transactional
    public void delete(Long postId, Authentication authentication) {
        log.info("Deleting post with id: {}", postId);

        // 1. post 찾기
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Post not found for id: {}", postId);
                    return new CustomException(ErrorCode.POST_NOT_FOUND, postId.toString());
                });

        // 2. 사용자 찾기
        Customer customer = customerRepository.findById(post.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Customer not found for id: {}", post.getCustomerId());
                    return new CustomException(ErrorCode.USER_NOT_FOUND, post.getCustomerId().toString());
                });

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()));
        boolean isOwner = customer.getEmail().equals(authentication.getName());

        if (isAdmin || isOwner) {
            log.debug("User {} is authorized to delete post id: {}", authentication.getName(), postId);

            // 4. 관련된 key, post ,like 삭제
            String cacheKey = CACHE_KEY_PREFIX + post.getId();
            redisService.delete(cacheKey);
            postRepository.deleteById(postId);
            likesService.deleteAllByPostId(postId);

            // 5. 해당 채팅방 삭제
            chatRoomService.deleteChatRoomAndMessagesByName(post.getTitle());

            log.info("Successfully deleted post with id: {}", postId);
        } else {
            log.warn("User {} is not authorized to delete post id: {}", authentication.getName(), postId);
            throw new CustomException(ErrorCode.USER_NOT_AUTHORIZED,
                    String.format("post.getCustomerName(): %s, authentication.getName(): %s",
                            post.getCustomerName(), authentication.getName()));
        }
    }

    /**
     * 선택한 글 목록 삭제
     */
    @Transactional
    public void deleteList(List<Long> postList, Authentication authentication) {
        log.info("Deleting post list: {}", postList);

        if (postList == null || postList.isEmpty()) {
            log.warn("Post list is empty or null");
            return;
        }

        // 1. posts 찾기
        List<Post> posts = postRepository.findAllById(postList);

        if (posts.isEmpty()) {
            log.warn("No posts found for ids: {}", postList);
            throw new CustomException(ErrorCode.POST_NOT_FOUND, "No posts found for provided IDs");
        }

        // 2. 권한 찾기
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals(Role.ROLE_ADMIN.toString()));
        String currentCustomerName = authentication.getName();

        List<Long> authorizedPostIds = posts.stream()
                .filter(post -> {
                    boolean isOwner = post.getCustomerName().equals(currentCustomerName);
                    return isAdmin || isOwner;
                })
                .map(Post::getId)
                .toList();

        if (authorizedPostIds.isEmpty()) {
            log.debug("No authorized posts found for ids: {}", postList);
            return;
        }

        // 3. key, like 삭제
        authorizedPostIds.forEach(postId -> {
            String cacheKey = CACHE_KEY_PREFIX + postId;
            redisService.delete(cacheKey);
            likesService.deleteAllByPostId(postId);
            log.debug("Deleted Redis cache for post id: {}", postId);
        });

        // 4. 이미지 삭제
        List<String> imageUrlsToDelete = posts.stream()
                .filter(post -> authorizedPostIds.contains(post.getId()))
                .flatMap(post -> post.getImageUrls().stream())
                .toList();

        if (!imageUrlsToDelete.isEmpty()) {
            s3Service.deleteFiles(imageUrlsToDelete);
            log.debug("Deleted S3 images: {}", imageUrlsToDelete);
        }

        // 5. 게시글 한번에 삭제
        postRepository.deleteAllByIdIn(authorizedPostIds);
        log.info("Successfully deleted {} posts: {}", authorizedPostIds.size(), authorizedPostIds);
    }

    /**
     * 조회수 redis에서 가져오기
     */
    private Long getRedisView(Post findPost) throws JsonProcessingException {
        log.info("Getting Redis view for post: {}", findPost);

        Long postId = findPost.getId();
        String key = CACHE_KEY_PREFIX + postId;

        Object cachedValue = redisService.get(key);
        RedisRequest redisRequest;

        if (cachedValue == null) {
            redisRequest = RedisRequest.builder()
                    .id(postId)
                    .views(findPost.getViews())
                    .likeCount(likesService.countLikes(postId))
                    .build();
            log.debug("Initialized Redis views from DB for postId: {}", postId);
        } else {
            redisRequest = objectMapper.readValue(cachedValue.toString(), RedisRequest.class);
            log.debug("Retrieved Redis views for postId: {}", postId);
        }

        Long updatedViews = redisRequest.views();
        Long currentLikeCount = redisRequest.likeCount();

        redisRequest = RedisRequest.builder()
                .id(postId)
                .views(updatedViews)
                .likeCount(currentLikeCount)
                .build();

        String redisValue = objectMapper.writeValueAsString(redisRequest);
        redisService.setKeyWithExpiration(key, redisValue, 6000L);
        log.debug("Incremented views for postId: {}, new value: {}", postId, updatedViews);

        return updatedViews;
    }

    /**
     * 조회수 증가시킨 후 redis에서 가져오기
     */
    private Long getIncreacseRedisView(Post findPost) throws JsonProcessingException {
        log.info("Getting Redis view for post: {}", findPost);

        // 1. 해당 키 만들기
        Long postId = findPost.getId();
        String key = CACHE_KEY_PREFIX + postId;

        Object cachedValue = redisService.get(key);
        RedisRequest redisRequest;

        // 2. redis의 키 존재 유무에 따른 조회
        if (cachedValue == null) {
            redisRequest = RedisRequest.builder()
                    .id(postId)
                    .views(findPost.getViews())
                    .likeCount(likesService.countLikes(postId))
                    .build();
            log.debug("Initialized Redis views from DB for postId: {}", postId);
        } else {
            redisRequest = objectMapper.readValue(cachedValue.toString(), RedisRequest.class);
            log.debug("Retrieved Redis views for postId: {}", postId);
        }

        // 3. 조회수 증가시키기
        Long currentViews = redisRequest.views();
        Long updatedViews = currentViews + 1;
        Long currentLikeCount = redisRequest.likeCount();

        redisRequest = RedisRequest.builder()
                .id(postId)
                .views(updatedViews)
                .likeCount(currentLikeCount)
                .build();

        // 4. redis에 Json으로 저장
        String redisValue = objectMapper.writeValueAsString(redisRequest);

        // 5. redis의 저장
        redisService.setKeyWithExpiration(key, redisValue, 6000L);
        log.debug("Incremented views for postId: {}, new value: {}", postId, updatedViews);

        return updatedViews;
    }
}