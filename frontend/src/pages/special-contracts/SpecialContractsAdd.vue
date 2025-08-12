<template>
  <div class="p-4" style="background-color: #f7f9fc; min-height: 70vh">
    <div class="d-flex align-items-center mb-3">
      <i
        class="fas fa-arrow-left me-2"
        style="cursor: pointer"
        @click="goBack"
      ></i>
      <h5 class="fw-bold mb-0">특약사항 추가</h5>
    </div>
    <p class="text-muted">
      선택한 특약사항에 대해 확인하고 추가할 수 있는 페이지입니다.
    </p>

    <form @submit.prevent="submitForm" class="mt-4">
      <div class="mb-3">
        <label class="form-label fw-semibold">특약분류</label>
        <input
          v-model="category"
          type="text"
          class="form-control"
          placeholder="예: 안전 장치 조항"
        />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">중요도</label>
        <select v-model="importance" class="form-select">
          <option disabled value="">중요도 선택</option>
          <option value="높음">높음</option>
          <option value="중간">중간</option>
          <option value="낮음">낮음</option>
        </select>
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">설명</label>
        <textarea
          v-model="description"
          rows="4"
          class="form-control"
          placeholder="설명을 입력해주세요."
        ></textarea>
      </div>

      <div class="text-end mt-4">
        <button type="button" class="btn btn-secondary me-2" @click="goBack">
          취소
        </button>
        <button type="submit" class="btn btn-primary">추가</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useSpecialContractsStore } from '@/stores/specialContractsStore';

const router = useRouter();
const store = useSpecialContractsStore();

const category = ref('');
const importance = ref('');
const description = ref('');

// 중요도에 따른 색상 코드 매핑
const importanceColorMap = {
  높음: '#FF0000',
  중간: '#FFA500',
  낮음: '#00FF00',
};

// computed로 색상 코드 계산
const importanceColor = computed(
  () => importanceColorMap[importance.value] || ''
);

const goBack = () => {
  router.back();
};

const submitForm = async () => {
  if (!category.value || !importance.value || !description.value) {
    alert('모든 항목을 입력해주세요.');
    return;
  }

  const newContract = {
    category: category.value,
    importance: importance.value,
    importanceColor: importanceColor.value, // 카멜케이스 적용
    description: description.value,
  };

  try {
    console.log('제출할 데이터:', newContract);
    await store.addContract(newContract);
    router.push('/category/special');
  } catch (error) {
    console.error('특약사항 추가 실패:', error);
    alert('특약사항 추가 중 오류가 발생했습니다.');
  }
};
</script>
