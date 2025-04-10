<template>
    <div class="trading-container">
        <RouterView />

        <!-- 헤더: 검색 바 -->
        <div class="trading-header">
            <v-text-field
                v-model="searchQuery"
                prepend-inner-icon="mdi-magnify"
                placeholder="대화방 검색..."
                variant="solo"
                clearable
                color="#6164ff"
                class="search-bar"
                @input="filterPost"
            />
            <v-btn color="#6164ff" size="large" dark @click="filterPost" class="search-btn">검색</v-btn>
        </div>

        <!-- 채팅방 목록과 페이지네이션 -->
        <div class="trading-list">
            <v-list v-if="paginatedChatingRooms.length > 0" class="scrollable-list" lines="two" item-props>
                <v-list-item
                    v-for="chatingRoom in paginatedChatingRooms"
                    :key="chatingRoom.id"
                    :value="chatingRoom.id"
                    @click="showChatting(chatingRoom.id)"
                    class="list-item"
                    rounded="lg"
                    elevation="1"
                >
                    <v-list-item-title class="title-text">
                        {{ chatingRoom.name }}
                    </v-list-item-title>
                    <v-list-item-subtitle class="subtitle-text">
                        <span class="sender-receiver">
                            {{ chatingRoom.sender }} → {{ chatingRoom.receiver }}
                        </span>
                        <span class="created-at">
                            {{ formatDate(chatingRoom.createdAt) }}
                        </span>
                    </v-list-item-subtitle>
                </v-list-item>
            </v-list>

            <!-- 데이터가 없을 때 표시 -->
            <v-card v-else class="no-data-card" flat>
                <v-card-text class="text-center">
                    <v-icon size="64" color="#6164ff" class="mb-4">mdi-database-off</v-icon>
                    <h3 class="no-data-title">데이터가 없습니다</h3>
                    <p class="no-data-subtitle">검색어에 해당하는 대화방이 없습니다.</p>
                </v-card-text>
            </v-card>

            <v-pagination
                v-model="currentPage"
                :length="totalPages"
                color="#6164ff"
                class="pagination"
                @input="updatePage"
            />
        </div>
    </div>
</template>

<script>
import axios from 'axios';
import { RouterView } from 'vue-router';
import { mapState, mapMutations } from 'vuex';

export default {
    name: 'ChattingRoomListComponent',
    data: () => ({
        chatingRooms: [], // 채팅방 목록
        filteredChatingRooms: [], // 필터링된 채팅방 목록
        searchQuery: '', // 검색어
        currentPage: 1, // 현재 페이지
        itemsPerPage: 5, // 페이지당 항목 수
        totalPages: 0, // 총 페이지 수
        chattingAlertMessageCount: {}, // 채팅방별 알림 메시지 수
        userInfo: {}, // 사용자 정보
    }),
    computed: {
        ...mapState(['isModalOpen', 'backendUrl']),
        totalPages() { // 총 페이지 수 계산
            return Math.ceil(this.filteredChatingRooms.length / this.itemsPerPage);
        },
        paginatedChatingRooms() { // 페이지네이션된 채팅방 목록 반환
            const start = (this.currentPage - 1) * this.itemsPerPage;
            const end = start + this.itemsPerPage;
            return this.filteredChatingRooms.slice(start, end);
        },
    },
    methods: {
        initialize() { // 컴포넌트 초기화
            this.userInfo = JSON.parse(sessionStorage.getItem('userInformation') || '{}');
            this.searchRoom();
            this.filteredChatingRooms = [...this.chatingRooms];
            this.chattingAlertMessageCount = {
                1: 2,
                3: 1,
                5: 3,
            };
        },
        getRoom() { // 전체 채팅방 조회 (사용되지 않음)
            axios.get(`${this.backendUrl}/api/chat`)
                .then(response => {
                })
                .catch(() => {
                });
        },
        searchRoom() { // 채팅방 검색
            const page = this.currentPage - 1; // Spring은 0부터 시작
            const requestDto = {
                searchText: this.searchQuery,
                senderId: this.userInfo.customer.id,
            };
            axios.post(`${this.backendUrl}/api/chat`, requestDto, { params: { page } })
                .then(response => {
                    this.chatingRooms = [...response.data.content];
                    this.filteredChatingRooms = [...response.data.content];
                    this.totalPages = response.data.totalPages;
                })
                .catch(() => {
                    this.chatingRooms = [];
                    this.filteredChatingRooms = [];
                    this.totalPages = 0;
                });
        },
        filterPost() { // 검색어로 채팅방 필터링
            this.currentPage = 1;
            this.filteredChatingRooms = this.chatingRooms.filter(room =>
                room.name.toLowerCase().includes(this.searchQuery.toLowerCase())
            );
        },
        showChatting(roomId) { // 채팅방 상세 보기
            this.SET_MODAL_OPEN(true);
            this.$router.push(`/chat/list/${roomId}`);
        },
        updatePage() { // 페이지 업데이트 (현재 빈 함수, 필요 시 구현)
            this.searchRoom();
        },
        formatDate(dateString) { // 날짜 포맷팅
            const date = new Date(dateString);
            return date.toLocaleString('ko-KR', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit',
            });
        },
        ...mapMutations(['SET_MODAL_OPEN']),
    },
    created() {
        this.initialize();
    },
};
</script>

<style scoped>
/* 전체 컨테이너 스타일 */
.trading-container {
    background-color: #f5f5f5;
    min-width: 80vw;
    min-height: 90vh;
    margin-top: 50px;
    padding: 16px;
}

/* 헤더 스타일 */
.trading-header {
    max-width: 600px;
    margin: 0 auto 16px auto;
    display: flex;
    gap: 16px;
    align-items: stretch;
}

.search-bar {
    flex: 3;
    border-radius: 8px;
}

.search-btn {
    height: 56px;
    border-radius: 8px;
}

/* 거래 리스트 스타일 */
.trading-list {
    max-width: 600px;
    margin: 0 auto;
    position: relative;
    min-height: 70vh;
}

/* 스크롤 가능한 리스트 스타일 */
.scrollable-list {
    max-height: 65vh;
    overflow-y: auto;
    background-color: transparent;
}

.scrollable-list::-webkit-scrollbar {
    display: none;
}

/* 리스트 항목 스타일 */
.list-item {
    padding: 12px;
    margin-bottom: 12px;
    background-color: #fff;
    border-radius: 12px;
    transition: transform 0.2s ease;
}

.list-item:hover {
    transform: scale(1.02);
}

.avatar {
    border-radius: 8px;
    width: 80px;
    height: 80px;
    object-fit: cover;
}

.title-text {
    font-weight: 600;
    margin-left: 12px;
}

.subtitle-text {
    margin-left: 12px;
    margin-top: 8px;
    font-size: 14px;
    color: #666;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.sender-receiver {
    font-weight: 500;
    color: #424242;
}

.created-at {
    font-size: 12px;
    color: #888;
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

/* 데이터 없음 메시지 스타일 */
.no-data-card {
    margin: 20px 0;
    padding: 20px;
    background-color: #f5f5f5;
    border-radius: 8px;
}

.no-data-title {
    color: #6164ff;
    font-size: 1.5rem;
    font-weight: 500;
    margin-bottom: 10px;
}

.no-data-subtitle {
    color: #757575;
    font-size: 1rem;
}

/* 반응형 디자인 */
@media (max-width: 600px) {
    .trading-header {
        flex-direction: column;
        gap: 8px;
    }
    .search-bar {
        width: 100%;
    }
    .pagination {
        margin-top: 20px;
        position: relative;
    }
}
</style>