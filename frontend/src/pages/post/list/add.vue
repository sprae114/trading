<template>
    <div v-if="isModalOpen" class="modal-overlay" @click="SET_MODAL_OPEN(false)">
        <div class="modal-content" @click.stop>
            <div class="modal-body">
                <!-- 파일 업로드 및 캐러셀 -->
                <v-carousel class="product-carousel" hide-delimiters>
                    <v-carousel-item v-for="uploadImage in product.uploadImages" :key="uploadImage" :src="uploadImage" cover />
                </v-carousel>
                <v-file-input
                    v-model="files"
                    label="파일 업로드"
                    placeholder="파일을 선택하세요"
                    prepend-icon="mdi-paperclip"
                    variant="outlined"
                    counter
                    multiple
                    @change="onFileUpload()"
                />

                <v-divider class="my-4" />

                <!-- 상품 정보 입력 폼 -->
                <div class="product-context">
                    <v-form ref="form" v-model="valid" fast-fail @submit.prevent>
                        <div class="product-description">
                            <div class="product-title-group">
                                <v-select
                                    class="product-category"
                                    v-model="product.category"
                                    :items="categories"
                                    label="카테고리"
                                    variant="outlined"
                                    dense
                                />
                                <v-text-field
                                    label="가격"
                                    variant="outlined"
                                    type="number"
                                    :rules="[rules.priceRequired]"
                                    v-model.number="product.price"
                                />
                                <v-btn
                                    prepend-icon="mdi-content-save"
                                    variant="elevated"
                                    rounded="lg"
                                    class="chat-btn"
                                    color="#6164ff"
                                    @click="saveProduct"
                                >
                                    상품 게시
                                </v-btn>
                            </div>
                            <v-text-field
                                label="제목"
                                variant="outlined"
                                :rules="[rules.titleRequired]"
                                v-model="product.title"
                            />
                            <v-textarea
                                label="상품 설명"
                                variant="outlined"
                                :readonly="isReadonly"
                                :bg-color="textColor"
                                rows="6"
                                no-resize
                                :rules="[rules.bodyRequired]"
                                v-model="product.body"
                            />
                        </div>
                    </v-form>
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
            valid: false, // 폼 유효성 상태
            categories: [
                'ALL', 'ELECTRONICS', 'FASHION', 'HOME_APPLIANCES', 'BOOKS',
                'SPORTS', 'BABY', 'BEAUTY', 'VEHICLES', 'HOBBY', 'GENERAL'
            ],
            files: [], // 업로드할 파일 배열
            isReadonly: false, // 읽기 전용 여부
            textColor: 'white', // 텍스트 필드 배경색
            rules: {
                titleRequired: value => !!value || '제목을 입력해주세요',
                bodyRequired: value => !!value || '본문을 입력해주세요',
                priceRequired: value => (value !== null && value >= 0) || '유효한 가격을 입력해주세요',
            },
            product: { // 상품 정보
                title: '',
                body: '',
                category: '',
                price: null,
                author: '',
                uploadImages: [], // 업로드된 이미지 URL 배열
            },
            userInfo: {}, // 사용자 정보
        };
    },
    methods: {
        initialize() { // 컴포넌트 초기화
            this.userInfo = JSON.parse(sessionStorage.getItem('userInformation') || '{}');
        },
        closeModal() { // 모달 닫기
            this.SET_MODAL_OPEN(false);
            this.$router.push('/post/list');
        },
        saveProduct() { // 상품 저장
            if (!this.product.title || !this.product.body || this.product.price === null) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '제목, 내용, 가격을 입력해주세요.']);
                return;
            }
            if (!this.valid) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '입력 형식이 맞지 않습니다.']);
                return;
            }

            const requestDto = {
                title: this.product.title,
                body: this.product.body,
                price: this.product.price,
                customerId: this.userInfo.customer.id,
                customerName: this.userInfo.customer.name,
                category: this.product.category || 'ALL',
            };

            const formData = new FormData();
            formData.append('requestDto', new Blob([JSON.stringify(requestDto)], { type: 'application/json' }));

            if (this.product.uploadImages.length > 0) {
                Promise.all(
                    this.product.uploadImages.map((url, index) =>
                        fetch(url)
                            .then(response => response.blob())
                            .then(blob => {
                                const fileName = this.files[index]?.name || `image-${index}.jpg`;
                                return new File([blob], fileName, { type: blob.type });
                            })
                    )
                ).then(files => {
                    files.forEach(file => formData.append('imageFiles', file));
                    axios.post(`${this.$store.state.backendUrl}/api/post`, formData, {
                        headers: { 'Content-Type': 'multipart/form-data' },
                    }).then(() => {
                        this.SHOW_NOMAL_ALERT_MESSAGE(['success', '게시글이 성공적으로 저장되었습니다.']);
                        this.closeModal();
                        this.product = { title: '', body: '', category: '', price: null, uploadImages: [] };
                        this.files = [];
                    }).catch(() => {
                        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '게시글 저장에 실패했습니다.']);
                    });
                }).catch(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이미지 업로드 처리 중 오류가 발생했습니다.']);
                });
            } else {
                axios.post(`${this.$store.state.backendUrl}/api/post`, formData, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                }).then(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['success', '게시글이 성공적으로 저장되었습니다.']);
                    this.closeModal();
                    this.product = { title: '', body: '', category: '', price: null, uploadImages: [] };
                    this.files = [];
                }).catch(() => {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '게시글 저장에 실패했습니다.']);
                });
            }
        },
        onFileUpload() { // 파일 업로드 처리
            this.product.uploadImages = [];
            if (this.files && this.files.length > 0) {
                this.files.forEach(file => {
                    const url = URL.createObjectURL(file);
                    this.product.uploadImages.push(url);
                });
            }
        },
        ...mapMutations(['SET_MODAL_OPEN', 'SHOW_NOMAL_ALERT_MESSAGE']),
    },
    computed: {
        ...mapState(['isModalOpen']),
    },
    created() {
        this.initialize();
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
    width: 100%;
    overflow-y: auto;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
    padding: 24px;
    position: relative;
}

