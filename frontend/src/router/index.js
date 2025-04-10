/**
 * router/index.ts
 *
 * Automatic routes for `./src/pages/*.vue`
 */

import { createRouter, createWebHistory } from 'vue-router/auto';
import { setupLayouts } from 'virtual:generated-layouts';
import { routes } from 'vue-router/auto-routes';

// 404 페이지 컴포넌트 수동 임포트
import NotFound from '@/pages/error/NotFound.vue';

// 기존 자동 생성된 라우트에 404 경로 추가
const customRoutes = [
  ...setupLayouts(routes), // 자동 생성된 라우트 유지
  {
    path: '/:pathMatch(.*)*', // 모든 정의되지 않은 경로를 처리
    name: 'NotFound',
    component: NotFound,
    props: (route) => ({ invalidPath: route.path }), // 잘못된 경로 전달
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: customRoutes, // 수정된 라우트 사용
});

// Workaround for dynamic import error
router.onError((err, to) => {
  if (err?.message?.includes?.('Failed to fetch dynamically imported module')) {
    if (!localStorage.getItem('vuetify:dynamic-reload')) {
      console.log('Reloading page to fix dynamic import error');
      localStorage.setItem('vuetify:dynamic-reload', 'true');
      location.assign(to.fullPath);
    } else {
      console.error('Dynamic import error, reloading page did not fix it', err);
    }
  } else {
    console.error(err);
  }
});

router.isReady().then(() => {
  localStorage.removeItem('vuetify:dynamic-reload');
});

export default router;