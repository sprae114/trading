INSERT INTO customer (customer_id, email, pwd, name, created_at) VALUES
                                                                     (1, 'user1@example.com', 'password1', 'User One', '2025-03-10 10:00:00'),
                                                                     (2, 'user2@example.com', 'password2', 'User Two', '2025-03-10 10:05:00'),
                                                                     (3, 'user3@example.com', 'password3', 'User Three', '2025-03-10 10:10:00'),
                                                                     (4, 'admin@example.com', 'adminpass', 'Admin User', '2025-03-10 10:15:00'),
                                                                     (5, 'user5@example.com', 'password5', 'User Five', '2025-03-10 10:20:00');

INSERT INTO authority (id, customer_id, role, created_at) VALUES
                                                              (1, 1, 'ROLE_CUSTOMER', '2025-03-10 10:00:00'),
                                                              (2, 2, 'ROLE_CUSTOMER', '2025-03-10 10:05:00'),
                                                              (3, 3, 'ROLE_CUSTOMER', '2025-03-10 10:10:00'),
                                                              (4, 4, 'ROLE_ADMIN', '2025-03-10 10:15:00'),
                                                              (5, 5, 'ROLE_CUSTOMER', '2025-03-10 10:20:00');


INSERT INTO post (post_id, title, body, customer_id, customer_name, category, trade_status, image_urls, created_at, views) VALUES
                                                                                                                               (1, '맛있는 피자', '최고의 피자 추천합니다!', 1, 'User One', 'FOOD', 'SALE', '["https://example.com/pizza1.jpg"]', '2025-03-10 11:00:00', 10),
                                                                                                                               (2, '여행 후기', '제주도 여행 다녀왔어요', 2, 'User Two', 'TRAVEL', 'SOLD_OUT', '["https://example.com/jeju1.jpg", "https://example.com/jeju2.jpg"]', '2025-03-10 11:05:00', 25),
                                                                                                                               (3, '최신 패션', '봄맞이 옷 추천', 3, 'User Three', 'FASHION', 'RESERVED', '["https://example.com/fashion1.jpg"]', '2025-03-10 11:10:00', 15),
                                                                                                                               (4, '강아지 사진', '우리 강아지 귀여워요', 5, 'User Five', 'ANIMAL', 'SALE', '["https://example.com/dog1.jpg"]', '2025-03-10 11:15:00', 30),
                                                                                                                               (5, '일상 기록', '오늘의 점심', 1, 'User One', 'DAILY', 'SALE', '["https://example.com/lunch1.jpg"]', '2025-03-10 11:20:00', 8),
                                                                                                                               (6, '음식 추천', '맛있는 라멘집', 2, 'User Two', 'FOOD', 'SOLD_OUT', '["https://example.com/ramen1.jpg"]', '2025-03-10 11:25:00', 20),
                                                                                                                               (7, '패션 아이템', '가방 팝니다', 3, 'User Three', 'FASHION', 'SALE', '["https://example.com/bag1.jpg"]', '2025-03-10 11:30:00', 12),
                                                                                                                               (8, '고양이 사진', '고양이 너무 귀여움', 5, 'User Five', 'ANIMAL', 'RESERVED', '["https://example.com/cat1.jpg"]', '2025-03-10 11:35:00', 35),
                                                                                                                               (9, '여행지 추천', '부산 여행 후기', 1, 'User One', 'TRAVEL', 'SALE', '["https://example.com/busan1.jpg"]', '2025-03-10 11:40:00', 18),
                                                                                                                               (10, '일상 이야기', '주말 나들이', 2, 'User Two', 'DAILY', 'HIDDEN', '["https://example.com/weekend1.jpg"]', '2025-03-10 11:45:00', 5),
                                                                                                                               (11, '음식 리뷰', '스시 맛집', 3, 'User Three', 'FOOD', 'SALE', '["https://example.com/sushi1.jpg"]', '2025-03-10 11:50:00', 22),
                                                                                                                               (12, '패션 룩북', '여름 코디', 5, 'User Five', 'FASHION', 'CANCELED', '["https://example.com/summer1.jpg"]', '2025-03-10 11:55:00', 10),
                                                                                                                               (13, '강아지 소식', '강아지 산책', 1, 'User One', 'ANIMAL', 'SALE', '["https://example.com/dog2.jpg"]', '2025-03-10 12:00:00', 28),
                                                                                                                               (14, '여행 기록', '서울 나들이', 2, 'User Two', 'TRAVEL', 'REPORTED', '["https://example.com/seoul1.jpg"]', '2025-03-10 12:05:00', 15),
                                                                                                                               (15, '일상 사진', '저녁 풍경', 3, 'User Three', 'DAILY', 'SALE', '["https://example.com/evening1.jpg"]', '2025-03-10 12:10:00', 7),
                                                                                                                               (16, '음식 후기', '카페 탐방', 5, 'User Five', 'FOOD', 'RESERVED', '["https://example.com/cafe1.jpg"]', '2025-03-10 12:15:00', 19),
                                                                                                                               (17, '패션 팁', '겨울 옷 추천', 1, 'User One', 'FASHION', 'SALE', '["https://example.com/winter1.jpg"]', '2025-03-10 12:20:00', 14),
                                                                                                                               (18, '고양이 일상', '고양이 낮잠', 2, 'User Two', 'ANIMAL', 'SALE', '["https://example.com/cat2.jpg"]', '2025-03-10 12:25:00', 32),
                                                                                                                               (19, '여행 사진', '강원도 여행', 3, 'User Three', 'TRAVEL', 'SOLD_OUT', '["https://example.com/gangwon1.jpg"]', '2025-03-10 12:30:00', 25),
                                                                                                                               (20, '일상 기록', '아침 루틴', 5, 'User Five', 'DAILY', 'SALE', '["https://example.com/morning1.jpg"]', '2025-03-10 12:35:00', 9),
                                                                                                                               (21, '음식 추천', '디저트 카페', 1, 'User One', 'FOOD', 'SALE', '["https://example.com/dessert1.jpg"]', '2025-03-10 12:40:00', 23),
                                                                                                                               (22, '패션 아이템', '신발 팝니다', 2, 'User Two', 'FASHION', 'RESERVED', '["https://example.com/shoes1.jpg"]', '2025-03-10 12:45:00', 11);


INSERT INTO likes (likes_id, customer_id, post_id, created_at) VALUES
                                                                   (1, 1, 2, '2025-03-10 13:00:00'), -- User One이 User Two의 게시글 좋아요
                                                                   (2, 2, 1, '2025-03-10 13:05:00'), -- User Two가 User One의 게시글 좋아요
                                                                   (3, 3, 4, '2025-03-10 13:10:00'), -- User Three가 User Five의 게시글 좋아요
                                                                   (4, 5, 3, '2025-03-10 13:15:00'), -- User Five가 User Three의 게시글 좋아요
                                                                   (5, 1, 6, '2025-03-10 13:20:00'), -- User One이 User Two의 게시글 좋아요
                                                                   (6, 2, 5, '2025-03-10 13:25:00'), -- User Two가 User One의 게시글 좋아요
                                                                   (7, 3, 8, '2025-03-10 13:30:00'); -- User Three가 User Five의 게시글 좋아요