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
  export default {
    name: 'IndexPage' // 컴포넌트 이름
  };
  </script>
  
  <style scoped>
  /* 메인 콘텐츠 스타일 */
  .main-content {
    display: grid;
    place-items: center;
    height: 100vh;
  }
  
  /* 소개 카드 스타일 */
  .intro-card {
    background-color: #fff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
    max-width: 500px;
    width: 100%;
  }
  
  /* 제목 스타일 */
  h1 {
    color: #6164ff;
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 16px;
  }
  
  /* 설명 텍스트 스타일 */
  p {
    color: #666;
    font-size: 16px;
    line-height: 1.5;
  }
  </style>