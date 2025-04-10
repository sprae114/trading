<template>
    <div v-if="isModalOpen" class="modal-overlay" @click="goBack">
        <div class="modal-content" @click.stop>
            <div class="modal-body">
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
                        <v-list-item v-for="(message, index) in chatHistory" :key="index"
                            :class="{ 'my-message': message.isMine, 'other-message': !message.isMine }">
                                <v-list-item-title class="message-text">{{ message.text }}</v-list-item-title>
                                <v-list-item-subtitle>{{ message.timestamp }}</v-list-item-subtitle>
                        </v-list-item>
                    </v-list>
                </div>

                <div class="chat-message">
                    <v-text-field v-model="newMessage" placeholder="메시지를 입력하세요..." variant="outlined" dense
                        append-icon="mdi-send" @click:append="sendMessage" @keyup.enter="sendMessage"
                        class="message-input"></v-text-field>
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
            newMessage: '',
            chatHistory: [],
            userInfo: {},
            chatRoomData: {},
            postInfo : {},
            currentPage: 1,
            totalPages: 1,
            stompClient: null,
            connected: false,
            showLoadMore: false,
            isLoading: false,
        };
    },

    props: {
        path: String,
    },

    methods: {
        async initialize() { // 정보 불러오기
            this.userInfo = JSON.parse(sessionStorage.getItem('userInformation')); // 사용자 정보
            await this.getOneChatRoom(); // 채팅방 정보
            await this.getChatMessage(); // 채팅 메시지 정보
        },

        async getOneChatRoom() { // 채팅방 정보 요청 API
            try {
                const response = await axios.get(`${this.backendUrl}/api/chat/rooms/${this.$route.params.chatRoom}`);
                this.chatRoomData = response.data['chatRoom'];
                this.postInfo = response.data['postSimpleResponseDto'];

                this.product = {
                    id: this.chatRoomData.id,
                    title: this.chatRoomData.name,
                    price: this.postInfo.price,
                    status: this.postInfo.tradeStatus,
                };
            } catch (error) {
                console.error('Error fetching chat room:', error);
            }
        },

        async getChatMessage() { // 채팅 메시지 요청 API
            try {
                this.isLoading = true;
                const page = this.currentPage - 1; // Spring은 0부터 시작
                const response = await axios.get(`${this.backendUrl}/api/chat/rooms/${this.$route.params.chatRoom}/messages`, { 
                    params: { page: page } 
                });
                console.log(response);

                this.totalPages = response.data.totalPages;
                let messages = response.data.content || []; // content가 없으면 빈 배열

                const newMessages = messages.map(message => ({ 
                    text: message.content,
                    isMine: message.sender === this.userInfo.customer.name,
                    rawTimestamp: message.timestamp,
                    timestamp: new Date(message.timestamp).toLocaleString('ko-KR', { dateStyle: 'short', timeStyle: 'short' })
                }));

                this.chatHistory = [...newMessages, ...this.chatHistory];
                this.chatHistory.sort((a, b) => new Date(a.rawTimestamp) - new Date(b.rawTimestamp));

                if (this.currentPage === 1) {
                    this.$nextTick(() => this.scrollToBottom());
                }
                // 페이지가 남아있으면 버튼 표시 가능성 체크
                if (this.currentPage < this.totalPages) {
                    this.showLoadMore = true;
                }
            } catch (error) {
                console.error('Error fetching chat messages:', error);
                this.chatHistory = [];
            } finally {
                this.isLoading = false;
            }
        },

        async loadMoreMessages() { // 채팅 메시지 추가 요청 API
            if (this.currentPage < this.totalPages && !this.isLoading) {
                const previousHeight = this.$refs.chatContainer.scrollHeight;
                this.currentPage++;
                await this.getChatMessage();
                this.$nextTick(() => {
                    const newHeight = this.$refs.chatContainer.scrollHeight;
                    this.$refs.chatContainer.scrollTop = newHeight - previousHeight;
                });
            }
        },

        handleScroll() {
            const container = this.$refs.chatContainer;
            if (!container) return;

            const scrollTop = container.scrollTop;

            if (scrollTop < 10 && this.currentPage < this.totalPages && !this.isLoading) {
                this.showLoadMore = true;
            } else if (scrollTop >= 10) {
                this.showLoadMore = false;
            }
        },

        connect() { // 채팅 Socket 연결 API
            // const socket = new SockJS("http://localhost:8080/ws-chat");
            const socket = new SockJS(`${this.backendUrl}/ws-chat`);
            this.stompClient = Stomp.over(socket);

            this.stompClient.connect(
                {},
                (frame) => {
                    this.connected = true;
                    this.stompClient.subscribe(`/topic/chat-room/${this.chatRoomData.id}`, (message) => {
                        const chatMessage = JSON.parse(message.body);
                        this.chatHistory.push({
                            text: chatMessage.content,
                            isMine: chatMessage.sender === this.userInfo['customer'].name,
                            timestamp: new Date(chatMessage.timestamp).toLocaleString('ko-KR', { dateStyle: 'short', timeStyle: 'short' }),
                        });
                        this.$nextTick(() => this.scrollToBottom());
                    });
                },
                (error) => {
                    console.error("Connection error: ", error);
                    setTimeout(() => this.connect(), 5000);
                }
            );
        },

        disconnect() {
            if (this.stompClient && this.connected) {
                this.stompClient.disconnect();
                this.connected = false;
            }
        },

        sendMessage() {
            if (!this.newMessage.trim()) {
                alert("메시지를 입력하세요.");
                return;
            }
            if (!this.connected) {
                alert("서버에 연결 중입니다. 잠시 후 다시 시도해주세요.");
                return;
            }

            const message = {
                roomId: this.chatRoomData.id,
                sender: this.userInfo['customer'].name,
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

        goBack() {
            this.SET_MODAL_OPEN(false);
            this.$router.push(`${this.path}`);
        },

        scrollToBottom() {
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
                const unwatch = this.$watch('chatRoomData.id', (newVal) => {
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

.modal-content {
    background: #fff;
    border-radius: 16px;
    max-width: 800px;
    width: 90%;
    overflow-y: auto;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    padding: 24px;
}

.modal-body {
    display: flex;
    flex-direction: column;
    height: 100%;
}

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
  white-space: normal; /* 줄바꿈 허용 */
  word-break: break-word; /* 긴 단어 줄바꿈 */
  overflow-wrap: break-word;
  max-width: 100%; /* 컨테이너 너비에 맞춤 */
  padding: 8px; /* 읽기 편하도록 여백 추가 (선택) */
}


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

.chat-message {
    padding-top: 8px;
}

.message-input {
    margin: 0;
}

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