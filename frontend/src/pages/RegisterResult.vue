<template>
  <div v-if="result && result.analysis !== null" class="list-group">
    <div class="d-flex flex-column align-items-center mt-3 p-3">
      <div class="text-center mb-4">
        <p class="fw-bold fs-5 mb-1">
          올려주신 등기부등본에 대해서 분석했어요.
        </p>
        <p class="fw-bold fs-5">
          <span style="color: #fe5252">위험 요소</span>는 없는지,
          <span style="color: #1a80e5">꼼꼼히</span> 살펴봤어요.
        </p>
        <p class="text-muted mb-4">
          모든 등급은
          <span
            class="text-primary text-decoration-underline"
            role="button"
            style="cursor: pointer"
            @click="openInfoModal"
            aria-label="등급 판정 기준 안내 모달 열기"
            aria-haspopup="dialog"
            aria-controls="diagnosis-grade-info-modal"
            >판정기준</span
          >에 의해 설정된 등급입니다.
        </p>
        <DiagnosisGradeInfoModal
          :show="showInfoModal"
          @close="closeInfoModal"
        />
      </div>
      <!-- 등급 표시 원형 -->
      <div
        class="rounded-circle d-flex align-items-center justify-content-center fw-bold mb-5"
        :style="{
          border: `12px solid ${gradeColor[result.rating] || '#6c757d'}`,
          color: gradeColor[result.rating] || '#6c757d',
          // borderWidth: '12px',
          width: '13rem',
          height: '13rem',
          fontSize: '2.2rem',
        }"
      >
        {{ result.rating }}
      </div>
      <BuddyHelper @open-dictionary="openDictionaryModal" />
      <TermViewModal v-if="showDictionaryModal" @close="closeDictionaryModal" />
      <!-- 등급 설명 -->
      <div
        class="text-center px-4 py-3 mb-4"
        style="background-color: #f0f6ff; border-radius: 1rem; max-width: 640px"
      >
        <span v-html="getGradeMessage(result.rating)"></span>
      </div>

      <!-- 좌우분할 → 반응형으로 변경 -->
      <div class="analysis-outer">
        <div class="two-col-card">
          <div class="row w-100 align-items-start analysis-container">
            <!-- PDF 뷰어 섹션 -->
            <div class="col-lg-6 col-12 pdf-section">
              <p class="fw-bold fs-5 mb-2" style="color: #1a80e5">
                어떤 점이 위험한지 하나씩 확인해보세요.
              </p>
              <div v-if="result?.fileUrl" class="pdf-wrapper">
                <PDFView :pdfUrl="result.fileUrl" />
              </div>
            </div>

            <!-- 분석 결과 섹션 -->
            <div class="col-lg-6 col-12 analysis-section">
              <p class="fw-bold fs-5 mb-2" style="color: #1a80e5">기본 정보</p>
              <p class="address-info">주소: {{ result.address }}</p>
              <p class="jeonse-rate-info">
                예상 전세가율:
                <span v-if="result.jeonseRate !== -1"
                  >{{ result.jeonseRate }} %</span
                >
                <span v-else style="color: gray">판단 불가</span>
              </p>
              <p class="prior-info">
                선순위 채권총액: {{ formatCurrency(result.totalPriorAmount) }}원
              </p>
              <hr class="my-3" />
              <p class="fw-bold fs-5 mb-1" style="color: #1a80e5">주의 사항</p>
              <div class="analysis-cards-wrapper">
                <AnalysisCards
                  v-if="result && result.analysis"
                  :analysis="result.analysis"
                  :analysisItems="analysisItems"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="button-area">
      <button @click="showModal = true" class="checklist-button">
        체크리스트로 넘어가기
      </button>
    </div>

    <!-- 모달 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>체크리스트 작성</h3>
        </div>
        <div class="modal-body">
          <p>체크리스트를 작성하러 가시겠습니까?</p>
        </div>
        <div class="modal-footer">
          <button @click="goToChecklist" class="confirm-button">예</button>
          <button @click="goToHome" class="cancel-button">아니오</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api/index.js';
import PDFView from '@/components/PDFView.vue';
import AnalysisCards from '@/components/AnalysisCards.vue';
import BuddyHelper from '@/components/BuddyHelper.vue';
import TermViewModal from '@/components/TermViewModal.vue';
import { useAuthStore } from '@/stores/auth';
import DiagnosisGradeInfoModal from '@/components/final-report/DiagnosisGradeInfoModal.vue';

const route = useRoute();
const router = useRouter();
const result = ref(null);
const auth = useAuthStore();
const user = ref(null);

