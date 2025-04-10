<!-- src/components/NotFound.vue -->
<template>
    <v-container>
      <div class="main-content">
        <div class="intro-card">
          <h1>404 - 페이지를 찾을 수 없습니다</h1>
          <p>입력하신 주소 "{{ invalidPath }}"는 존재하지 않습니다.<br />주소를 확인하거나 홈으로 돌아가세요.</p>
          <v-btn color="primary" @click="goHome" class="mt-4">홈으로 돌아가기</v-btn>
        </div>
      </div>
    </v-container>
  </template>
  
  <script>
  export default {
    name: 'NotFound',
    props: {
      invalidPath: {
        type: String,
        default: '',
      },
    },
    methods: {
      goHome() {
        this.$router.push('/');
      },
    },
  };
  </script>
  
  <style scoped>
  /* 레이아웃 스타일 */
  .main-content {
    display: grid;
    place-items: center;
    height: 100vh;
  }
  
  /* 카드 스타일 */
  .intro-card {
    background-color: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
    max-width: 500px;
    width: 100%;
  }
  
  /* 텍스트 스타일 */
  h1 {
    color: #6164ff;
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 16px;
  }
  
  p {
    color: #666;
    font-size: 16px;
    line-height: 1.5;
  }
  
  /* 버튼 스타일 (선택적) */
  .v-btn {
    border-radius: 8px;
    text-transform: none;
    font-weight: 500;
  }
  </style>