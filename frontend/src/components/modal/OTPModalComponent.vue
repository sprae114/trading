<template>
    <div class="modal" v-if="isModalOpen" @click="closeModal">
        <v-card class="py-8 px-6 text-center mx-auto ma-4" elevation="12" max-width="400" width="100%" @click.stop>
            <h3 class="text-h6 mb-4">인증 코드 확인하기</h3>
            <div class="text-body-2">
                이메일을 확인하고 아래 번호를 붙여넣으세요.
            </div>

            <v-sheet color="surface">
                <v-otp-input v-model="otp" type="password" variant="solo" :length="6"></v-otp-input>
            </v-sheet>

            <v-btn class="my-4" color="purple" height="40" text="인증" variant="flat" width="70%" @click="checkOTP">
            </v-btn>

            <div class="text-caption">
                인증번호를 못 받으셨나요? <a href="#" @click="reOTP">재요청</a>
            </div>
        </v-card>
    </div>
</template>

<script>
    import { mapState, mapMutations } from 'vuex';
    import axios from 'axios';

    export default {
        data() {
            return {
                otp: '',
                alertOTPMessage: '인증번호가 일치하지 않습니다.',
                alertOTPtype: 'error',
            };
        },

        props: {
            email: String,
            url: String,
        },

        methods: {
            closeModal() { // 모달 닫기
                this.SET_MODAL_OPEN(false);
            },

            sendVaildNumber() { // 이메일 인증 코드 재전송
                console.log(this.email);
                axios.get(`${this.backendUrl}${this.url}?email=${this.email}`, {})
                    .then((response) => {
                        console.log(response);
                    })
                    .catch((error) => {
                        console.error(error);
                    });
            },

            checkOTP() { // 인증 코드 확인
                axios.post(`${this.backendUrl}${this.url}`, {
                    email: this.email,
                    otp: this.otp,
                })
                    .then((response) => {
                        console.log(response);
                        console.log('인증 코드 확인');
                        this.SET_OTP_VAILD(true);
                        this.SET_MODAL_OPEN(false);
                        this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증이 완료되었습니다.']);
                    })
                    .catch((error) => {
                        console.error(error);
                        console.log('인증 코드 불일치');
                        this.otp = '';
                        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증번호가 일치하지 않습니다.']);
                    });
            },

            reOTP() { // 인증 코드 재요청
                this.sendVaildNumber();
                console.log('인증 코드 재요청');
                this.otp = '';
                this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증번호를 재전송했습니다.']);
            },

            ...mapMutations(['SHOW_NOMAL_ALERT', 'SHOW_NOMAL_ALERT_MESSAGE', 'SET_MODAL_OPEN', 'SET_OTP_VAILD']),
        },
        computed: {
            ...mapState(['isAlertOpen', 'isModalOpen', 'isOTPVaild', 'alertTpye', 'alertMessage', 'backendUrl']),
        },
    };
</script>

<style scoped>
    .v-alert {
        position: fixed;
        top: 0;
        left: 50%;
        /* 화면의 중앙으로 이동 */
        transform: translateX(-50%);
        /* 요소의 너비의 절반만큼 왼쪽으로 이동 */
        z-index: 999;
        width: 25%;
        height: auto;
    }

    .modal {
        display: block;
        position: fixed;
        z-index: 101;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        overflow: auto;
        background-color: rgba(0, 0, 0, 0.4);
    }

    .v-card {
        transition: transform 0.3s ease;
    }

    .close-button {
        color: white;
        background-color: #ff5252;
        /* 닫기 버튼 색상 */
        border: none;
        border-radius: 4px;
        padding: 8px 16px;
        font-size: 16px;
        cursor: pointer;
        margin-top: 20px;
    }

    .close-button:hover {
        background-color: #ff1744;
        /* 마우스 오버 시 색상 변경 */
    }

    /* 추가적인 스타일 */
    .text-body-2 {
        margin-bottom: 16px;
    }

    .text-caption {
        margin-top: 16px;
    }

    .my-4 {
        margin-top: 16px;
        margin-bottom: 16px;
    }
</style>