// 금액 쉼표 표시
const formatCurrency = (value) => {
  if (!value && value !== 0) return '';
  return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

const analysisItems = [
  { label: '경매', key: 'auctionInfos' },
  { label: '가압류', key: 'provisionalSeizureInfos' },
  { label: '압류', key: 'seizureInfos' },
  { label: '가처분', key: 'injunctionInfos' },
  { label: '근저당권', key: 'mortgageInfos' },
  { label: '신탁등기', key: 'trustInfos' },
  { label: '전세권설정', key: 'jeonseRightInfos' },
  { label: '가등기', key: 'provisionalRegistrationInfos' },
];

// 등급별 색상
const gradeColor = {
  안전: '#00AEEF',
  보통: '#39B54A',
  주의: '#F7941D',
  위험: '#ED1C24',
};

// 등급별 메시지 반환 함수
const getGradeMessage = (rating) => {
  const userName = user.value?.name || '사용자';
  const colors = {
    안전: '#31BDF9',
    보통: '#1ABE5F',
    주의: '#FF8400',
    위험: '#FF3838',
    '판단 보류': '#FFCF64',
  };
  const endings = {
    안전: '안전합니다.',
    보통: '보통입니다.',
    주의: '주의가 필요합니다.',
    위험: '위험합니다.',
    '판단 보류': '판단 보류입니다.',
  };
  if (['안전', '보통', '주의', '위험'].includes(rating)) {
    // 등급 키워드만 색상 span으로 감싸기
    const coloredEnding = endings[rating].replace(
      rating,
      `<span style="color:${colors[rating]}; font-weight:800;">${rating}</span>`
    );

    return `<span style="font-size:1.3rem; font-weight:800;">
      ${userName}님이 올려주신 등기부등본은 ${coloredEnding}
    </span>`;
  }
  return '등기부등본을 분석 중입니다...';
};

// 판정기준 모달 표시 상태
const showInfoModal = ref(false);

// 용어 모달 표시 상태
const showDictionaryModal = ref(false);

// 체크리스트 모달 표시 상태
const showModal = ref(false);

// 판정 기준 모달
const openInfoModal = () => {
  showInfoModal.value = true;
};
const closeInfoModal = () => {
  showInfoModal.value = false;
};

// 용어모달 열기/닫기 함수
const openDictionaryModal = () => {
  showDictionaryModal.value = true;
};
const closeDictionaryModal = () => {
  showDictionaryModal.value = false;
};

// 체크리스트 모달 관련 함수
const closeModal = () => {
  showModal.value = false;
};

// 체크리스트 모달창 예 클릭시 이벤트 함수
const goToChecklist = () => {
  showModal.value = false;
  router.push({
    path: '/checklist/checklist',
    query: {
      userId: auth.userId,
      registryId: route.params.registerId,
    },
  });
};

// 체크리스트 모달창 아니오 클릭시 이벤트 함수
const goToHome = () => {
  showModal.value = false;
  router.push('/');
};

onMounted(async () => {
  const registerId = route.params.registerId;
  try {
    // 사용자 정보 가져오기
    if (auth.token) {
      const userRes = await api.get('/api/user/me', {
        headers: {
          Authorization: `Bearer ${auth.token}`,
        },
      });
      user.value = userRes.data;
    }

    // 두 개의 API 요청을 병렬로 처리
    const [safetyRes, jeonseRes] = await Promise.all([
      api.get(`/api/safety-check/${registerId}`),
      api.get(`/api/diagnosis/result?registerId=${registerId}`),
    ]);

    // jeonseRate를 기존 결과에 병합
    result.value = {
      ...safetyRes.data,
      jeonseRate: jeonseRes.data.jeonseRate,
    };
  } catch (e) {
    console.error('데이터 가져오기 실패:', e);
  }
});
</script>

<style scoped>
.donut-thin {
  border-width: 8px;
  border-style: solid;
  transition: all 0.3s ease;
}

/* 기본 정보 스타일 */
.address-info,
.jeonse-rate-info,
.prior-info {
  font-size: 1.25rem;
}

/* 반응형 레이아웃 스타일 */
.analysis-container {
  min-height: 80vh;
}

/* 데스크톱 (lg 이상) - 992px 이상 */
@media (min-width: 992px) {
  .pdf-section {
    padding: 1rem;
    height: 80vh;
  }

  .pdf-wrapper {
    position: sticky;
    top: 1rem;
    max-height: calc(80vh - 2rem);
    overflow-y: auto;
  }

  .analysis-section {
    height: 80vh;
    overflow-y: auto;
    padding: 1rem;
    display: flex;
    flex-direction: column;
  }

  .analysis-cards-wrapper {
    flex: 1;
    overflow-y: auto;
    margin-top: 1rem;
  }
}

/* 태블릿 (md~lg) - 768px ~ 991px */
@media (max-width: 991.98px) and (min-width: 768px) {
  .analysis-container {
    min-height: auto;
  }

  .pdf-section {
    padding: 1rem;
    margin-bottom: 2rem;
  }

  .pdf-wrapper {
    max-height: 60vh;
    overflow-y: auto;
  }

  .analysis-section {
    padding: 1rem;
  }

  .analysis-cards-wrapper {
    margin-top: 1rem;
  }

  .address-info,
  .jeonse-rate-info {
    font-size: 1.1rem !important;
  }
}

/* 모바일 (sm~md) - 576px ~ 767px */
@media (max-width: 767.98px) and (min-width: 576px) {
  .analysis-container {
    min-height: auto;
  }

  .pdf-section {
    padding: 0.75rem;
    margin-bottom: 1.5rem;
  }

  .pdf-wrapper {
    max-height: 55vh;
    overflow-y: auto;
  }

  .analysis-section {
    padding: 0.75rem;
  }

  .analysis-cards-wrapper {
    margin-top: 1rem;
  }

  .address-info,
  .jeonse-rate-info {
    font-size: 1.05rem !important;
  }

  .checklist-button {
    padding: 15px 28px;
    font-size: 17px;
    min-width: 190px;
  }

  .modal-content {
    min-width: 350px;
    margin: 15px;
  }
}

/* 작은 모바일 (xs) - 576px 미만 */
@media (max-width: 575.98px) {
  .analysis-container {
    min-height: auto;
  }

  .pdf-section,
  .analysis-section {
    padding: 0.5rem;
  }

  .pdf-section {
    margin-bottom: 1rem;
  }

  .pdf-wrapper {
    max-height: 50vh;
  }

  .analysis-cards-wrapper {
    margin-top: 0.75rem;
  }

  .fw-bold.fs-5 {
    font-size: 1.1rem !important;
  }

  .address-info,
  .jeonse-rate-info {
    font-size: 1rem !important;
  }

  /* 버튼 및 모달 반응형 */
  .checklist-button {
    padding: 14px 24px;
    font-size: 16px;
    min-width: 180px;
  }

  .modal-content {
    min-width: 320px;
    margin: 20px;
  }

  .modal-footer {
    flex-direction: column;
  }

  .confirm-button,
  .cancel-button {
    width: 100%;
  }
}

/* 버튼 영역 - 중앙 정렬 */
.button-area {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 20px;
  margin-top: 30px;
}

/* 메인 버튼 스타일 */
.checklist-button {
  background-color: #1a80e5;
  color: white;
  border: none;
  padding: 16px 32px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(26, 128, 229, 0.3);
  min-width: 200px;
}

.checklist-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(26, 128, 229, 0.4);
  background-color: #1570cc;
}

