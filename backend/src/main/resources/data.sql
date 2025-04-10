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


INSERT INTO post (post_id, title, body, customer_id, customer_name, category, trade_status, image_urls, created_at, views, price) VALUES
                                                                                                                                      (1, '피자 오븐 팝니다', '거의 새것, 피자 만들 때만 썼어요. 직거래 선호.', 1, 'User One', 'GENERAL', 'SALE', '["mac1.jpg", "mac2.jpg"]', '2025-03-10 11:00:00', 10, 150000),
                                                                                                                                      (2, '제주도 기념품 세트', '여행 다녀와서 남은 기념품 팝니다. 상태 좋아요.', 2, 'User Two', 'HOBBY', 'SOLD_OUT', '["metrix1.jpg"]', '2025-03-10 11:05:00', 25, 30000),
                                                                                                                                      (3, '봄 옷 세트', '새 옷, 한 번도 안 입었어요. 택배 가능.', 3, 'User Three', 'FASHION', 'RESERVED', '["cloth1.jpg", "cloth2.jpg"]', '2025-03-10 11:10:00', 15, 50000),
                                                                                                                                      (4, '강아지 옷 팝니다', '우리 강아지한테 안 맞아서 판매. 깨끗합니다.', 5, 'User Five', 'HOBBY', 'SALE', '["bicycle1.jpg", "bicycle2.jpg"]', '2025-03-10 11:15:00', 30, 20000),
                                                                                                                                      (5, '도시락 통 판매', '점심 싸 먹던 거 정리합니다. 사용감 거의 없음.', 1, 'User One', 'GENERAL', 'SALE', '["bag1.jpg", "bag2.jpg"]', '2025-03-10 11:20:00', 8, 10000),
                                                                                                                                      (6, '라멘 그릇 세트', '집에서 라멘 먹으려고 샀는데 안 써요. 새것.', 2, 'User Two', 'GENERAL', 'SOLD_OUT', '["mac1.jpg", "mac2.jpg"]', '2025-03-10 11:25:00', 20, 25000),
                                                                                                                                      (7, '가죽 가방 팝니다', '가방 정리 중, 상태 좋고 저렴하게 드려요.', 3, 'User Three', 'FASHION', 'SALE', '["metrix1.jpg"]', '2025-03-10 11:30:00', 12, 80000),
                                                                                                                                      (8, '고양이 장난감', '고양이가 안 놀아서 팝니다. 새것 수준.', 5, 'User Five', 'HOBBY', 'RESERVED', '["cloth1.jpg", "cloth2.jpg"]', '2025-03-10 11:35:00', 35, 15000),
                                                                                                                                      (9, '부산 여행 가방', '여행용으로 쓰던 가방, 깨끗해요. 직거래만.', 1, 'User One', 'HOBBY', 'SALE', '["bicycle1.jpg", "bicycle2.jpg"]', '2025-03-10 11:40:00', 18, 45000),
                                                                                                                                      (10, '주말용 캠핑 용품', '나들이 갔을 때 썼던 용품 팝니다. 숨김 처리.', 2, 'User Two', 'GENERAL', 'HIDDEN', '["bag1.jpg", "bag2.jpg"]', '2025-03-10 11:45:00', 5, 120000),
                                                                                                                                      (11, '스시 접시 세트', '집에서 스시 파티용으로 샀어요. 거의 새것.', 3, 'User Three', 'GENERAL', 'SALE', '["mac1.jpg", "mac2.jpg"]', '2025-03-10 11:50:00', 22, 35000),
                                                                                                                                      (12, '여름 옷 정리', '코디용 옷, 입을 일 없어서 팝니다. 숨김 처리.', 5, 'User Five', 'FASHION', 'HIDDEN', '["metrix1.jpg"]', '2025-03-10 11:55:00', 10, 40000),
                                                                                                                                      (13, '강아지 목줄', '산책용 목줄, 새거예요. 직거래 가능.', 1, 'User One', 'HOBBY', 'SALE', '["cloth1.jpg", "cloth2.jpg"]', '2025-03-10 12:00:00', 28, 18000),
                                                                                                                                      (14, '서울 여행용 백팩', '나들이용 백팩, 사용감 적음. 숨김 처리.', 2, 'User Two', 'HOBBY', 'HIDDEN', '["bicycle1.jpg", "bicycle2.jpg"]', '2025-03-10 12:05:00', 15, 60000),
                                                                                                                                      (15, '저녁 식기 세트', '저녁에 쓰던 접시, 정리합니다. 상태 좋아요.', 3, 'User Three', 'GENERAL', 'SALE', '["bag1.jpg", "bag2.jpg"]', '2025-03-10 12:10:00', 7, 30000),
                                                                                                                                      (16, '카페 컵 세트', '카페 느낌 내는 컵, 거의 안 썼어요.', 5, 'User Five', 'GENERAL', 'RESERVED', '["mac1.jpg", "mac2.jpg"]', '2025-03-10 12:15:00', 19, 20000),
                                                                                                                                      (17, '겨울 코트', '따뜻한 코트, 한 시즌만 입었어요.', 1, 'User One', 'FASHION', 'SALE', '["metrix1.jpg"]', '2025-03-10 12:20:00', 14, 90000),
                                                                                                                                      (18, '고양이 캣타워', '고양이 낮잠용, 상태 좋아요. 직거래.', 2, 'User Two', 'HOBBY', 'SALE', '["cloth1.jpg", "cloth2.jpg"]', '2025-03-10 12:25:00', 32, 70000),
                                                                                                                                      (19, '강원도 캠핑 장비', '캠핑 갔을 때 쓴 장비 팝니다. 거래 완료.', 3, 'User Three', 'HOBBY', 'SOLD_OUT', '["bicycle1.jpg", "bicycle2.jpg"]', '2025-03-10 12:30:00', 25, 150000),
                                                                                                                                      (20, '아침 식사용 접시', '아침에 쓰던 접시, 새것 수준이에요.', 5, 'User Five', 'GENERAL', 'SALE', '["bag1.jpg", "bag2.jpg"]', '2025-03-10 12:35:00', 9, 15000),
                                                                                                                                      (21, '디저트 접시 세트', '디저트용 접시, 깨끗하게 썼어요.', 1, 'User One', 'GENERAL', 'SALE', '["mac1.jpg", "mac2.jpg"]', '2025-03-10 12:40:00', 23, 25000),
                                                                                                                                      (22, '운동화 팝니다', '새 운동화, 사이즈 안 맞아서 판매.', 2, 'User Two', 'FASHION', 'RESERVED', '["metrix1.jpg"]', '2025-03-10 12:45:00', 11, 60000);


INSERT INTO likes (likes_id, customer_id, post_id, created_at) VALUES
                                                                   (1, 1, 2, '2025-03-10 13:00:00'), -- User One이 User Two의 게시글 좋아요
                                                                   (2, 2, 1, '2025-03-10 13:05:00'), -- User Two가 User One의 게시글 좋아요
                                                                   (3, 3, 4, '2025-03-10 13:10:00'), -- User Three가 User Five의 게시글 좋아요
                                                                   (4, 5, 3, '2025-03-10 13:15:00'), -- User Five가 User Three의 게시글 좋아요
                                                                   (5, 1, 6, '2025-03-10 13:20:00'), -- User One이 User Two의 게시글 좋아요
                                                                   (6, 2, 5, '2025-03-10 13:25:00'), -- User Two가 User One의 게시글 좋아요
                                                                   (7, 3, 8, '2025-03-10 13:30:00'); -- User Three가 User Five의 게시글 좋아요