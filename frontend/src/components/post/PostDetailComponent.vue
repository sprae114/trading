<template>
    <div>
        <!-- 삭제 확인 알림 -->
        <v-alert
            v-show="isBtnAlertVisible"
            icon="mdi-delete"
            class="custom-alert"
            title="이 물품을 삭제할까요?"
            type="error"
            elevation="4"
        >
            <div class="alert-btn">
                <v-btn
                    color="#6164ff"
                    variant="elevated"
                    rounded="lg"
                    @click="doDeleteAction"
                    class="alert-action-btn"
                >
                    삭제
                </v-btn>
                <v-btn
                    color="#e0e0e0"
                    text-color="#333"
                    variant="elevated"
                    rounded="lg"
                    @click="cancelDeleteAction"
                    class="alert-action-btn"
                >
                    취소
                </v-btn>
            </div>
        </v-alert>

        <!-- 모달: 상품 상세 정보 -->
        <div v-if="isModalOpen" class="modal-overlay" @click="goBack">
            <div class="modal-content" @click.stop>
                <div class="modal-body">
                    <!-- 상품 이미지 캐러셀 -->
                    <v-carousel class="product-carousel" hide-delimiters height="400">
                        <v-carousel-item
                            v-for="uploadImage in product.images"
                            :key="uploadImage"
                            :src="uploadImage"
                            contain
                            class="carousel-item"
                        />
                    </v-carousel>

                    <!-- 상품 제목과 가격 -->
                    <div class="product-header">
                        <div class="product-title-group">
                            <v-text-field
                                v-if="!isReadonly"
                                v-model="product.title"
                                class="product-title-input"
                                variant="plain"
                                :bg-color="textColor"
                                hide-details
                            />
                            <h1 v-else class="product-title">{{ product.title }}</h1>
                            <v-text-field
                                v-if="!isReadonly"
                                v-model="product.price"
                                class="product-price-input"
                                variant="plain"
                                type="number"
                                suffix="원"
                                :bg-color="textColor"
                                hide-details
                            />
                            <p v-else class="product-price">
                                {{ product.price ? '₩ ' + product.price.toLocaleString('ko-KR') + '원' : '가격 미정' }}
                            </p>
                        </div>
                    </div>

                    <!-- 상품 정보 (상태, 카테고리 등) -->
                    <div class="product-info">
                        <v-select
                            v-if="!isReadonly"
                            v-model="product.tradeStatus"
                            :items="tradeStatus"
                            class="category-tradeStatus"
                            variant="plain"
                            :bg-color="textColor"
                            hide-details
                        />
                        <v-chip v-else text-color="white" size="small" class="product-status">
                            {{ product.tradeStatus }}
                        </v-chip>
                        <v-select
                            v-if="!isReadonly"
                            v-model="product.category"
                            :items="categories"
                            class="category-select"
                            label="카테고리"
                            variant="plain"
                            :bg-color="textColor"
                            hide-details
                        />
                        <p v-else class="info-text">카테고리: {{ product.category || '기타' }}</p>
                        <p v-if="isReadonly" class="info-text">작성자: {{ product.customerName }}</p>
                        <p v-if="isReadonly" class="info-text">조회수: {{ product.views }}</p>
                    </div>

                    <v-divider class="my-4" />

                    <!-- 상품 설명 -->
                    <div class="product-description">
                        <v-textarea
                            label="상품 설명"
                            variant="plain"
                            :readonly="isReadonly"
                            :bg-color="textColor"
                            v-model="product.body"
                            rows="6"
                            no-resize
                        />
                    </div>

                    <!-- 액션 버튼 (좋아요, 수정, 삭제, 채팅) -->
                    <div class="action-buttons" v-if="isUser">
                        <v-btn
                            v-if="userInfo"
                            :color="liked ? '#6164ff' : 'grey'"
                            icon="mdi-heart"
                            size="small"
                            @click="toggleLike"
                        />
                        <v-btn
                            v-if="isReadonly && (isAuthor || isAdmin)"
                            icon="mdi-pencil"
                            size="small"
                            color="#6164ff"
                            @click="editProduct"
                        />
                        <v-btn
                            v-if="!isReadonly"
                            icon="mdi-content-save"
                            size="small"
                            color="#6164ff"
                            @click="saveProduct"
                        />
                        <v-btn
                            v-if="isReadonly && (isAuthor || isAdmin)"
                            icon="mdi-delete"
                            size="small"
                            color="error"
                            @click="deleteProduct"
                        />
                        <!--  글쓴이가 아니거나 관리자일때 채팅 버튼-->
                        <v-btn
                            v-if="userInfo && (!isAuthor || isAdmin)"  
                            prepend-icon="mdi-message-outline"
                            color="#6164ff"
                            variant="elevated"
                            rounded="lg"
                            @click="addChatting"
                            class="chat-btn"
                        >
                            채팅하기
                        </v-btn>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { mapState, mapMutations } from 'vuex';