.checklist-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 10px rgba(102, 126, 234, 0.3);
}

/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

/* 모달 콘텐츠 */
.modal-content {
  background: white;
  border-radius: 16px;
  padding: 0;
  min-width: 400px;
  max-width: 500px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 모달 헤더 */
.modal-header {
  padding: 24px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
}

/* 모달 바디 */
.modal-body {
  padding: 24px;
  text-align: center;
}

.modal-body p {
  margin: 0;
  font-size: 16px;
  color: #4b5563;
  line-height: 1.6;
}

/* 모달 푸터 */
.modal-footer {
  padding: 16px 24px 24px;
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 예 버튼 */
.confirm-button {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.confirm-button:hover {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.3);
}

/* 아니오 버튼 */
.cancel-button {
  background: linear-gradient(135deg, #9ca3af 0%, #6b7280 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 80px;
}

.cancel-button:hover {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(107, 114, 128, 0.3);
}

/* 버튼 폭 동일 */
.confirm-button,
.cancel-button {
  width: 100%;
}

/* 전체 폭 제한 + 중앙 정렬 */
.analysis-outer {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 0 1rem;
}
.two-col-card {
  width: 100%;
  max-width: 1440px; /* 🔹 데스크톱에서 두 칼럼이 한눈에 */
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 12px 14px; /* 얇은 카드 느낌 */
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.04);
}

/* 상단 원 주변 여백 살짝 축소 */
.mb-5 {
  margin-bottom: 2rem !important;
} /* 원 아래 간격 줄이기 */

/* 데스크톱 레이아웃 높이 통일 + 스크롤 */
@media (min-width: 992px) {
  .analysis-container {
    min-height: auto;
  }

  /* 기존 80vh 높이 무효화 */
  .pdf-section,
  .analysis-section {
    height: auto !important;
    padding: 0.5rem 0.75rem;
  }

  /* 내부만 스크롤(두 칼럼 같은 높이) */
  .pdf-wrapper {
    position: sticky;
    top: 12px;
    max-height: 74vh;
    overflow: auto;
  }
  .analysis-section {
    max-height: 74vh;
    overflow: auto;
    display: flex;
    flex-direction: column;
  }
  .analysis-cards-wrapper {
    flex: 1;
    overflow: auto;
    margin-top: 0.75rem;
  }
}

/* 텍스트 살짝 컴팩트하게 */
.address-info,
.jeonse-rate-info,
.prior-info {
  font-size: 1.2rem;
}
</style>
