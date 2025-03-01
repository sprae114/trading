<template>
    <v-container>
        <div class="main-content">
            <div class="intro-card">
                <h1>중고거래 웹사이트</h1>
                <p>개인간의 채팅을 통해서 중고거래 하는 사이트</p>
            </div>
        </div>
    </v-container>
</template>

<script>
    import { mapState, mapMutations } from 'vuex';

    export default {
        name: 'index',
        methods: {
            ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE']),
        },
        computed: {
            ...mapState(['isAlertOpen', 'alertTpye', 'alertMessage', 'isLoggedIn']),
        },
    };
</script>