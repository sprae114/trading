package com.backend.chat.config;

import com.backend.chat.model.ChatMessage;
import com.backend.chat.model.ChatRoom;
import com.backend.chat.repository.ChatMessageRepository;
import com.backend.chat.repository.ChatRoomRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @PostConstruct
    public void init() {
        // ChatRoom 데이터 (중고 거래 물건 제목으로 설정)
        List<ChatRoom> chatRooms = Arrays.asList(
                // 원래 senderId: 1L (User One) -> 이제 receiver가 User One
                ChatRoom.builder().id("room001").name("피자 오븐 팝니다").senderId(2L).sender("User Two").receiverId(1L).receiver("User One").createdAt(LocalDateTime.of(2025, 3, 10, 11, 0)).build(),
                ChatRoom.builder().id("room005").name("도시락 통 판매").senderId(3L).sender("User Three").receiverId(1L).receiver("User One").createdAt(LocalDateTime.of(2025, 3, 10, 11, 20)).build(),
                ChatRoom.builder().id("room009").name("부산 여행 가방").senderId(5L).sender("User Five").receiverId(1L).receiver("User One").createdAt(LocalDateTime.of(2025, 3, 10, 11, 40)).build(),
                ChatRoom.builder().id("room013").name("강아지 목줄").senderId(5L).sender("User Five").receiverId(1L).receiver("User One").createdAt(LocalDateTime.of(2025, 3, 10, 12, 0)).build(),
                ChatRoom.builder().id("room017").name("겨울 코트").senderId(3L).sender("User Three").receiverId(1L).receiver("User One").createdAt(LocalDateTime.of(2025, 3, 10, 12, 20)).build(),
                ChatRoom.builder().id("room021").name("디저트 접시 세트").senderId(3L).sender("User Three").receiverId(1L).receiver("User One").createdAt(LocalDateTime.of(2025, 3, 10, 12, 40)).build(),

                // 원래 senderId: 2L (User Two) -> 이제 receiver가 User Two
                ChatRoom.builder().id("room002").name("제주도 기념품 세트").senderId(3L).sender("User Three").receiverId(2L).receiver("User Two").createdAt(LocalDateTime.of(2025, 3, 10, 11, 5)).build(),
                ChatRoom.builder().id("room006").name("라멘 그릇 세트").senderId(5L).sender("User Five").receiverId(2L).receiver("User Two").createdAt(LocalDateTime.of(2025, 3, 10, 11, 25)).build(),
                ChatRoom.builder().id("room010").name("주말용 캠핑 용품").senderId(3L).sender("User Three").receiverId(2L).receiver("User Two").createdAt(LocalDateTime.of(2025, 3, 10, 11, 45)).build(),
                ChatRoom.builder().id("room014").name("서울 여행용 백팩").senderId(3L).sender("User Three").receiverId(2L).receiver("User Two").createdAt(LocalDateTime.of(2025, 3, 10, 12, 5)).build(),
                ChatRoom.builder().id("room018").name("고양이 캣타워").senderId(5L).sender("User Five").receiverId(2L).receiver("User Two").createdAt(LocalDateTime.of(2025, 3, 10, 12, 25)).build(),
                ChatRoom.builder().id("room022").name("운동화 팝니다").senderId(5L).sender("User Five").receiverId(2L).receiver("User Two").createdAt(LocalDateTime.of(2025, 3, 10, 12, 45)).build(),

                // 원래 senderId: 3L (User Three) -> 이제 receiver가 User Three
                ChatRoom.builder().id("room003").name("봄 옷 세트").senderId(5L).sender("User Five").receiverId(3L).receiver("User Three").createdAt(LocalDateTime.of(2025, 3, 10, 11, 10)).build(),
                ChatRoom.builder().id("room007").name("가죽 가방 팝니다").senderId(1L).sender("User One").receiverId(3L).receiver("User Three").createdAt(LocalDateTime.of(2025, 3, 10, 11, 30)).build(),
                ChatRoom.builder().id("room011").name("스시 접시 세트").senderId(1L).sender("User One").receiverId(3L).receiver("User Three").createdAt(LocalDateTime.of(2025, 3, 10, 11, 50)).build(),
                ChatRoom.builder().id("room015").name("저녁 식기 세트").senderId(1L).sender("User One").receiverId(3L).receiver("User Three").createdAt(LocalDateTime.of(2025, 3, 10, 12, 10)).build(),
                ChatRoom.builder().id("room019").name("강원도 캠핑 장비").senderId(1L).sender("User One").receiverId(3L).receiver("User Three").createdAt(LocalDateTime.of(2025, 3, 10, 12, 30)).build(),

                // 원래 senderId: 5L (User Five) -> 이제 receiver가 User Five
                ChatRoom.builder().id("room004").name("강아지 옷 팝니다").senderId(1L).sender("User One").receiverId(5L).receiver("User Five").createdAt(LocalDateTime.of(2025, 3, 10, 11, 15)).build(),
                ChatRoom.builder().id("room008").name("고양이 장난감").senderId(2L).sender("User Two").receiverId(5L).receiver("User Five").createdAt(LocalDateTime.of(2025, 3, 10, 11, 35)).build(),
                ChatRoom.builder().id("room012").name("여름 옷 정리").senderId(2L).sender("User Two").receiverId(5L).receiver("User Five").createdAt(LocalDateTime.of(2025, 3, 10, 11, 55)).build(),
                ChatRoom.builder().id("room016").name("카페 컵 세트").senderId(2L).sender("User Two").receiverId(5L).receiver("User Five").createdAt(LocalDateTime.of(2025, 3, 10, 12, 15)).build(),
                ChatRoom.builder().id("room020").name("아침 식사용 접시").senderId(2L).sender("User Two").receiverId(5L).receiver("User Five").createdAt(LocalDateTime.of(2025, 3, 10, 12, 35)).build()
        );
        chatRoomRepository.saveAll(chatRooms);

        // ChatMessage 데이터 (중고 거래 대화로 수정)
        List<ChatMessage> chatMessages = Arrays.asList(
                // room001
                ChatMessage.builder().id("msg001").roomId("room001").sender("User One").content("아이폰 13 아직 있나요?").timestamp(LocalDateTime.of(2025, 3, 26, 9, 5)).build(),
                ChatMessage.builder().id("msg002").roomId("room001").sender("User Five").content("네, 있습니다. 상태 좋아요.").timestamp(LocalDateTime.of(2025, 3, 26, 9, 6)).build(),
                ChatMessage.builder().id("msg003").roomId("room001").sender("User One").content("가격 좀 깎아주실 수 있나요?").timestamp(LocalDateTime.of(2025, 3, 26, 9, 7)).build(),

                // room002
                ChatMessage.builder().id("msg004").roomId("room002").sender("User Two").content("노트북 배터리 상태 어때요?").timestamp(LocalDateTime.of(2025, 3, 25, 14, 35)).build(),
                ChatMessage.builder().id("msg005").roomId("room002").sender("User Five").content("80% 이상 남아있어요.").timestamp(LocalDateTime.of(2025, 3, 25, 14, 36)).build(),
                ChatMessage.builder().id("msg006").roomId("room002").sender("User Two").content("오늘 만날 수 있나요?").timestamp(LocalDateTime.of(2025, 3, 25, 14, 37)).build(),

                // room003
                ChatMessage.builder().id("msg007").roomId("room003").sender("User Three").content("에어팟 사용 기간이どれくらいですか?").timestamp(LocalDateTime.of(2025, 3, 24, 10, 20)).build(),
                ChatMessage.builder().id("msg008").roomId("room003").sender("User Five").content("6개월 정도 썼습니다.").timestamp(LocalDateTime.of(2025, 3, 24, 10, 21)).build(),
                ChatMessage.builder().id("msg009").roomId("room003").sender("User Three").content("사진 좀 더 볼 수 있을까요?").timestamp(LocalDateTime.of(2025, 3, 24, 10, 22)).build(),

                // room004 
                ChatMessage.builder().id("msg010").roomId("room004").sender("User One").content("강아지 옷 사이즈가 어떻게 되나요?").timestamp(LocalDateTime.of(2025, 3, 10, 11, 15)).build(),
                ChatMessage.builder().id("msg011").roomId("room004").sender("User Five").content("소형견용이에요. 몸길이 30cm 정도 됩니다.").timestamp(LocalDateTime.of(2025, 3, 10, 11, 17)).build(),
                ChatMessage.builder().id("msg012").roomId("room004").sender("User One").content("직거래로 받을 수 있을까요?").timestamp(LocalDateTime.of(2025, 3, 10, 11, 19)).build(),

                // room005
                ChatMessage.builder().id("msg013").roomId("room005").sender("User One").content("운동화 사이즈 몇이에요?").timestamp(LocalDateTime.of(2025, 3, 22, 13, 50)).build(),
                ChatMessage.builder().id("msg014").roomId("room005").sender("User Five").content("270mm입니다.").timestamp(LocalDateTime.of(2025, 3, 22, 13, 51)).build(),
                ChatMessage.builder().id("msg015").roomId("room005").sender("User One").content("택배로 보내줄 수 있나요?").timestamp(LocalDateTime.of(2025, 3, 22, 13, 52)).build(),

                // room006
                ChatMessage.builder().id("msg016").roomId("room006").sender("User Two").content("전자레인지 몇 와트예요?").timestamp(LocalDateTime.of(2025, 3, 21, 11, 25)).build(),
                ChatMessage.builder().id("msg017").roomId("room006").sender("User Five").content("700W입니다.").timestamp(LocalDateTime.of(2025, 3, 21, 11, 26)).build(),
                ChatMessage.builder().id("msg018").roomId("room006").sender("User Two").content("내일 받을 수 있을까요?").timestamp(LocalDateTime.of(2025, 3, 21, 11, 27)).build(),

                // room007 
                ChatMessage.builder().id("msg019").roomId("room007").sender("User One").content("안녕하세요, 가죽 가방 아직 판매 중인가요?").timestamp(LocalDateTime.of(2025, 3, 10, 11, 30)).build(),
                ChatMessage.builder().id("msg020").roomId("room007").sender("User Three").content("네, 아직 판매 중입니다. 상태 궁금하시면 사진 보내드릴까요?").timestamp(LocalDateTime.of(2025, 3, 10, 11, 32)).build(),
                ChatMessage.builder().id("msg021").roomId("room007").sender("User One").content("사진 부탁드릴게요!").timestamp(LocalDateTime.of(2025, 3, 10, 11, 34)).build(),

                // room008
                ChatMessage.builder().id("msg022").roomId("room008").sender("Admin User").content("기타 줄 상태 어때요?").timestamp(LocalDateTime.of(2025, 3, 19, 16, 35)).build(),
                ChatMessage.builder().id("msg023").roomId("room008").sender("User Five").content("새로 교체한 지 얼마 안 됐어요.").timestamp(LocalDateTime.of(2025, 3, 19, 16, 36)).build(),
                ChatMessage.builder().id("msg024").roomId("room008").sender("Admin User").content("오늘 저녁에 볼까요?").timestamp(LocalDateTime.of(2025, 3, 19, 16, 37)).build(),

                // room009
                ChatMessage.builder().id("msg025").roomId("room009").sender("User One").content("PS5 컨트롤러 포함인가요?").timestamp(LocalDateTime.of(2025, 3, 18, 12, 5)).build(),
                ChatMessage.builder().id("msg026").roomId("room009").sender("User Five").content("네, 포함입니다.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 6)).build(),
                ChatMessage.builder().id("msg027").roomId("room009").sender("User One").content("입금하면 바로 보내주시죠?").timestamp(LocalDateTime.of(2025, 3, 18, 12, 7)).build(),
                ChatMessage.builder().id("msg028").roomId("room009").sender("User Five").content("네, 입금 확인 후 바로 발송해 드리겠습니다.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 8)).build(),
                ChatMessage.builder().id("msg029").roomId("room009").sender("User One").content("좋아요. 계좌 정보 알려주세요.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 9)).build(),
                ChatMessage.builder().id("msg030").roomId("room009").sender("User Five").content("계좌는 국민은행 123-456-789 입니다. 입금 후 알려주세요.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 10)).build(),
                ChatMessage.builder().id("msg031").roomId("room009").sender("User One").content("입금 완료했습니다. 확인 부탁드려요.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 15)).build(),
                ChatMessage.builder().id("msg032").roomId("room009").sender("User Five").content("확인했습니다. 오늘 중으로 발송하겠습니다. 감사합니다!").timestamp(LocalDateTime.of(2025, 3, 18, 12, 16)).build(),
                ChatMessage.builder().id("msg033").roomId("room009").sender("User One").content("혹시 배송 추적 번호를 받을 수 있을까요?").timestamp(LocalDateTime.of(2025, 3, 18, 12, 17)).build(),
                ChatMessage.builder().id("msg034").roomId("room009").sender("User Five").content("물품 발송 후 바로 추적 번호를 보내드리겠습니다.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 18)).build(),
                ChatMessage.builder().id("msg035").roomId("room009").sender("User One").content("감사합니다! 물건 잘 받으면 연락드릴게요.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 19)).build(),
                ChatMessage.builder().id("msg036").roomId("room009").sender("User Five").content("네! 좋은 하루 보내세요. 감사합니다.").timestamp(LocalDateTime.of(2025, 3, 18, 12, 20)).build(),
                ChatMessage.builder().id("msg037").roomId("room009").sender("User Five").content("발송 완료했습니다! 추적 번호는 ABC123456입니다. 확인 부탁드립니다.").timestamp(LocalDateTime.of(2025, 3, 18, 14, 0)).build(),
                ChatMessage.builder().id("msg038").roomId("room009").sender("User One").content("추적 번호 확인했습니다. 감사합니다! 배송 기다릴게요. 😊").timestamp(LocalDateTime.of(2025, 3, 18, 14, 10)).build(),
                ChatMessage.builder().id("msg039").roomId("room009").sender("User One").content("물건 잘 받았습니다! 상태도 아주 좋네요. 감사합니다! 🙏🏻").timestamp(LocalDateTime.of(2025, 3, 19, 10, 30)).build(),
                ChatMessage.builder().id("msg040").roomId("room009").sender("User Five").content("좋은 소식이라니 다행입니다! 사용하시면서 궁금한 점 있으시면 언제든 말씀해주세요. 😊").timestamp(LocalDateTime.of(2025, 3, 19, 10, 45)).build(),
                ChatMessage.builder().id("msg041").roomId("room009").sender("User One").content("네! 혹시 컨트롤러 배터리는 완충 상태인가요? 사용하려고 보니 배터리가 부족한 것 같아서요. 😅").timestamp(LocalDateTime.of(2025, 3, 19, 11, 0)).build(),
                ChatMessage.builder().id("msg042").roomId("room009").sender("User Five").content("아 컨트롤러는 발송 전에 완충하지 못했네요. 충전 케이블로 충전하시면 금방 사용 가능하실 거예요! 죄송합니다 😅 ").timestamp(LocalDateTime.of(2025, 3, 19, 11,15)).build(),
                ChatMessage.builder().id("msg043").roomId("room009").sender("User One").content("괜찮아요! 충전 중입니다 😊 물건 상태가 너무 좋아서 만족스럽습니다. 다시 한 번 감사드려요! 🙏🏻 ").timestamp(LocalDateTime.of(2025 ,3 ,19 ,11 ,30 )).build()
        );
        chatMessageRepository.saveAll(chatMessages);

        log.info("Data initialized - ChatRooms: {}, ChatMessages: {}", chatRooms.size(), chatMessages.size());
    }

    @PreDestroy
    public void destroy() {
        chatMessageRepository.deleteAll();
        chatRoomRepository.deleteAll();
        log.info("Data cleared - ChatMessages and ChatRooms removed");
    }
}