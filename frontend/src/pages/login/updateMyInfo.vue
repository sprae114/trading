<template>
    <v-container>
        <OTPModalComponent :email="email" :url="url" />
        <div class="find-password-container">
            <v-card>
                <v-card-title class="headline text-center">
                    <h2>내 정보 수정</h2>
                </v-card-title>
                <v-card-text>
                    <div class="rowclass">
                        <v-text-field v-model="email" :rules="[rules.required, rules.verificationEmail]" label="이메일"
                            outlined dense :readonly="isOTPVaild"></v-text-field>
                        <v-btn @click="sendVerificationCode" color="dark" variant="outlined" :readonly="isOTPVaild"
                            :disabled="isOTPVaild">인증 코드
                            전송</v-btn>
                    </div>

                    <v-text-field v-if="isOTPVaild" v-model="name" :rules="[rules.required, rules.name]"
                        label="이름" type="text" outlined dense></v-text-field>

                    <v-text-field v-if="isOTPVaild" v-model="password" :rules="[rules.required, rules.password]"
                        label="비밀번호" type="password" outlined dense></v-text-field>

                    <v-text-field v-if="isOTPVaild" v-model="passwordCheck"
                        :rules="[rules.required, rules.passwordCheck]" label="비밀번호확인" type="password" outlined
                        dense></v-text-field>

                    <v-card-actions v-if="isOTPVaild">
                        <v-btn @click="completePasswordChange" variant="outlined">비밀번호 수정</v-btn>
                    </v-card-actions>
                </v-card-text>
                <v-divider></v-divider>
                <v-card-actions>
                    <v-btn @click="goToLogin" color="primary" variant="outlined">로그인 페이지</v-btn>
                    <v-btn @click="goToRegister" variant="outlined">회원가입 페이지</v-btn>
                </v-card-actions>
            </v-card>
        </div>
    </v-container>
</template>

<script>
import OTPModalComponent from '@/components/modal/OTPModalComponent.vue';
import { mapState, mapMutations } from 'vuex';
import axios from 'axios';

export default {
    data() {
        return {
            email: '',
            name: '',
            password: '',
            passwordCheck: '',
            url: '/api/login/find-pw/auth',
            valid: false,
            isVaildEmail: false,
            rules: {
                required: value => !!value || '필수 입력 항목입니다.',
                verificationEmail: value => {
                    if (/.+@.+\..+/.test(value)) {
                        this.isVaildEmail = true;
                        return true;
                    }
                    else {
                        this.isVaildEmail = false;
                        return '유효한 이메일 형식이 아닙니다.';
                    }
                },
                name: value => /^[a-zA-Z가-힣]{2,10}$/.test(value) || '이름은 2자 이상 10자 이하의 한글 또는 영어로 입력해야 합니다.',
                password: value =>
                    /^(?=.*[a-zA-Z])(?=.*\d).{5,}$/.test(value) ||
                    '비밀번호는 최소 5자 이상, 영문자와 숫자를 포함해야 합니다.',
                passwordCheck: value => value === this.password || '비밀번호가 일치하지 않습니다.',
            },
            alertFindPasswordMessage: '인증을 완료하였습니다!',
            alertFindPasswordType: 'success',
        };
    },
    components: {
        OTPModalComponent
    },
    methods: {
        sendVerificationCode() { // 이메일 인증 코드 모달 띄우기
            if (this.isVaildEmail) {
                axios.get(`${this.backendUrl}/api/login/find-pw/auth?email=${this.email}`)
                    .then(() => {
                        this.SET_MODAL_OPEN(true);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증 코드가 전송되었습니다. 이메일을 확인해주세요.']);
                    })
                    .catch((error) => {
                        console.log(error);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증 코드 전송에 실패했습니다.']);
                    });
            } else {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '유효한 이메일을 입력해주세요.']);
            }
        },

        completePasswordChange() { // 비밀번호 변경 API
            if (this.password == '') { // 비밀번호 입력 확인
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '비밀번호를 입력해주세요.']);
                return;
            }

            if (this.password === this.passwordCheck) { // 비밀번호 변경이 완료
                axios.post(`${this.backendUrl}/api/login/find-pw`, {
                    email: this.email,
                    name: this.name,
                    pwd: this.password,
                })
                    .then(() => {
                        this.$router.push('/login/home'); // 로그인 페이지로 이동
                        this.SET_OTP_VAILD(false);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['success', '비밀번호 변경이 완료되었습니다.']);
                        // 비밀번호 변경 API 호출
                    })
                    .catch((error) => {
                        console.log(error);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '해당 이메일로 가입된 계정이 없습니다.']);
                        this.$router.push('/login/register'); // 회원가입 페이지로 이동
                    });
            }
        },

        goToLogin() {
            this.$router.push('/login/home'); // 로그인 페이지로 이동
        },

        goToRegister() {
            this.$router.push('/login/register'); // 로그인 페이지로 이동
        },
        ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_OTP_VAILD', 'SET_MODAL_OPEN']),
    },
    computed: {
        validEmail() {
            return this.rules.email(this.email);
        },

        ...mapState(['isAlertOpen', 'isModalOpen', 'isOTPVaild', 'alertTpye', 'alertMessage', 'backendUrl']),
    },
    destroyed() {
        this.SET_OTP_VAILD(false);
    },
};
</script>

<style scoped>
.find-password-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100vh;
}

.v-card {
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    max-width: 500px;
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

.rowclass {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
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
