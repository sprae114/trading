<template>
    <div class="trading-container">
        <RouterView />

        <!-- 헤더: 카테고리 선택과 검색 바 -->
        <div class="trading-header">
            <v-select
                v-if="isOnlyPostList()"
                v-model="selectedCategory"
                :items="categories"
                @change="filterPosts"
                class="category-select"
                label="카테고리"
                variant="solo"
                color="#6164ff"
            />
            <v-text-field
                v-model="searchQuery"
                class="search-bar"
                prepend-inner-icon="mdi-magnify"
                placeholder="물품 검색..."
                variant="solo"
                clearable
                color="#6164ff"
            />
            <v-btn @click="filterPosts" class="search-btn" color="#6164ff" size="large">검색</v-btn>
        </div>

        <!-- 카테고리 필터링을 위한 칩 그룹 -->
        <div v-if="isOnlyPostList()" class="trading-categories">
            <v-chip-group v-model="selectedCategoryIndex" mandatory>
                <v-chip
                    v-for="(category, index) in categories"
                    :key="category"
                    :outlined="selectedCategoryIndex !== index"
                    :class="{ 'selected-chip': selectedCategoryIndex === index }"
                    :value="index"
                    @click="filterProductsByCategory(category)"
                    color="#6164ff"
                >
                    {{ category }}
                </v-chip>
            </v-chip-group>
            <v-btn @click="initBtn" color="#6164ff" variant="text">
                <v-icon>mdi-refresh</v-icon>
            </v-btn>
        </div>

        <!-- 상품 목록과 페이지네이션 -->
        <div class="trading-list">
            <v-list
                v-if="paginatedProducts.length > 0"
                class="scrollable-list"
                lines="two"
                item-props
            >
                <v-list-item
                    v-for="product in paginatedProducts"
                    :key="product.id"
                    :value="product.id"
                    @click="showPosts(product.id)"
                    class="list-item"
                    rounded="lg"
                    elevation="1"
                >
                    <template v-slot:prepend>
                        <v-img
                            :src="product.images && product.images.length > 0 ? product.images[0] : 'no-image.png'"
                            class="avatar"
                        />
                    </template>
                    <v-list-item-title class="title-text">{{ product.title }}</v-list-item-title>
                    <v-list-item-subtitle class="subtitle-text">
                        <span>좋아요 {{ product.likesCount }} • 조회수 {{ product.views }}</span>
                    </v-list-item-subtitle>
                </v-list-item>
            </v-list>

            <!-- 데이터가 없을 때 표시 -->
            <v-card v-else class="no-data-card" flat>
                <v-card-text class="text-center">
                    <v-icon size="64" color="#6164ff" class="mb-4">mdi-database-off</v-icon>
                    <h3 class="no-data-title">데이터가 없습니다</h3>
                    <p class="no-data-subtitle">선택한 카테고리나 검색어에 해당하는 상품이 없습니다.</p>
                </v-card-text>
            </v-card>

            <v-pagination v-model="currentPage" :length="totalPages" color="#6164ff" class="pagination" />

            <!-- 새 게시글 추가를 위한 플로팅 버튼 -->
            <v-btn
                v-if="isOnlyPostList()"
                fab
                dark
                icon="mdi-plus"
                size="x-large"
                @click="addPost"
                class="fab-button"
                color="#6164ff"
            />
        </div>
    </div>
</template>

<script>
import { RouterView } from 'vue-router';
import { mapState, mapMutations } from 'vuex';
import axios from 'axios';