/* 모달 본문 스타일 */
.modal-body {
    display: flex;
    height: 90vh;
    flex-direction: column;
}

/* 상품 캐러셀 스타일 */
.product-carousel {
    border-radius: 12px;
    overflow: hidden;
    margin-bottom: 20px;
    min-height: 55vh;
}

/* 상품 헤더 스타일 */
.product-header {
    padding-bottom: 16px;
}

/* 상품 입력 그룹 스타일 */
.product-title-group {
    display: flex;
    align-items: stretch;
    gap: 15px;
    margin: 0 10px;
}

.product-category {
    max-width: 150px;
    min-width: 80px;
    width: 100%;
}

/* 상품 제목 스타일 */
.product-title {
    font-size: 28px;
    font-weight: 700;
    color: #333;
    margin: 0;
}

/* 상품 정보 스타일 */
.product-info {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    color: #666;
    font-size: 14px;
    margin-bottom: 5px;
}

/* 텍스트エリア 스타일 */
.v-textarea {
    border-radius: 8px;
    padding: 12px;
}

/* 저장 버튼 스타일 */
.chat-btn {
    margin-left: auto;
    font-size: 15px;
    text-transform: none;
    height: 56px;
}

/* 스크롤바 스타일 */
::-webkit-scrollbar {
    width: 10px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 10px;
}

/* 반응형 조정 */
@media (max-width: 600px) {
    .modal-content {
        width: 95%;
        max-height: 90vh;
        padding: 16px;
    }
    .product-carousel {
        height: 250px;
    }
    .product-title {
        font-size: 22px;
    }
    .chat-btn {
        width: 100%;
        margin-left: 0;
        margin-top: 12px;
    }
}
</style>