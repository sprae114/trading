<template>
    <!-- OTP 모달 컨테이너 -->
    <div class="modal" v-if="isModalOpen" @click="closeModal">
      <v-card class="py-8 px-6 text-center mx-auto ma-4" elevation="12" max-width="400" width="100%" @click.stop>
        <h3 class="text-h6 mb-4">인증 코드 확인하기</h3>
        <div class="text-body-2">이메일을 확인하고 아래 번호를 붙여넣으세요.</div>
        <v-sheet color="surface">
          <v-otp-input v-model="otp" type="password" variant="solo" :length="6"></v-otp-input>
        </v-sheet>
        <v-btn class="my-4" color="purple" height="40" variant="flat" width="70%" @click="checkOTP">인증</v-btn>
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
        otp: '' // OTP 입력값
      };
    },
    props: {
      email: String, // 이메일 prop
      url: String // 요청 URL prop
    },
    methods: {
      closeModal() {  // 모달 닫기
        this.SET_MODAL_OPEN(false);
      },

      checkOTP() { // OTP 인증 확인
        axios.post(`${this.backendUrl}${this.url}`, {
          email: this.email,
          otp: this.otp
        })
          .then(() => {
            this.SET_OTP_VAILD(true);
            this.SET_MODAL_OPEN(false);
            this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증이 완료되었습니다.']);
          })
          .catch(() => {
            this.otp = '';
            this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증번호가 일치하지 않습니다.']);
          });
      },
      
      reOTP() { // 인증 코드 재전송
        axios.get(`${this.backendUrl}${this.url}?email=${this.email}`)
          .then(() => {
            this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증번호를 재전송했습니다.']);
            this.otp = '';
          })
          .catch(error => {
            this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증 코드 전송에 실패했습니다.']);
          });
      },

      ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_MODAL_OPEN', 'SET_OTP_VAILD']) 
    },
    computed: {
      ...mapState(['isModalOpen', 'isOTPValid', 'backendUrl'])
    }
  };
  </script>
  
  <style scoped>
  /* 모달 배경 */
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
  
  /* 카드 스타일 */
  .v-card {
    transition: transform 0.3s ease;
  }
  
  /* 안내 텍스트 */
  .text-body-2 {
    margin-bottom: 16px;
  }
  
  /* 재요청 링크 */
  .text-caption {
    margin-top: 16px;
  }
  
  /* 버튼 여백 */
  .my-4 {
    margin-top: 16px;
    margin-bottom: 16px;
  }
  </style>