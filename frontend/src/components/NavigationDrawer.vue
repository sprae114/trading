<template>
    <v-navigation-drawer app permanent>
        <v-list>
            <v-list-item to="/">홈</v-list-item>
            <v-list-item to="/login/home">로그인</v-list-item>
            <v-list-item to="/login/register">회원가입</v-list-item>
            <v-list-item to="/login/updateMyInfo">내 정보 수정</v-list-item>
            <!-- <v-list-item to="/post/list">중고거래 글</v-list-item>
            <v-list-item to="/my/post">작성한 글 보기</v-list-item>
            <v-list-item to="/my/likes">좋아요 글 보기</v-list-item>
            <v-list-item to="/my/info">내정보 보기</v-list-item>
            <v-list-item to="/my/chatRoom">현재 참여중인 채팅 보기</v-list-item> -->
        </v-list>
    </v-navigation-drawer>
</template>

<script>
export default {
    name: 'NavigationDrawer'
};
</script>