import axios from 'axios';

export default {
    name: 'TradingDetailComponent',
    data() {
        return {
            isUser: true, // 사용자 여부
            isReadonly: true, // 읽기 전용 모드
            liked: false, // 좋아요 상태
            textColor: 'white', // 텍스트 필드 배경색
            product: {}, // 상품 데이터
            categories: [
                'ALL', 'ELECTRONICS', 'FASHION', 'HOME_APPLIANCES', 'BOOKS',
                'SPORTS', 'BABY', 'BEAUTY', 'VEHICLES', 'HOBBY', 'GENERAL'
            ],
            tradeStatus: ['SALE', 'RESERVED', 'SOLD_OUT', 'HIDDEN'],
            isAuthor: false, // 작성자 여부
            isAdmin: false, // 관리자 여부
            userInfo: {}, // 현재 사용자 정보
        };
    },
    props: {
        path: String,
    },
    methods: {
        async initialize() { // 상품 및 사용자 정보 초기화
            if (this.showProduct && Object.keys(this.showProduct).length > 0) {
                this.product = this.showProduct;
                await this.getProdutInfo();
                this.userInfo = JSON.parse(sessionStorage.getItem('userInformation') || '{}');
                const currentUserId = this.userInfo?.customer?.id || null;
                const currentUserRole = this.userInfo?.role || null;
                this.isAuthor = currentUserId === this.product.customerId;
                this.isAdmin = currentUserRole === 'ROLE_ADMIN';
            } else {
                this.$router.push(this.path);
            }
        },
        async saveProduct() { // 상품 수정 저장
            const updateData = {
                id: this.product.id,
                title: this.product.title,
                body: this.product.body,
                price: this.product.price,
                tradeStatus: this.product.tradeStatus,
                category: this.product.category,
            };
            const jwt = sessionStorage.getItem('jwt')?.trim();
            axios.put(
                `${this.backendUrl}/api/post/${this.product.id}`,
                updateData,
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${jwt}`,
                    },
                }
            ).then(response => {
                if (response.status === 200) {
                    this.isReadonly = true;
                    this.textColor = 'white';
                    this.SHOW_NOMAL_ALERT_MESSAGE(['success', '게시물이 수정되었습니다.']);
                    this.initialize();
                }
            }).catch(error => {
                if (error.status === 401) {
                    this.$router.push('/');
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '로그아웃 후, 다시 로그인 해주세요']);
                } else {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '알 수 없는 오류가 발생했습니다.']);
                }
            });
        },
        async getProdutInfo() { // 상품 정보 가져오기
            const jwt = sessionStorage.getItem('jwt')?.trim();
            if (!jwt) {
                this.$router.push('/login/home');
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '로그인을 해주세요']);
                return;
            }
            axios.get(`${this.backendUrl}/api/post/${this.$route.params.item}`, {
                headers: { 'Authorization': `Bearer ${jwt}` },
            }).then(response => {
                this.liked = response.data.isLiked;
                this.userInfo = JSON.parse(sessionStorage.getItem('userInformation') || '{}');
            }).catch(() => {
                this.$router.push('/');
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '로그아웃 후, 다시 로그인 해주세요']);
            });
        },
        closeModal() { // 모달 닫기
            this.SET_MODAL_OPEN(false);
        },
        editProduct() { // 상품 수정 모드 활성화
            this.isReadonly = false;
            this.textColor = '#f5f5f5';
        },
        deleteProduct() { // 삭제 알림 표시
            this.SET_BTN_ALERT(true);
        },
        doDeleteAction() { // 삭제 실행
            this.deletePost();
        },
        cancelDeleteAction() { // 삭제 취소
            this.SET_BTN_ALERT(false);
        },
        async addChatting() { // 채팅 시작
            const chatRoomInfo = {
                name: this.product.title,
                senderId: this.userInfo.customer.id,
                sender: this.userInfo.customer.name,
                receiverId: this.product.customerId,
                receiver: this.product.customerName,
            };
            axios.post(`${this.backendUrl}/api/chat/rooms`, chatRoomInfo)
                .then(response => {
                    this.$router.push(`${this.path}/chat/${response.data.id}`);
                })
                .catch(() => {
                    this.$router.push('/');
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '로그아웃 후, 다시 로그인 해주세요']);
                });
        },
        async deletePost() { // 상품 삭제
            const jwt = sessionStorage.getItem('jwt')?.trim();
            axios.delete(`${this.backendUrl}/api/post?postId=${this.$route.params.item}`, {
                headers: { 'Authorization': `Bearer ${jwt}` },
            }).then(() => {
                this.$router.push(this.path);
            }).catch(error => {
            });
        },
        async like() { // 좋아요 추가
            axios.post(`${this.backendUrl}/api/post/${this.$route.params.item}/like?customerId=${this.userInfo.customer.id}`)
                .then(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['success', '좋아요를 했습니다.']);
                    this.liked = true;
                })
                .catch(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이미 좋아요를 했습니다.']);
                });
        },
        async unLike() { // 좋아요 취소
            axios.delete(`${this.backendUrl}/api/post/${this.$route.params.item}/like?customerId=${this.userInfo.customer.id}`)
                .then(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['info', '좋아요가 취소되었습니다.']);
                    this.liked = false;
                })
                .catch(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '좋아요 취소에 실패했습니다.']);
                });
        },
        toggleLike() { // 좋아요 토글
            if (this.liked) {
                this.unLike();
            } else {
                this.like();
            }
        },
        goBack() { // 뒤로 가기
            this.SET_MODAL_OPEN(false);
            this.SET_SHOWPRODUCT({});
            this.$router.go(-1);
        },
        ...mapMutations(['SET_MODAL_OPEN', 'SHOW_NOMAL_ALERT_MESSAGE', 'SET_BTN_ALERT', 'SET_SHOWPRODUCT']),
    },
    created() {
        this.initialize();
    },
    computed: {
        ...mapState(['isModalOpen', 'isBtnAlertVisible', 'tradingTab', 'showProduct', 'backendUrl']),
    },
};
</script>

<style scoped>
/* 삭제 알림 스타일 */
.custom-alert {
    position: fixed;
    top: 20px;
    left: 50%;
    transform: translateX(-50%);
    z-index: 1000;
    min-width: 400px;
    max-width: 600px;
    padding: 20px;
    border-radius: 16px;
    text-align: center;
    background-color: #ffebee;
    color: #333;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 알림 버튼 그룹 */
.alert-btn {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 16px;
}

.alert-action-btn {
    padding: 8px 24px;
    font-weight: 600;
    text-transform: none;
}

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
    position: relative;
}

.modal-body {
    display: flex;
    flex-direction: column;
}

/* 상품 이미지 캐러셀 스타일 */
.product-carousel {
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 20px;
}

.carousel-item {
    display: flex;
    justify-content: end;
    align-items: end;
}

/* 상품 제목과 가격 그룹 */
.product-title-group {
    display: flex;
    justify-content: space-between;
    gap: 10px;
    padding: 0 10px;
    margin-bottom: 8px;
}

.product-title,
.product-title-input {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    margin: 0;
    flex: 4;
    min-width: 0;
}

.product-price,
.product-price-input {
    font-size: 22px;
    font-weight: 600;
    margin: 0;
    flex: 1;
    text-align: right;
}

/* 상품 정보 스타일 */
.product-info {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    color: #666;
    gap: 10px;
    padding: 0 10px;
    font-size: 14px;
    margin-bottom: 5px;
}

.category-select {
    font-size: 14px;
    color: #666;
}

/* 상품 설명 스타일 */
.product-description {
    margin-bottom: 20px;
}

.product-description .v-textarea {
    border-radius: 8px;
    padding: 12px;
}

/* 액션 버튼 그룹 */
.action-buttons {
    display: flex;
    gap: 12px;
    align-items: center;
    flex-wrap: wrap;
    margin-bottom: 5px;
}

.chat-btn {
    margin-left: auto;
    font-size: 15px;
    text-transform: none;
}

/* 반응형 디자인 */
@media (max-width: 600px) {
    .custom-alert {
        min-width: 280px;
        max-width: 90%;
    }
    .modal-content {
        width: 95%;
        max-height: 90vh;
        padding: 16px;
    }
    .product-carousel {
        height: 250px;
    }
    .product-title,
    .product-title-input {
        font-size: 22px;
    }
    .product-price,
    .product-price-input {
        font-size: 18px;
    }
    .chat-btn {
        width: 100%;
        margin-left: 0;
        margin-top: 12px;
    }
}
</style>