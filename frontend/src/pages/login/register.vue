<template>
    <div>
        <OTPModalComponent :email="email" :url="url" />
        <v-container fluid>
            <div class="register-container">
                <v-card>
                    <v-card-title class="headline text-center">
                        <h2> 회원가입 </h2>
                    </v-card-title>
                    <v-card-text>
                        <v-form ref="form" v-model="valid">
                            <div class="rowclass">
                                <v-text-field v-model="name" :rules="[rules.required, rules.name]" label="이름" outlined dense></v-text-field>
                            </div>

                            <div class="rowclass">
                                <v-text-field v-model="email" :rules="[rules.required, rules.verificationEmail]" label="이메일" outlined dense :readonly="isOTPVaild"></v-text-field>
                                <v-btn @click="sendVerificationCode" color="dark" variant="outlined" :readonly="isOTPVaild" :disabled="!isVaildEmail">인증 코드 전송</v-btn>
                            </div>

                            <div class="rowclass">
                                <v-text-field v-model="password" :rules="[rules.required, rules.password]" label="비밀번호" type="password" outlined dense></v-text-field>

                                <v-text-field v-model="passwordCheck" :rules="[rules.required, rules.passwordCheck]" label="비밀번호확인" type="password" outlined dense></v-text-field>
                            </div>
                        </v-form>
                    </v-card-text>
                    <v-divider></v-divider>
                    <v-card-actions>
                        <v-btn color="primary" variant="outlined" @click="goToLogin">로그인 페이지</v-btn>
                        <v-btn variant="outlined" @click="submit">회원가입 하기</v-btn>
                    </v-card-actions>
                </v-card>
            </div>
        </v-container>
    </div>
</template>

<script>
import OTPModalComponent from '@/components/modal/OTPModalComponent.vue';
import { mapState, mapMutations } from 'vuex';
import axios from 'axios';

export default {
    data() {
        return {
            name: '',
            email: '',
            verificationCode: '',
            password: '',
            passwordCheck: '',
            isVaildEmail: false,
            valid: false,
            url: '/api/login/register/auth',
            rules: {
                required: value => !!value || '필수 입력 항목입니다.',
                name: value => /^[a-zA-Z가-힣]{2,10}$/.test(value) || '이름은 2자 이상 10자 이하의 한글 또는 영어로 입력해야 합니다.',
                email: value => /.+@.+\..+/.test(value) || '유효한 이메일 형식이 아닙니다.',
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
                password: value => /^(?=.*[a-zA-Z])(?=.*\d).{5,}$/.test(value) || '비밀번호는 최소 5자 이상, 영문자와 숫자를 포함해야 합니다.',
                passwordCheck: value => value === this.password || '비밀번호가 일치하지 않습니다.',
            },
            registerErrorMessages: '이메일 인증을 먼저 해주세요.',
        };
    },

    components: {
        OTPModalComponent
    },

    methods: {
        sendVerificationCode() {
            if (this.isVaildEmail) {
                axios.get(`${this.backendUrl}/api/login/register/auth?email=${this.email}`)
                    .then(() => {
                        this.SET_MODAL_OPEN(true);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증 코드가 전송되었습니다. 이메일을 확인해주세요.']);
                    })
                    .catch((error) => {
                        if (error.response.status === 400) {
                            this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이미 가입된 이메일입니다.']);
                        } else {
                            this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증 코드 전송에 실패했습니다.']);
                        }
                    });
            } else {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '유효한 이메일을 입력해주세요.']);
            }
        },
        submit() {
            if (!this.isOTPVaild) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이메일 인증을 먼저 해주세요.']);
            } else if (!this.valid) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '필수 입력 항목을 모두 입력해주세요.']);
            } else {
                axios.post(`${this.backendUrl}/api/login/register`, {
                    name: this.name,
                    email: this.email,
                    pwd: this.password,
                })
                    .then((response) => {
                        console.log(response);
                        this.SET_OTP_VAILD(false);
                        this.$router.push('/login/home');
                        this.SHOW_NOMAL_ALERT_MESSAGE(['success', '회원가입 성공했습니다!']);
                    })
                    .catch((error) => {
                        console.error(error);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '회원가입에 실패했습니다. 다시 시도해주세요.']);
                    });
            }
        },

        goToLogin() {
            this.$router.push('/login/home');
        },
        ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_MODAL_OPEN', 'SET_OTP_VAILD']),
    },

    computed: {
        ...mapState(['isAlertOpen', 'isModalOpen', 'isOTPVaild', 'alertTpye', 'alertMessage', 'backendUrl']),
    },
};
</script>

<style scoped>
.register-container {
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100vh;
}

.rowclass {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 20px;
    gap: 20px;
}

.v-card {
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    max-width: 600px;
    width: 100%;
    padding: 10px;
}

.v-card-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
}
</style>
