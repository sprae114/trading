<template>
    <div v-if="isModalOpen" class="modal-overlay" @click="goBack">
        <div class="modal-content" @click.stop>
            <div class="modal-body">
                <!-- 상품 정보 헤더 -->
                <div class="product-header">
                    <div class="product-title-group">
                        <h1 class="product-title">{{ product.title }}</h1>
                    </div>
                    <div class="product-title-group-info">
                        <v-chip text-color="white" size="small" class="product-status">
                            {{ product.status || 'SALE' }}
                        </v-chip>
                        <p class="product-price">{{ product.price ? '₩ ' + product.price.toLocaleString('ko-KR') + '원' : '가격 미정' }}</p>
                    </div>
                </div>

                <v-divider class="my-1" />

                <!-- 채팅 내역 -->
                <div class="chatting-room" ref="chatContainer" @scroll="handleScroll">
                    <div v-if="showLoadMore" class="load-more-container">
                        <v-btn color="primary" :loading="isLoading" @click="loadMoreMessages">이전 채팅 불러오기</v-btn>
                    </div>
                    <v-list class="chat-list" dense>
                        <v-list-item
                            v-for="(message, index) in chatHistory"
                            :key="index"
                            :class="{ 'my-message': message.isMine, 'other-message': !message.isMine }"
                        >
                            <v-list-item-title class="message-text">{{ message.text }}</v-list-item-title>
                            <v-list-item-subtitle>{{ message.timestamp }}</v-list-item-subtitle>
                        </v-list-item>
                    </v-list>
                </div>

                <!-- 메시지 입력 -->
                <div class="chat-message">
                    <v-text-field
                        v-model="newMessage"
                        placeholder="메시지를 입력하세요..."
                        variant="outlined"
                        dense
                        append-icon="mdi-send"
                        @click:append="sendMessage"
                        @keyup.enter="sendMessage"
                        class="message-input"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import axios from 'axios';
import { mapState, mapMutations } from 'vuex';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

export default {
    data() {
        return {
            product: {
                id: null,
                title: '',
                price: '',
                status: 'SALE',
            },
            newMessage: '', // 새 메시지 입력값
            chatHistory: [], // 채팅 내역
            userInfo: {}, // 사용자 정보
            chatRoomData: {}, // 채팅방 데이터
            postInfo: {}, // 게시물 정보
            currentPage: 1, // 현재 페이지
            totalPages: 1, // 총 페이지 수
            stompClient: null, // WebSocket 클라이언트
            connected: false, // 연결 상태
            showLoadMore: false, // 더 불러오기 버튼 표시 여부
            isLoading: false, // 로딩 상태
        };
    },
    methods: {
        async initialize() { // 초기화
            this.userInfo = JSON.parse(sessionStorage.getItem('userInformation') || '{}');
            await this.getOneChatRoom();
            await this.getChatMessage();
        },
        getOneChatRoom() { // 채팅방 정보 가져오기
            return axios.get(`${this.backendUrl}/api/chat/rooms/${this.$route.params.chatRoom}`)
                .then(response => {
                    this.chatRoomData = response.data.chatRoom;
                    this.postInfo = response.data.postSimpleResponseDto;
                    this.product = {
                        id: this.chatRoomData.id,
                        title: this.chatRoomData.name,
                        price: this.postInfo.price,
                        status: this.postInfo.tradeStatus,
                    };
                })
                .catch(() => {
                });
        },
        getChatMessage() { // 채팅 메시지 가져오기
            this.isLoading = true;
            const page = this.currentPage - 1; // Spring은 0부터 시작
            return axios.get(`${this.backendUrl}/api/chat/rooms/${this.$route.params.chatRoom}/messages`, {
                params: { page },
            })
                .then(response => {
                    this.totalPages = response.data.totalPages;
                    const messages = response.data.content || [];
                    const newMessages = messages.map(message => ({
                        text: message.content,
                        isMine: message.sender === this.userInfo.customer.name,
                        rawTimestamp: message.timestamp,
                        timestamp: new Date(message.timestamp).toLocaleString('ko-KR', { dateStyle: 'short', timeStyle: 'short' }),
                    }));
                    this.chatHistory = [...newMessages, ...this.chatHistory];
                    this.chatHistory.sort((a, b) => new Date(a.rawTimestamp) - new Date(b.rawTimestamp));
                    if (this.currentPage === 1) {
                        this.$nextTick(() => this.scrollToBottom());
                    }
                    this.showLoadMore = this.currentPage < this.totalPages;
                })
                .catch(() => {
                    this.chatHistory = [];
                })
                .finally(() => {
                    this.isLoading = false;
                });
        },
        loadMoreMessages() { // 이전 메시지 추가 로드
            if (this.currentPage < this.totalPages && !this.isLoading) {
                const previousHeight = this.$refs.chatContainer.scrollHeight;
                this.currentPage++;
                this.getChatMessage().then(() => {
                    this.$nextTick(() => {
                        const newHeight = this.$refs.chatContainer.scrollHeight;
                        this.$refs.chatContainer.scrollTop = newHeight - previousHeight;
                    });
                });
            }
        },
        handleScroll() { // 스크롤 이벤트 처리
            const container = this.$refs.chatContainer;
            if (!container) return;
            const scrollTop = container.scrollTop;
            this.showLoadMore = scrollTop < 10 && this.currentPage < this.totalPages && !this.isLoading;
        },
        connect() { // WebSocket 연결
            const socket = new SockJS(`${this.backendUrl}/ws-chat`);
            this.stompClient = Stomp.over(socket);
            this.stompClient.connect(
                {},
                () => {
                    this.connected = true;
                    this.stompClient.subscribe(`/topic/chat-room/${this.chatRoomData.id}`, message => {
                        const chatMessage = JSON.parse(message.body);
                        this.chatHistory.push({
                            text: chatMessage.content,
                            isMine: chatMessage.sender === this.userInfo.customer.name,
                            timestamp: new Date(chatMessage.timestamp).toLocaleString('ko-KR', { dateStyle: 'short', timeStyle: 'short' }),
                        });
                        this.$nextTick(() => this.scrollToBottom());
                    });
                },
                error => {
                    setTimeout(() => this.connect(), 5000);
                }
            );
        },
        disconnect() { // WebSocket 연결 해제
            if (this.stompClient && this.connected) {
                this.stompClient.disconnect();
                this.connected = false;
            }
        },
        sendMessage() { // 메시지 전송
            if (!this.newMessage.trim()) {
                alert('메시지를 입력하세요.');
                return;
            }
            if (!this.connected) {
                alert('서버에 연결 중입니다. 잠시 후 다시 시도해주세요.');
                return;
            }
            const message = {
                roomId: this.chatRoomData.id,
                sender: this.userInfo.customer.name,
                content: this.newMessage,
            };
            this.stompClient.send(
                `/app/send/${this.chatRoomData.id}`,
                {},
                JSON.stringify(message)
            );
            this.newMessage = '';
            this.$nextTick(() => this.scrollToBottom());
        },
        goBack() { // 뒤로 가기
            this.SET_MODAL_OPEN(false);
            this.$router.push('/chat/list');
        },
        scrollToBottom() { // 채팅창 하단으로 스크롤
            const container = this.$refs.chatContainer;
            if (container) {
                container.scrollTop = container.scrollHeight;
            }
        },
        ...mapMutations(['SET_MODAL_OPEN']),
    },
    created() {
        this.initialize();
    },
    mounted() {
        this.$nextTick(() => {
            if (this.chatRoomData.id) {
                this.connect();
            } else {
                const unwatch = this.$watch('chatRoomData.id', newVal => {
                    if (newVal) {
                        this.connect();
                        unwatch();
                    }
                });
            }
        });
    },
    beforeDestroy() {
        this.disconnect();
    },
    computed: {
        ...mapState(['backendUrl', 'isModalOpen']),
    },
};
</script>

