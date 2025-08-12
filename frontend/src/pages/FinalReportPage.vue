<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import DiagnosisGradeInfoModal from '@/components/final-report/DiagnosisGradeInfoModal.vue';
import FinalGrade from '@/components/final-report/FinalGrade.vue';
import FinalJeonse from '@/components/final-report/FinalJeonse.vue';
import FinalChecklist from '@/components/final-report/FinalChecklist.vue';
import { useNavigation } from '@/composables/final-report/useNavigation';
import {
  getFinalReport,
  getFinalReportByUserAndRegistry,
} from '@/api/finalReport';
import FinalJeonseCard from '@/components/final-report/FinalJeonseCard.vue';
import checklistStore from '@/stores/checklistStore';
import AnalysisCards from '@/components/AnalysisCards.vue';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const route = useRoute();
const showModal = ref(false);
const reportData = ref(null);
const isLoadingPDF = ref(false); // 로딩 상태 추가

const openModal = () => (showModal.value = true);
const closeModal = () => (showModal.value = false);
const { goToHome, goToMyPage } = useNavigation();

// 체크리스트 미체크 항목 계산
const uncheckedItems = computed(() => {
  return checklistStore.filter(
    (_, index) => !reportData.value?.checked?.[index]
  );
});

// 등기부 분석 항목
const registryKeys = [
  { key: 'mortgageInfos', label: '근저당권' },
  { key: 'seizureInfos', label: '압류' },
  { key: 'provisionalSeizureInfos', label: '가압류' },
  { key: 'auctionInfos', label: '경매' },
  { key: 'provisionalRegistrationInfos', label: '가등기' },
  { key: 'injunctionInfos', label: '가처분' },
  { key: 'jeonseRightInfos', label: '전세권설정' },
  { key: 'trustInfos', label: '신탁등기' },
];

// PDF 다운로드 (로딩 처리 추가)
async function downloadPDF() {
  const pdfArea = document.getElementById('pdf-area');
  if (!pdfArea) return;

  isLoadingPDF.value = true; // 로딩 시작

  try {
    // 파일명용 날짜 문자열
    const dateStr = new Date().toISOString().split('T')[0]; // yyyy-mm-dd

    const canvas = await html2canvas(pdfArea, { scale: 2 });
    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF('p', 'mm', 'a4');

    const pageWidth = pdf.internal.pageSize.getWidth();
    const pageHeight = pdf.internal.pageSize.getHeight();
    const imgWidth = pageWidth;
    const imgHeight = (canvas.height * imgWidth) / canvas.width;

    let heightLeft = imgHeight;
    let position = 0;

    // 첫 페이지
    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    // 남은 부분 페이지 추가
    while (heightLeft > 0) {
      position -= pageHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    pdf.save(
      `${reportData.value.username || '사용자'}_최종리포트_${dateStr}.pdf`
    );
  } catch (e) {
    console.error('PDF 생성 오류:', e);
  } finally {
    isLoadingPDF.value = false; // 로딩 종료
  }
}

// 데이터 로드
onMounted(async () => {
  const userId = Number(route.query.userId);
  const registryId = Number(route.query.registryId);
  const reportId = Number(route.query.reportId);

  try {
    let res;

    if (userId && registryId) {
      // final_report에 저장 (없으면 생성)
      await fetch(`/api/reports?userId=${userId}&registryId=${registryId}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${auth.token}`,
        },
      });

      // 저장 후 조회
      res = await getFinalReportByUserAndRegistry(userId, registryId);
    } else if (reportId) {
      // 마이페이지에서 reportId만 넘어온 경우
      res = await getFinalReport(reportId);
    } else {
      console.warn('잘못된 쿼리 파라미터');
      return;
    }

    // 데이터 바인딩
    reportData.value = {
      registryRating: res?.registryRating ?? '',
      registryAnalysis: res?.registryAnalysis ?? null,
      jeonseRatioRating: res?.jeonseRatioRating ?? '',
      checklistRating: res?.checklistRating ?? '',
      jeonseRatio: res?.jeonseRatio ?? 0,
      regionAvgJeonseRatio: res?.regionAvgJeonseRatio ?? 0,
      expectedSellingPrice: res?.expectedSellingPrice ?? 0,
      deposit: res?.deposit ?? 0,
      username: res?.username ?? '사용자',
      checked: res?.checked ?? [],
    };
  } catch (error) {
    console.error('최종 리포트 생성 또는 조회 실패:', error);
  }
});
</script>