export default {
    data: () => ({
        products: [], // 백엔드에서 가져온 모든 상품 데이터
        filteredProducts: [], // 필터링된 상품 데이터
        searchQuery: '', // 기본 검색 입력 값
        selectedCategoryIndex: 0, // 기본 선택된 카테고리 칩의 인덱스
        selectedCategory: 'ALL', // 기본 선택된 카테고리 값
        categories: [
            'ALL', 'ELECTRONICS', 'FASHION', 'HOME_APPLIANCES', 'BOOKS',
            'SPORTS', 'BABY', 'BEAUTY', 'VEHICLES', 'HOBBY', 'GENERAL'
        ],
        currentPage: 1, // 현재 페이지 번호
        totalPages: 0, // 백엔드에서 받은 총 페이지 수
        isLogin: false, // 로그인 상태
    }),
    props: {
        path: String,
        postListPath: String,
    },
    methods: {
        initialize() { // 컴포넌트 초기화
            this.isLogin = this.checkLogin();
            this.fetchPosts();
        },
        fetchPosts() { // 백엔드에서 게시글 가져오기
            const userInfo = JSON.parse(sessionStorage.getItem('userInformation') || '{}');
            const page = this.currentPage - 1; // Spring은 0부터 시작하므로 조정
            
            const requestData = this.postListPath.includes('/api/post/search') 
                ? { // post 경로인 경우
                    title: this.searchQuery, 
                    postCategory: this.selectedCategory 
                }
                : { // like 경로인 경우
                    customerId: userInfo?.customer?.id || '', 
                    title: this.searchQuery, 
                    postCategory: 'ALL' 
                };

            axios.post(this.postListPath, requestData, { params: { page } })
                .then(response => {
                    const newProducts = response.data.content.map(product => ({
                        ...product,
                        images: product.images?.map(base64 => `data:image/jpeg;base64,${base64}`) || []
                    }));
                    
                    this.products = newProducts;
                    this.filteredProducts = newProducts;
                    this.totalPages = response.data.totalPages;
                })
                .catch(error => {
                    console.error('게시물 불러오기 오류 : ', error);
                    this.products = [];
                    this.filteredProducts = [];
                    this.totalPages = 0;
                });
        },

        checkLogin() { // 사용자 로그인 여부 확인
            return !!sessionStorage.getItem('jwt');
        },
        filterPosts() { // 검색 및 카테고리 필터 적용
            this.currentPage = 1;
            this.fetchPosts();
        },
        initBtn() { // 필터 초기화 및 게시글 새로고침
            this.selectedCategory = 'ALL';
            this.searchQuery = '';
            this.selectedCategoryIndex = 0;
            this.currentPage = 1;
            this.fetchPosts();
        },
        filterProductsByCategory(category) { // 카테고리로 필터링
            this.selectedCategory = category;
            this.currentPage = 1;
            this.fetchPosts();
        },
        showPosts(id) { // 게시글 상세 보기
            const product = this.filteredProducts.find(p => p.id === id);
            this.SET_SHOWPRODUCT(product || {});
            this.SET_MODAL_OPEN(true);
            this.$router.push(`${this.path}/${id}`);
        },
        addPost() { // 새 게시글 추가 페이지로 이동
            this.SET_MODAL_OPEN(true);
            this.$router.push(`${this.path}/add`);
        },
        isOnlyPostList() { // 게시글 추가 버튼 표시 여부 확인
            return this.isLogin && this.path === '/post/list';
        },
        ...mapMutations(['SET_MODAL_OPEN', 'SET_SHOWPRODUCT']),
    },
    computed: {
        ...mapState(['backendUrl', 'showProduct']),
        paginatedProducts() { // 페이지네이션된 상품 데이터 반환
            return this.filteredProducts;
        },
    },
    watch: {
        currentPage() { // 페이지 변경 시 게시글 새로고침
            this.fetchPosts();
        },
    },
    created() {
        this.initialize();
    },
    destroyed() { // 컴포넌트 소멸 시 정리
        this.products = [];
        this.filteredProducts = [];
        this.categories = [];
        this.SET_SHOWPRODUCT({});
    },
};
</script>

<style scoped>
/* 메인 컨테이너 스타일 */
.trading-container {
    background-color: #f5f5f5;
    min-width: 80vw;
    min-height: 90vh;
    margin-top: 50px;
    padding: 16px;
}

/* 헤더 레이아웃 */
.trading-header {
    max-width: 600px;
    margin: 0 auto 16px auto;
    display: flex;
    gap: 16px;
    align-items: stretch;
}

.category-select { flex: 1; border-radius: 8px; }
.search-bar { flex: 3; border-radius: 8px; }
.search-btn { height: 56px; border-radius: 8px; }

/* 카테고리 칩 스타일 */
.trading-categories {
    display: flex;
    justify-content: space-between;
    max-width: 600px;
    margin: 0 auto 16px auto;
    overflow-x: auto;
}

.selected-chip {
    background-color: #6164ff;
    color: white !important;
}

/* 상품 목록 컨테이너 */
.trading-list {
    max-width: 600px;
    margin: 0 auto;
    position: relative;
    min-height: 70vh;
}

/* 스크롤 가능한 목록 */
.scrollable-list {
    max-height: 65vh;
    overflow-y: auto;
    background-color: transparent;
}

.scrollable-list::-webkit-scrollbar { display: none; }

/* 목록 아이템 스타일 */
.list-item {
    padding: 12px;
    margin-bottom: 12px;
    background-color: #fff;
    border-radius: 12px;
    transition: transform 0.2s ease;
}

.list-item:hover { transform: scale(1.02); }
.avatar { border-radius: 8px; width: 80px; height: 80px; object-fit: cover; }
.title-text { font-weight: 600; margin-left: 12px; }
.subtitle-text { margin-left: 12px; margin-top: 8px; font-size: 14px; color: #666; }
.subtitle-text span { white-space: nowrap; }

/* 플로팅 액션 버튼 */
.fab-button {
    position: absolute;
    right: 20px;
    bottom: 50px;
    z-index: 100;
    background-color: #6164ff;
    box-shadow: 0 4px 12px rgba(97, 100, 255, 0.3);
}

/* 페이지네이션 스타일 */
.pagination {
    position: absolute;
    bottom: 16px;
    left: 0;
    right: 0;
    display: flex;
    justify-content: center;
}

/* 데이터 없음 카드 스타일 */
.no-data-card {
    margin: 20px 0;
    padding: 20px;
    background-color: #f5f5f5;
    border-radius: 8px;
}

.no-data-title { color: #6164ff; font-size: 1.5rem; font-weight: 500; margin-bottom: 10px; }
.no-data-subtitle { color: #757575; font-size: 1rem; }

/* 반응형 조정 */
@media (max-width: 600px) {
    .trading-header { flex-direction: column; gap: 8px; }
    .search-bar, .category-select { width: 100%; }
    .fab-button { bottom: 100px; }
    .pagination { margin-top: 20px; position: relative; }
}
</style>