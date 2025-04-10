<template>
  <v-app>
    <!-- 네비게이션 드로어 -->
    <NavigationDrawer />

    <!-- 드로어 토글 버튼 -->
    <v-btn v-if="!isDrawerOpen" @click.stop="TOGGLE_DRAWER()" class="drawer-toggle-btn" variant="text">
      <v-icon>mdi-menu</v-icon>
    </v-btn>

    <!-- 알림 -->
    <v-alert v-if="isAlertOpen" :type="alertType" class="floating-alert">
      {{ alertMessage }}
    </v-alert>
    
    <!-- 메인 콘텐츠 -->
    <v-main>
      <transition name="fade" mode="out-in">
        <router-view />
      </transition>    
    </v-main>
  </v-app>
</template>

<script>
import { mapState, mapMutations } from 'vuex';
import NavigationDrawer from '@/components/NavigationDrawer.vue';

export default {
  name: 'App',
  components: {
    NavigationDrawer // 네비게이션 드로어 컴포넌트
  },
  methods: {
    ...mapMutations(['TOGGLE_DRAWER']) // 드로어 토글 뮤테이션
  },
  computed: {
    ...mapState(['isAlertOpen', 'alertType', 'alertMessage', 'isDrawerOpen']) // 필요한 스토어 상태
  }
};
</script>

<style scoped>
/* 메인 콘텐츠 배경 스타일 */
.v-main {
  background-color: #f5f5f5;
}

/* 드로어 토글 버튼 스타일 */
.drawer-toggle-btn {
  position: fixed;
  top: 10px;
  left: 10px;
  z-index: 1001;
}

/* 플로팅 알림 스타일 */
.floating-alert {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1000;
  max-width: 500px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

/* 알림 등장 애니메이션 */
.v-alert.floating-alert {
  animation: slideIn 0.3s ease-in-out;
}

/* 페이지 전환 페이드 애니메이션 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.7s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>