<template>
    <v-navigation-drawer v-model="isDrawerOpen" absolute temporary>
        <v-list>
            <v-list-item @click="TOGGLE_DRAWER()">
                <v-icon>mdi-close</v-icon>
            </v-list-item>
            <v-list-item @click="gotoLink('/')">홈</v-list-item>
            <v-list-item v-if="!isLoggedIn" @click="gotoLink('/login/home')">로그인</v-list-item>
            <v-list-item v-if="!isLoggedIn" @click="gotoLink('/login/register')">회원가입</v-list-item>
            <v-list-item v-if="isLoggedIn" @click="logout()">로그아웃</v-list-item>
            <v-list-item v-if="isLoggedIn" @click="gotoLink('/my/updateMyInfo')">내 정보 수정</v-list-item>
    
            <v-list-item @click="gotoLink('/post/list')">중고거래 글</v-list-item>
            <v-list-item v-if="isLoggedIn" @click="gotoLink('/chat/list')">현재 참여중인 채팅방</v-list-item>
            <v-list-item v-if="isLoggedIn" @click="gotoLink('/my/like/list')">좋아요 한 글</v-list-item>
        </v-list>
    </v-navigation-drawer>
</template>

<script>
import { mapState, mapMutations } from 'vuex';

export default {
    name: 'NavigationDrawer',
    methods: {
        logout() { // 로그아웃 처리
            sessionStorage.removeItem('jwt');
            sessionStorage.removeItem('userInformation');
            this.SET_IS_LOIGIN(false);
            this.SHOW_NOMAL_ALERT_MESSAGE(['success', '로그아웃을 완료했습니다.']);
            this.$router.push('/');
        },
        gotoLink(link) { // 페이지 이동 및 상태 초기화
            this.$router.push(link);
            this.SET_OTP_VAILD(false);
            this.SET_MODAL_OPEN(false);
            this.TOGGLE_DRAWER();
        },
        getLogin() { // 로그인 상태 확인
            if (sessionStorage.getItem('jwt')) {
                this.SET_IS_LOIGIN(true);
            } else {
                this.SET_IS_LOIGIN(false);
            }
        },
        ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_IS_LOIGIN', 'SET_MODAL_OPEN', 'SET_OTP_VAILD', 'TOGGLE_DRAWER']),
    },
    created() {
        this.getLogin();
    },
    computed: {
        ...mapState(['isDrawerOpen', 'isLoggedIn']),
    },
};
</script>

<style scoped>
/* 네비게이션 드로어 스타일 */
.v-navigation-drawer {
    background-color: #f5f5f5;
    z-index: 1000;
}

/* 리스트 아이템 스타일 */
.v-list-item {
    cursor: pointer;
}

.v-list-item:hover {
    background-color: #e0e0e0;
}
</style>