<template>
    <div class="login-container">
        <v-card>
            <v-card-title class="headline text-center">
                <h2>로그인</h2>
            </v-card-title>
            <v-card-text>
                <v-form ref="form" v-model="valid" fast-fail @submit.prevent>
                    <v-text-field v-model="email" :rules="[rules.required, rules.email]" label="이메일" outlined
                        dense></v-text-field>
                    <v-text-field v-model="password" :rules="[rules.required]" label="비밀번호" type="password" outlined
                        dense></v-text-field>
                </v-form>
            </v-card-text>
            <v-divider></v-divider>
            <v-card-actions>

                <v-btn color="primary" variant="outlined" @click="submit">로그인 하기</v-btn>
                <v-btn @click="goToRegister" variant="outlined">회원가입 페이지</v-btn>
            </v-card-actions>
            <v-card-actions>
                <v-btn @click="goToPassword" variant="plain"> 비밀번호 찾기</v-btn>
            </v-card-actions>
        </v-card>
    </div>
</template>

<script>
    import axios from 'axios';
    import { mapState, mapMutations } from 'vuex';

    export default {
        data() {
            return {
                email: '',
                password: '',
                valid: false,
                rules: {
                    required: value => !!value || '필수 입력 항목입니다.',
                    email: valid => /.+@.+\..+/.test(valid) || '유효한 이메일 형식이 아닙니다.',
                },
            };
        },
        methods: {
            submit() { // 로그인 버튼 클릭 시
                if (this.email == '' || this.password == '') {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '아이디 또는 비밀번호를 입력해주세요.']);
                    return;
                }

                if (this.valid) {
                    axios.post(`${this.backendUrl}/api/login/home`, { // 백엔드 서버에 로그인 요청
                        email: this.email,
                        pwd: this.password
                    })
                        .then(response => {
                            this.SET_IS_LOIGIN(true); // 로그인 상태로 변경
                            sessionStorage.setItem('jwt', response.data.jwtToken);
                            sessionStorage.setItem('userInformation', JSON.stringify(response.data.customerDetails));
                            this.$router.push('/');
                            this.SHOW_NOMAL_ALERT_MESSAGE(['success', '로그인을 완료했습니다.']);
                        })
                        .catch((error) => {
                            console.log(error);
                            this.SHOW_NOMAL_ALERT_MESSAGE(['error', '아이디 또는 비밀번호가 일치하지 않습니다.']);
                        });
                }

                else {
                    this.SHOW_NOMAL_ALERT_MESSAGE(['error', '아이디 또는 비밀번호가 일치하지 않습니다.']);
                }
            },


            goToRegister() {
                this.$router.push('/login/register'); // 회원가입 페이지로 이동
            },

            goToPassword() {
                this.$router.push('/login/find-pw'); // 비밀번호 찾기 페이지로 이동
            },

            ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_IS_LOIGIN']),
        },
        computed: {
            ...mapState(['alertTpye', 'alertMessage', 'backendUrl', 'isLoggedIn']),
        },
    };
</script>

<style scoped>
    .login-container {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 100%;
        height: 100vh;

    }

    .v-card {
        box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        border-radius: 8px;
        max-width: 400px;
        /* 최대 너비 설정 */
        width: 100%;
        /* 너비를 100%로 설정하여 반응형 적용 */
        padding: 10px;

    }

    .v-form {
        display: flex;
        flex-direction: column;
        gap: 20px;
        margin-top: 20px;
    }

    .v-card-actions {
        display: flex;
        justify-content: center;
        gap: 20px;
    }


    /* 반응형 디자인을 위한 스타일 */
    @media (max-width: 600px) {
        .v-card {
            width: 90%;
            /* 작은 화면에서는 카드 너비를 90%로 설정 */
        }
    }
</style>