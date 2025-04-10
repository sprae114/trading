<template>
    <v-container>
      <!-- OTP 모달 -->
      <OTPModalComponent :email="request.email" :url="url" />
 
      <div class="info-container">
        <v-card>
          <!-- 카드 제목 -->
          <v-card-title class="text-center">
            <h1>내 정보 수정</h1>
          </v-card-title>
          <!-- 입력 폼 -->
          <v-card-text>
            <div class="email-input-group">
              <v-text-field v-model="request.email" :rules="[rules.required, rules.verificationEmail]" label="이메일" variant="underlined" :readonly="isOTPValid"></v-text-field>
              <v-btn class="email-send-btn" @click="sendVerificationCode" variant="tonal" :disabled="isOTPValid">인증 코드 전송</v-btn>
            </div>
             <!-- OTP 인증 후 변경 가능하도록 설정 -->
            <div v-if="isOTPValid">
              <v-text-field v-model="request.name" :rules="[rules.required, rules.name]" label="이름" variant="underlined"></v-text-field>
              <v-text-field v-model="request.password" :rules="[rules.required, rules.password]" label="비밀번호" type="password" variant="underlined"></v-text-field>
              <v-text-field v-model="request.passwordCheck" :rules="[rules.required, rules.passwordCheck]" label="비밀번호 확인" type="password" variant="underlined"></v-text-field>
              <!-- 제출 버튼 -->
              <v-card-actions>
                <v-btn class="submit-btn" @click="completePasswordChange" color="primary" variant="tonal">비밀번호 수정</v-btn>
              </v-card-actions>
            </div>
          </v-card-text>
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
        request: {
          email: '', // 이메일 입력값
          name: '', // 사용자 이름
          password: '', // 새 비밀번호
          passwordCheck: '' // 비밀번호 확인
        },
        isValidEmail: false, // 이메일 유효성 상태
        url: '/api/login/find-pw/auth', // 인증 코드 요청 URL
        rules: {
          required: value => !!value || '필수 입력 항목입니다.', 
          verificationEmail: value => {
            const isValid = /.+@.+\..+/.test(value);
            this.isValidEmail = isValid;
            return isValid || '유효한 이메일 형식이 아닙니다.';
          },
          name: value => /^[a-zA-Z가-힣]{2,10}$/.test(value) || '2~10자, 한글 또는 영어', 
          password: value => /^(?=.*[a-zA-Z])(?=.*\d).{5,}$/.test(value) || '최소 5자, 영문자와 숫자 포함', 
          passwordCheck: value => value === this.request.password || '비밀번호가 일치하지 않습니다.' 
        }
      };
    },

    components: {
      OTPModalComponent // OTP 모달 컴포넌트
    },

    methods: {
      sendVerificationCode() { // 인증 코드 전송
        if (this.isValidEmail) {
          axios.get(`${this.backendUrl}${this.url}?email=${this.request.email}`)
            .then(() => {
              this.SET_MODAL_OPEN(true);
              this.SHOW_NOMAL_ALERT_MESSAGE(['success', '인증 코드가 전송되었습니다. 이메일을 확인해주세요.']);
            })
            .catch(error => {
              const status = error.response?.status;
              if (status === 404) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '등록된 이메일이 없습니다.']);
              } else {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증 코드 전송에 실패했습니다.']);
              }
            });
        } else {
          this.SHOW_NOMAL_ALERT_MESSAGE(['error', '유효한 이메일을 입력해주세요.']);
        }
      },

      completePasswordChange() { // 비밀번호 변경 완료
        if (!this.request.password) {
          this.SHOW_NOMAL_ALERT_MESSAGE(['error', '비밀번호를 입력해주세요.']);
          return;
        }
        if (this.request.password === this.request.passwordCheck) {
          axios.post(`${this.backendUrl}/api/login/find-pw`, {
            email: this.request.email,
            name: this.request.name,
            pwd: this.request.password
          })
            .then(() => {
              this.$router.push('/login/home');
              this.SET_OTP_VAILD(false);
              this.SHOW_NOMAL_ALERT_MESSAGE(['success', '비밀번호 변경이 완료되었습니다.']);
            })
            .catch(error => {
              const status = error.response?.status;
              if (status === 400) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '등록된 이름이 있습니다. 다른 이름을 입력해주세요.']);
              } else {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '해당 이메일로 가입된 계정이 없습니다.']);
                this.$router.push('/login/register');
              }
            });
        }
      },

      ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_OTP_VAILD', 'SET_MODAL_OPEN']) 
    },
    computed: {
      ...mapState(['isOTPValid', 'backendUrl'])
    },
    
    destroyed() {
      this.SET_OTP_VAILD(false);
    }
  };
  </script>
  
  <style scoped>
  /* 정보 수정 컨테이너 */
  .info-container {
    display: grid;
    place-items: center;
    height: 100vh;
  }
  
  /* 카드 스타일 */
  .v-card {
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    width: 500px;
    padding: 10px;
  }
  
  /* 제목 스타일 */
  h1 {
    color: #6164ff;
    font-size: 28px;
    font-weight: 700;
  }
  
  /* 이메일 입력 그룹 */
  .email-input-group {
    display: flex;
    justify-content: space-between;
    gap: 20px;
  }
  
  /* 인증 코드 전송 버튼 */
  .email-send-btn {
    height: 50px;
    border-radius: 8px;
  }
  
  /* 제출 버튼 */
  .submit-btn {
    width: 100%;
  }
  
  /* 반응형 디자인 */
  @media (max-width: 600px) {
    .v-card {
      width: 90%;
    }
  }
  </style>