<template>
  <div
    class="FinalReportPage container py-5 mb-4 text-center"
    v-if="reportData"
  >
    <section class="position-relative mb-4" aria-labelledby="page-title">
      <h1 id="page-title" class="text-center mb-0">
        <span class="main-color fw-semibold">최종 분석</span>이 완료되었어요.
      </h1>

      <!-- 마이페이지에서 들어왔을 때만 버튼 보이게 -->
      <button
        v-if="route.query.from === 'myPage'"
        class="btn btn-primary position-absolute top-50 translate-middle-y end-0"
        @click="downloadPDF"
        :disabled="isLoadingPDF"
      >
        {{ isLoadingPDF ? 'PDF 생성 중...' : '다운로드' }}
      </button>
    </section>

    <!-- PDF 생성 중일 때 로딩 오버레이 -->
    <div
      v-if="isLoadingPDF"
      class="loading-overlay d-flex justify-content-center align-items-center"
    >
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <!-- PDF에 담길 영역 -->
    <div id="pdf-area">
      <p class="text-muted mb-4">
        모든 등급은
        <span
          class="text-primary text-decoration-underline"
          type="button"
          style="cursor: pointer"
          @click="openModal"
          aria-label="등급 판정 기준 안내 모달 열기"
          aria-haspopup="dialog"
          aria-controls="diagnosis-grade-info-modal"
          >판정기준</span
        >에 의해 설정된 등급입니다.
      </p>
      <DiagnosisGradeInfoModal :show="showModal" @close="closeModal" />

      <!-- 등급 -->
      <section class="final-grade-wrap">
        <FinalGrade
          :registry="reportData.registryRating"
          :jeonse="reportData.jeonseRatioRating"
          :checklist="reportData.checklistRating"
        />
      </section>

      <hr class="my-4 border-top border-secondary-subtle w-75 mx-auto" />

      <!-- 전세가율 -->
      <section
        class="final-jeonse-wrap mt-5 mb-4"
        role="region"
        aria-labelledby="jeonse-title"
      >
        <div v-if="reportData.jeonseRatioRating !== '판단보류'">
          <h2 style="margin-bottom: 2rem" id="jeonse-title">
            {{ reportData.username }}님의
            <span class="main-color fw-semibold">전세가율</span>을 분석했어요.
          </h2>
          <div
            class="d-flex flex-column flex-md-row justify-content-center align-items-end"
            style="max-width: 960px; margin: 0 auto; height: auto"
          >
            <div class="final-jeonse-col w-100" style="max-width: 480px">
              <div class="h-100 d-flex align-items-end">
                <FinalJeonse
                  :jeonseRatio="reportData.jeonseRatio"
                  :regionAvgJeonseRatio="reportData.regionAvgJeonseRatio"
                />
              </div>
            </div>
            <div class="final-jeonse-col w-100" style="max-width: 480px">
              <div class="d-flex h-100">
                <FinalJeonseCard
                  :salePrice="reportData.expectedSellingPrice"
                  :jeonseDeposit="reportData.deposit"
                  :jeonseRatio="reportData.jeonseRatio"
                  :jeonseRatioRating="reportData.jeonseRatioRating"
                  :username="reportData.username"
                />
              </div>
            </div>
          </div>
        </div>
        <div v-else>
          <h2>
            {{ reportData.username }}님의 전세가율은
            <br class="mobile-only-break" />
            <span class="text-secondary no-break fw-semibold"
              >판단보류 등급</span
            >으로, 분석이 어려워요.
          </h2>
        </div>
      </section>

      <hr class="my-4 border-top border-secondary-subtle w-75 mt-5" />

      <!-- 등기부등본 -->
      <section
        class="final-registry-wrap mt-5"
        role="region"
        aria-labelledby="registry-title"
      >
        <h2 class="mb-3" id="registry-title">
          {{ reportData.username }}님의
          <span class="main-color fw-semibold">등기부등본</span>을 분석했어요.
        </h2>
        <div class="final-analysis-card-wrap">
          <AnalysisCards
            v-if="reportData.registryAnalysis"
            :analysis="reportData.registryAnalysis"
            :analysisItems="registryKeys"
          />
        </div>
      </section>

      <hr class="my-4 border-top border-secondary-subtle w-75 mx-auto" />

      <!-- 체크리스트 -->
      <section
        class="final-checklist-wrap mt-5 mb-3"
        style="margin: 6rem 0"
        role="region"
        aria-labelledby="checklist-title"
      >
        <div class="final-checklist-inner">
          <div v-if="uncheckedItems.length > 0">
            <h2 class="mb-3" id="checklist-title">
              {{ reportData.username }}님이
              <br class="mobile-only-break" />
              <span class="main-color fw-semibold">체크하지 않은 항목</span
              >이에요.
            </h2>
            <p class="mb-5">
              향후 불이익을 방지하려면 지금 확인하는 것이 좋아요.
            </p>
            <FinalChecklist
              v-if="uncheckedItems.length > 0"
              :checked="reportData.checked"
            />
          </div>
          <div v-else>
            <h2>
              {{ reportData.username }}님은 모든 체크리스트 항목을 확인했어요.
            </h2>
          </div>
        </div>
      </section>
    </div>
    <!-- PDF 영역 끝 -->

    <nav class="final-btn-wrap d-flex justify-content-center">
      <button
        class="btn btn-primary px-4 py-2 rounded-3 background-main"
        @click="goToHome"
      >
        홈으로 이동
      </button>
      <button
        class="btn btn-primary px-4 py-2 rounded-3 background-main"
        @click="goToMyPage"
      >
        마이페이지로 이동
      </button>
    </nav>
  </div>
