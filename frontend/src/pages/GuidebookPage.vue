<script setup>
import GuidebookSection from '@/components/GuidebookSection.vue';
import { useNavigation } from '@/composables/final-report/useNavigation';
import Buddy from '@/components/BuddyHelper.vue';
import TermViewModal from '@/components/TermViewModal.vue';

import { ref } from 'vue';

const showDictionaryModal = ref(false);
const openDictionaryModal = () => {
  showDictionaryModal.value = true;
};
const closeDictionaryModal = () => {
  showDictionaryModal.value = false;
};

const { goToHome } = useNavigation();

// 계약 전 항목
const beforeContractItems = [
  {
    main: '계약 직전, 최신 등기부등본을 다시 확인하세요.',
    warning:
      '피해 사례: 계약 중간에 임대인이 몰래 근저당(담보대출)을 설정한 사례가 있었어요.',
  },
  {
    main: '무허가 또는 불법 건축물인지 꼭 확인해보세요.',
    note: '<a href="https://www.eais.go.kr" target="_blank" rel="noopener noreferrer" style="color: #007c06;">세움터</a>를 통해 확인이 가능해요.',
  },
  {
    main: '임대인의 사기 피해 이력이 있는지 확인해보세요.',
    note: '<a href="https://www.thecheat.co.kr" target="_blank" rel="noopener noreferrer" style="color: #007c06;">더치트</a> 사기 피해 공유 사이트에 임대인 이름, 전화번호, 계좌번호로 검색해보면 사기 피해 이력 확인이 가능해요.',
  },
  {
    main: '보증보험 가입이 가능한 매물인지 미리 확인해보세요.',
    note: '보증보험에 가입하면 보증금 미반환 위험을 줄일 수 있어요.',
  },
  {
    main: '관리비 체납 여부를 확인해보세요.',
    note: '계약 전, 관리사무소에서 최근 3개월 납부 내역을 확인하는 게 좋아요.',
  },
  {
    main: '다가구주택일 경우, 확정일자 순위를 꼭 확인하세요.',
    warning:
      '피해 사례: 먼저 확정일자를 받은 다른 세입자 때문에 보증금을 돌려받지 못하고 경매에 휘말린 사례가 있었어요.',
    note: '동 주민센터에 방문해서 전입세대 열람과 확정일자 등록 내역을 확인하세요. 나보다 먼저 입주하거나 확정일자를 받은 사람이 있는지 꼭 확인해야 해요.',
  },
];

// 계약 당일 항목
const onContractDayItems = [
  {
    main: '임대인 본인 신분증을 직접 확인하세요.',
    note: '만약 대리인과 계약할 경우에는 위임장, 인감증명서, 인감도장, 그리고 임대인과의 전화 인증이 모두 필요해요.',
  },
  {
    main: '계좌 송금 시 꼭 소유자 이름을 확인하세요.',
    note: '등기부등본에 기재된 임대인 명의 계좌로만 송금하고, 영수증, 이체 내역 등 증거를 꼭 보관하세요.',
  },
  {
    main: '계약서 작성 시 날짜와 잔금 지급 순서를 정확하게 기재하세요.',
    warning: '피해 사례: 계약금을 냈지만 입주하지 못했던 사례가 있었어요.',
  },
  {
    main: '이사 당일, 전입신고와 확정일자 신고를 바로 하세요.',
    note: '전입신고를 해야 대항력이 생겨 보증금을 지킬 수 있어요. 이사 후 14일 이내에 신고하지 않으면 5만 원 이하의 과태료가 부과될 수 있어요. 확정일자를 받지 않으면 보증금을 돌려받지 못할 수도 있으니 꼭 챙기세요.',
    warning:
      '피해 사례: 전입신고를 하지 않은 사이 집주인이 바뀌면서, 새 집주인이 퇴거를 요구해 어쩔 수 없이 나가야 했던 사례도 있었어요.',
  },
];
</script>

<template>
  <section
    class="GuidebookPage d-flex justify-content-center align-items-center vh-100 bg-light"
    aria-labelledby="guidebook-title"
  >
    <article
      class="guidebook-box bg-white rounded-4 shadow-sm p-4 d-flex flex-column"
    >
      <h1 class="fs-4 fw-bold text-center mb-3 my-2" id="guidebook-title">
        계약서에 안 나오는 부분까지,
        <!-- 모바일에서만 줄 바꿈 -->
        <br class="d-block d-md-none" />
        안전하게 확인해보세요.
      </h1>
      <section class="guidebook-content flex-grow-1 overflow-auto pe-2 py-4">
        <GuidebookSection title="계약 전" :items="beforeContractItems" />
        <GuidebookSection title="계약 당일" :items="onContractDayItems" />
      </section>
      <nav class="guidebook-btn text-center mt-3">
        <button
          class="btn btn-primary px-4 py-2 rounded-3 background-main"
          @click="goToHome"
        >
          확인완료
        </button>
      </nav>
    </article>
    <!-- 버디 캐릭터 -->
    <Buddy @open-dictionary="openDictionaryModal" />

    <!-- 용어 모달 -->
    <TermViewModal v-if="showDictionaryModal" @close="closeDictionaryModal" />
  </section>
</template>

<style scoped>
.GuidebookPage {
  overflow: hidden;
  height: calc(100dvh - 130px);
}

.guidebook-box {
  width: 95%;
  max-width: 900px;
  height: calc(100dvh - 130px);
}

.background-main {
  background: #1a80e5;
  color: #fff;
}

.text-green {
  color: #007c06;
}

@media (max-width: 576px) {
  .guidebook-box {
    width: 100%;
    height: 95vh;
    border-radius: 0;
  }
}
</style>
