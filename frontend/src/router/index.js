// router/index.js
import { createRouter, createWebHistory } from 'vue-router';
import HomePage from '@/pages/HomePage.vue';
import SafetyDiagnosis from '@/pages/SafetyDiagnosis.vue';
import RegisterResult from '@/pages/RegisterResult.vue';
import Preview from '@/pages/checklist/Preview.vue';
import ReferenceContract from '@/pages/referencecontracts/ReferenceContract.vue';
import GlossaryBook from '@/pages/GlossaryBook.vue';
import GuidebookPage from '@/pages/GuidebookPage.vue';
import MyPage from '@/pages/MyPage.vue';
import NonDiagnosis from '@/pages/checklist/ForNoneDiagnosis.vue';
import CheckList from '@/pages/checklist/Checklist.vue';
import FinalReportPage from '@/pages/FinalReportPage.vue';
import AgreementPage from '@/pages/AgreementPage.vue';
import LoginPage from '@/pages/login/LoginPage.vue';
import ContractListPage from '@/pages/listpages/ContractListPage.vue';
import ReportListPage from '@/pages/listpages/ReportListPage.vue';

// 카테고리 및 용어 관련 추가
import CategoryAll from '@/pages/category/CategoryAll.vue';
import CategoryManager from '@/pages/category/CategoryManager.vue';
import CategoryAdd from '@/pages/category/CategoryAdd.vue';
import CategoryEdit from '@/pages/category/CategoryEdit.vue';

import TermManager from '@/pages/term/TermManager.vue';
import TermAdd from '@/pages/term/TermAdd.vue';
import TermEdit from '@/pages/term/TermEdit.vue';
// 특약사항 관련 추가
import SpecialContractsManager from '@/pages/special-contracts/SpecialContractsManager.vue';
import SpecialContractsAdd from '@/pages/special-contracts/SpecialContractsAdd.vue';
import SpecialContractsEdit from '@/pages/special-contracts/SpecialContractsEdit.vue';
import KakaoCallback from '@/pages/login/KakaoCallback.vue';

const routes = [
  { path: '/', name: 'home', component: HomePage },
  {
    path: '/safety-check', // 매물 안전성 진단 페이지
    name: 'safety-check',
    component: SafetyDiagnosis,
    meta: { requiresAuth: true },
  },
  {
    path: '/safety-check/:registerId', // 등기부등본 분석 결과 페이지
    name: 'RegisterResult',
    component: RegisterResult,
    meta: { requiresAuth: true },
  },
  { path: '/checklist', name: 'preview', component: Preview },
  {
    path: '/agreement', // 면책고지 경로
    name: 'AgreementPage',
    component: AgreementPage,
    meta: { hideHeader: true, requiresAuth: true },
  },
  {
    path: '/referencecontracts',
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'ReferenceContract',
        component: () =>
          import('@/pages/referencecontracts/ReferenceContract.vue'),
      },
      {
        path: 'success/:id?',
        name: 'ReferenceContractSuccess',
        component: () =>
          import('@/pages/referencecontracts/ReferenceContractSuccess.vue'),
        props: true,
      },
    ],
  },
  {
    path: '/reference-guidebook',
    name: 'guidebookPage',
    component: GuidebookPage,
  },
  { path: '/glossary', name: 'glossary', component: GlossaryBook },
  { path: '/my', name: 'my', component: MyPage, meta: { requiresAuth: true } },
  {
    path: '/checklist/nondiagnosis',
    name: 'nondiagnosis',
    component: NonDiagnosis,
  },
  {
    path: '/checklist/checklist',
    name: 'checklist',
    component: CheckList,
  },
  { path: '/login', name: 'Login', component: LoginPage },

  {
    path: '/oauth/callback/kakao',
    name: 'KakaoCallback',
    component: KakaoCallback,
  },

  // 관리자 페이지
  {
    path: '/category',
    component: CategoryAll,
    meta: { requiresAuth: true, requiresAdmin: true, hideHeader: true },
    children: [
      // 카테고리 관리
      { path: '', name: 'CategoryManager', component: CategoryManager },
      { path: 'add', name: 'CategoryAdd', component: CategoryAdd },
      {
        path: 'edit/:id',
        name: 'CategoryEdit',
        component: CategoryEdit,
        props: true,
      },
      // 용어 관리
      { path: 'term', name: 'TermManager', component: TermManager },
      { path: 'term/add', name: 'TermAdd', component: TermAdd },
      {
        path: 'term/edit/:id',
        name: 'TermEdit',
        component: TermEdit,
        props: true,
      },
      // 특약사항 관리
      {
        path: 'special',
        name: 'SpecialContractsManager',
        component: SpecialContractsManager,
      },
      {
        path: 'special/add',
        name: 'SpecialContractsAdd',
        component: SpecialContractsAdd,
      },
      {
        path: 'special/edit/:id',
        name: 'SpecialContractsEdit',
        component: SpecialContractsEdit,
        props: true,
      },
    ],
  },
  {
    path: '/recommendation',
    name: 'SpecialContractsRecommendation',
    component: () =>
      import('@/pages/special-contracts/SpecialContractsRecommendation.vue'),
  },
  {
    path: '/final-report',
    name: 'finalReportPage',
    component: FinalReportPage,
    meta: { requiresAuth: true },
  },

  {
    path: '/contract-list',
    name: 'ContractListPage',
    component: () => import('@/pages/listpages/ContractListPage.vue'),
    meta: { requiresAuth: true },
  },

  {
    path: '/report-list',
    name: 'reportList',
    component: () => import('@/pages/listpages/ReportListPage.vue'),
    meta: { requiresAuth: true },
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

// 라우터 가드 설정
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore();

  if (to.meta.requiresAuth) {
    if (!auth.isLoggedIn) {
      return next('/login');
    }

    const payload = JSON.parse(atob(auth.token.split('.')[1]));

    // 1) 관리자 전용 페이지
    if (to.meta.requiresAdmin) {
      if (payload.role !== 'ROLE_ADMIN') {
        alert('관리자만 접근 가능한 페이지입니다.');
        return next('/'); // 일반 회원은 홈으로
      }
    }
    // 2) 회원 전용 페이지
    else {
      if (payload.role === 'ROLE_ADMIN') {
        alert('회원 전용 페이지입니다.');
        return next('/category'); // 관리자면 카테고리로 보내기
      }
    }

    try {
      await axios.get('/api/check-access' + to.path, {
        headers: { Authorization: `Bearer ${auth.token}` },
      });
      return next();
    } catch {
      return next('/login');
    }
  }

  return next();
});

export default router;