</template>

<style scoped>
/* 로딩 오버레이 스타일 */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.7);
  z-index: 9999;
}

.final-jeonse-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.final-jeonse-col {
  width: 100%;
  max-width: 480px;
}
.background-main {
  background: #1a80e5;
  color: #fff;
}

.final-btn-wrap {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rem;
  margin-top: 3rem;
  margin-bottom: 3rem;
  flex-wrap: wrap;
  flex-direction: row;
}

.final-btn-wrap button {
  height: 48px;
  font-weight: 600;
  font-size: 1rem;
  border-radius: 0.5rem;
  transition: background-color 0.2s ease-in-out;
  flex: 1 1 45%;
  min-width: 130px;
  max-width: 340px;
  white-space: nowrap;
}

.final-btn-wrap button:hover {
  background-color: #005ac1;
}

.final-analysis-card-wrap {
  max-width: 880px;
  margin: 0 auto;
  padding: 0 1rem;
}

.final-checklist-inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 1rem;
}

.main-color {
  color: #1a80e5;
}

.mobile-only-break {
  display: none;
}

.no-break {
  white-space: nowrap;
}

@media (max-width: 768px) {
  .final-btn-wrap {
    gap: 1rem;
    flex-wrap: nowrap;
  }

  .final-btn-wrap button {
    min-width: 120px;
    flex: 1 1 auto;
  }

  .mobile-only-break {
    display: block;
  }
}

@media (max-width: 576px) {
  .jeonse-card-wrap p {
    white-space: normal;
  }
}
</style>
