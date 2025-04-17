<template>
    <div class="login-container">
      <v-card>
        <v-card-title class="text-center">
          <h1>로그인</h1>
        </v-card-title>

        <!-- 로그인 폼 -->
        <v-card-text>
          <v-form ref="form" v-model="isFormValid" @submit.prevent>
            <v-text-field v-model="email" :rules="[rules.required, rules.email]" label="이메일" variant="underlined"></v-text-field>
            <v-text-field v-model="password" :rules="[rules.required]" label="비밀번호" type="password" variant="underlined"></v-text-field>
          </v-form>
        </v-card-text>
        
        <!-- 로그인 버튼 -->
        <v-card-actions>
          <v-btn class="submit-btn" @click="submit" color="primary" variant="tonal">로그인하기</v-btn>
        </v-card-actions>
  
        <v-divider></v-divider>
  
        <!-- 추가 링크 버튼 -->
        <v-card-actions>
          <v-btn @click="goToRegister" variant="plain">이메일 회원가입</v-btn>
          <v-btn @click="goToPassword" variant="plain">비밀번호 찾기</v-btn>
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
      email: '', // 이메일 입력값
      password: '', // 비밀번호 입력값
      isFormValid: false, // 폼 유효성 상태
      rules: {
        required: value => !!value || '필수 입력 항목입니다.', // 필수 입력 규칙
        email: value => /.+@.+\..+/.test(value) || '유효한 이메일 형식이 아닙니다.' // 이메일 형식 규칙
      }
    };
  },
  methods: {
    submit() { // 로그인 제출
      if (!this.email || !this.password) { // 입력값이 비어 있는 경우
        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '이메일 또는 비밀번호를 입력해주세요.']);
        return;
      }

      
      if (this.isFormValid) { // 폼이 유효할 때 로그인 요청
        axios.post(`${this.backendUrl}/api/login/home`, {
          email: this.email,
          pwd: this.password
        })
          .then(response => {
            this.saveStorage(response.data);
            this.$router.push('/');
            this.SHOW_NOMAL_ALERT_MESSAGE(['success', '로그인을 완료했습니다.']);
          })
          .catch(error => {
            if (error.response.status === 404) {
              this.SHOW_NOMAL_ALERT_MESSAGE(['error', '회원가입을 먼저 해주세요.']);
            } else {
              this.SHOW_NOMAL_ALERT_MESSAGE(['error', '아이디 또는 비밀번호가 일치하지 않습니다.']);
            }
          });
      } else {
        this.SHOW_NOMAL_ALERT_MESSAGE(['error', '아이디 또는 비밀번호가 일치하지 않습니다.']);
      }
    },

    goToRegister() { // 회원가입 페이지로 이동
      this.$router.push('/login/register');
    },

    goToPassword() { // 비밀번호 찾기 페이지로 이동
      this.$router.push('/login/password');
    },

    saveStorage(data) { // 로그인 정보 저장
      this.SET_IS_LOIGIN(true);
      sessionStorage.setItem('jwt', data.jwtToken);
      const customerDetails = data.customerDetails;
      const filteredCustomerDetails = JSON.stringify(customerDetails, (key, value) => {
        if (key === "createdAt" || key === "updatedAt") return undefined;
        return value;
      });
      sessionStorage.setItem('userInformation', filteredCustomerDetails);
    },
    ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE', 'SET_IS_LOIGIN'])
  },

  computed: {
    ...mapState(['backendUrl'])
  }
};
</script>

<style scoped>
/* 로그인 컨테이너 스타일 */
.login-container {
  display: grid;
  place-items: center;
  height: 100vh;
}

/* 카드 스타일 */
.v-card {
  box-shadow: 0 2px promotions10px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  width: 400px;
  padding: 10px;
}

/* 제목 스타일 */
h1 {
  color: #6164ff;
  font-size: 28px;
  font-weight: 700;
}

/* 폼 스타일 */
.v-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 20px;
}

/* 제출 버튼 스타일 */
.submit-btn {
  width: 100%;
  margin-bottom: 15px;
}

/* 링크 버튼 스타일 */
.v-card-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
}
</style>