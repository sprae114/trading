<template>
    <div>
      <!-- OTP 모달 -->
      <OTPModalComponent :email="request.email" :url="url" />
 
      <v-container fluid>
        <!-- 회원가입 컨테이너 -->
        <div class="register-container">
          <v-card>
            <v-card-title class="text-center">
              <h1>회원가입</h1>
            </v-card-title>
            <!-- 입력 폼 -->
            <v-card-text>
              <v-form ref="form" v-model="isFormValid">
                <v-text-field v-model="request.name" :rules="[rules.required, rules.name]" label="이름" variant="underlined"></v-text-field>
                <div class="input-row">
                  <v-text-field v-model="request.email" :rules="[rules.required, rules.verificationEmail]" label="이메일" variant="underlined" :readonly="isOTPValid"></v-text-field>
                  <v-btn @click="sendVerificationCode" color="dark" variant="outlined" :disabled="!isValidEmail || isOTPValid">인증 코드 전송</v-btn>
                </div>
                <div class="input-row">
                  <v-text-field v-model="request.password" :rules="[rules.required, rules.password]" label="비밀번호" type="password" variant="underlined"></v-text-field>
                  <v-text-field v-model="request.passwordCheck" :rules="[rules.required, rules.passwordCheck]" label="비밀번호 확인" type="password" variant="underlined"></v-text-field>
                </div>
              </v-form>
            </v-card-text>
            <!-- 제출 버튼 -->
            <v-card-actions>
              <v-btn color="primary" variant="tonal" @click="submit">회원가입</v-btn>
            </v-card-actions>
            <v-divider></v-divider>
            <!-- 링크 버튼 -->
            <v-card-actions>
              <v-btn variant="plain" @click="goToLogin">로그인하기</v-btn>
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
        request: {
          email: '', // 이메일 입력값
          name: '', // 사용자 이름
          password: '', // 비밀번호
          passwordCheck: '' // 비밀번호 확인
        },
        isValidEmail: false, // 이메일 유효성 상태
        isFormValid: false, // 폼 유효성 상태
        url: '/api/login/register/auth', // 인증 코드 요청 URL
        rules: {
          required: value => !!value || '필수 입력 항목입니다.', 
          name: value => /^[a-zA-Z가-힣]{2,10}$/.test(value) || '2~10자, 한글 또는 영어',
          verificationEmail: value => {
            const isValid = /.+@.+\..+/.test(value);
            this.isValidEmail = isValid;
            return isValid || '유효한 이메일 형식이 아닙니다.';
          },
          password: value => /^(?=.*[a-zA-Z])(?=.*\d).{5,}$/.test(value) || '최소 5자, 영문자와 숫자 포함', // 비밀번호 규칙
          passwordCheck: value => value === this.request.password || '비밀번호가 일치하지 않습니다.' // 비밀번호 일치 확인
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
              if (status === 400) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이미 가입된 이메일입니다.']);
              } else {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '인증 코드 전송에 실패했습니다.']);
              }
            });
        } else {
          this.SHOW_NOMAL_ALERT_MESSAGE(['error', '유효한 이메일을 입력해주세요.']);
        }
      },

      submit() { // 회원가입 제출
        if (!this.isOTPValid) {
          this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이메일 인증을 먼저 해주세요.']);
        } else if (!this.isFormValid) {
          this.SHOW_NOMAL_ALERT_MESSAGE(['error', '필수 입력 항목을 모두 입력해주세요.']);
        } else {
          axios.post(`${this.backendUrl}/api/login/register`, {
            name: this.request.name,
            email: this.request.email,
            pwd: this.request.password
          })
            .then(() => {
              this.SET_OTP_VAILD(false);
              this.$router.push('/login/home');
              this.SHOW_NOMAL_ALERT_MESSAGE(['success', '회원가입 성공했습니다!']);
            })
            .catch(error => {
              const status = error.response?.status;
              if (status === 400) {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이미 가입된 이메일입니다.']);
              } else {
                this.SHOW_NOMAL_ALERT_MESSAGE(['error', '회원가입에 실패했습니다. 다시 시도해주세요.']);
              }
            });
        }
      },

      goToLogin() {
        // 로그인 페이지로 이동
        this.$router.push('/login/home');
      },
      ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_MODAL_OPEN', 'SET_OTP_VAILD']) 
    },

    computed: {
      ...mapState(['isOTPValid', 'backendUrl'])
    },
    destroyed() {
      this.SET_OTP_VAILD(false);
      this.SET_MODAL_OPEN(false);
    }
  };
  </script>
  
  <style scoped>
  /* 회원가입 컨테이너 */
  .register-container {
    display: grid;
    place-items: center;
    height: 100vh;
  }
  
  /* 카드 스타일 */
  .v-card {
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    width: 600px;
    padding: 10px;
  }
  
  /* 입력 행 스타일 */
  .input-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
    gap: 20px;
  }
  
  /* 버튼 그룹 스타일 */
  .v-card-actions {
    display: flex;
    justify-content: center;
  }
  
  /* 제출 버튼 스타일 */
  .v-card-actions .v-btn {
    width: 100%;
    margin-bottom: 10px;
  }
  
  /* 제목 스타일 */
  h1 {
    color: #6164ff;
    font-size: 28px;
    font-weight: 700;
  }
  </style>