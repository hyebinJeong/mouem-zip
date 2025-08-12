<script setup>
defineProps({
  show: Boolean, // 부모 컴포넌트로부터 표시 여부
});

const gradeList = [
  {
    title: '안전 등급',
    colorClass: 'text-primary',
    items: [
      '등기분석: 권리 침해 사항이 하나도 없는 경우',
      '전세가율: -5% 이하',
      '체크리스트: 모두 체크',
    ],
  },
  {
    title: '보통 등급',
    colorClass: 'text-success',
    items: [
      '등기분석: 현재 말소 안 된 권리가 있으며, 선순위 채권총액이 전세 보증금의 0% 초과 ~ 40% 이하인 경우 또는 현재 권리 침해 사항이 없으면서 최근 2년 이내 기재된 내역이 있는 경우',
      '전세가율: +0~4%',
      '체크리스트: 입주 전 수리에 대한 확인, 임차 주택의 사용/관리 수선 확인 미체크 시',
    ],
  },
  {
    title: '주의 등급',
    colorClass: 'text-warning',
    items: [
      '등기분석: 현재 말소 안 된 권리가 있으며, 선순위 채권총액이 임차 보증금의 40% 초과 ~ 100% 미만인 경우 또는 현재 권리 침해 사항이 없으면서 최근 1년 이내 기재된 내역이 있는 경우',
      '전세가율: 해당 지역 평균보다 +5% 이상',
      '체크리스트: 불리한 특약 확인, 계약금·중도금·잔금 지급일/금액 일치 여부 미체크 시',
    ],
  },
  {
    title: '위험 등급',
    colorClass: 'text-danger',
    items: [
      '등기분석: 현재 말소 안 된 권리가 있으며, 선순위 채권총액이 전세 보증금 이상인 경우 또는 경매 사항이 있는 경우',
      '전세가율: 해당지역 평균보다 +10% 이상',
      '체크리스트: 임대인의 미납 세금 확인, 등기부등본상 소유주 일치 여부, 선순위 세입자 항목, 주소일치 여부, 전입신고 가능 여부 중 하나라도 미체크 시',
    ],
  },
  {
    title: '판단 보류 등급',
    colorClass: 'text-muted',
    items: [
      '전세가율: 시세가 제공되지 않는 경우, 전세가율 평균이 제공되지 않는 경우',
    ],
  },
];
</script>

<template>
  <div
    v-if="show"
    class="DiagnosisModalPage d-flex justify-content-center align-items-center bg-dark bg-opacity-50 position-fixed top-0 start-0 w-100"
    id="diagnosis-grade-info-modal"
    role="dialog"
    aria-modal="true"
    aria-labelledby="modal-title"
    aria-describedby="modal-desc"
  >
    <section
      class="diagnosis-box bg-white rounded-4 shadow-sm p-4 d-flex flex-column"
    >
      <h1 class="fs-4 fw-bold text-center mb-3 my-2" id="modal-title">
        진단 등급 판정 기준
      </h1>

      <section
        class="diagnosis-content flex-grow-1 overflow-auto px-1 py-2"
        id="modal-desc"
      >
        <article
          v-for="(grade, index) in gradeList"
          :key="index"
          class="mb-4 text-start"
        >
          <div :class="[grade.colorClass, 'fw-bold', 'mb-2']">
            {{ grade.title }}
          </div>
          <div class="bg-light rounded-3 p-3">
            <div v-for="(item, i) in grade.items" :key="i" class="mb-1">
              <p class="mb-1">{{ item }}</p>
            </div>
          </div>
        </article>
      </section>

      <div class="text-center mt-3">
        <button
          class="btn btn-primary px-4 py-2 rounded-3"
          @click="$emit('close')"
        >
          확인 완료
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.DiagnosisModalPage {
  overflow: hidden;
  height: 100dvh;
  z-index: 1050;
}

.diagnosis-box {
  width: 95%;
  max-width: 720px;
  height: calc(100dvh - 130px);
}

@media (max-width: 576px) {
  .diagnosis-box {
    width: 100%;
    height: 95vh;
    border-radius: 0;
  }
}
</style>