<style scoped>
/* 모달 오버레이 스타일 */
.modal-overlay {
    background: rgba(0, 0, 0, 0.5);
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 101;
    display: flex;
    justify-content: center;
    align-items: center;
}

/* 모달 콘텐츠 스타일 */
.modal-content {
    background: #fff;
    border-radius: 16px;
    max-width: 800px;
    width: 90%;
    overflow-y: auto;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    padding: 24px;
}

/* 모달 본문 스타일 */
.modal-body {
    display: flex;
    flex-direction: column;
    height: 100%;
}

/* 상품 헤더 스타일 */
.product-header {
    padding-bottom: 16px;
}

.product-title-group {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
}

.product-title-group-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.product-title {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    margin: 0;
}

.product-price {
    font-size: 22px;
    font-weight: 600;
    margin: 0;
}

/* 채팅 영역 스타일 */
.chatting-room {
    flex-grow: 1;
    overflow-y: auto;
    height: 70vh;
    padding: 8px 0;
    position: relative;
}

.chat-list .v-list-item {
    width: 100%;
}

.chat-list .message-text {
    white-space: normal;
    word-break: break-word;
    overflow-wrap: break-word;
    max-width: 100%;
    padding: 8px;
}

/* 내 메시지 스타일 */
.my-message {
    text-align: right;
    padding-right: 8px;
}

.my-message .v-list-item-title {
    background: #6164ff;
    color: white;
    padding: 8px;
    border-radius: 8px;
    display: inline-block;
}

/* 상대방 메시지 스타일 */
.other-message {
    text-align: left;
    padding-left: 8px;
}

.other-message .v-list-item-title {
    background: #e0e0e0;
    padding: 8px;
    border-radius: 8px;
    display: inline-block;
}

.v-list-item-subtitle {
    font-size: 12px;
    color: #888;
}

/* 메시지 입력 영역 스타일 */
.chat-message {
    padding-top: 8px;
}

.message-input {
    margin: 0;
}

/* 더 불러오기 버튼 스타일 */
.load-more-container {
    text-align: center;
    padding: 10px;
    position: sticky;
    top: 0;
    background: rgba(255, 255, 255, 0.9);
    z-index: 1;
}

/* 반응형 조정 */
@media (max-width: 600px) {
    .modal-content {
        width: 95%;
        max-height: 90vh;
        padding: 16px;
    }
    .product-title {
        font-size: 22px;
    }
    .product-price {
        font-size: 18px;
    }
    .chatting-room {
        max-height: 40vh;
    }
}
</style>