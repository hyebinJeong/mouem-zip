<template>
  <div class="p-4" style="background-color: #f7f9fc; min-height: 70vh">
    <div class="d-flex align-items-center mb-3">
      <i
        class="fas fa-arrow-left me-2"
        style="cursor: pointer"
        @click="goBack"
      ></i>
      <h5 class="fw-bold mb-0">용어 추가</h5>
    </div>
    <p class="text-muted">새로운 단어를 추가할 수 있는 페이지입니다.</p>

    <form @submit.prevent="submitForm" class="mt-4">
      <div class="mb-3">
        <label class="form-label fw-semibold">단어</label>
        <input
          v-model="termName"
          type="text"
          class="form-control"
          placeholder="단어를 입력해주세요."
          required
        />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">정의</label>
        <input
          v-model="termDefine"
          type="text"
          class="form-control"
          placeholder="정의를 입력해주세요."
          required
        />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">예시</label>
        <input
          v-model="termExample"
          type="text"
          class="form-control"
          placeholder="예시를 입력해주세요."
          required
        />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">주의</label>
        <input
          v-model="termCaution"
          type="text"
          class="form-control"
          placeholder="주의사항을 입력해주세요."
          required
        />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">카테고리</label>
        <select v-model="categoryId" class="form-select" required>
          <option disabled value="">카테고리를 선택하세요.</option>
          <option
            v-for="category in categories"
            :key="category.categoryId"
            :value="category.categoryId"
          >
            {{ category.categoryName }}
          </option>
        </select>
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
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useTermStore } from '@/stores/termStore';
import api from '@/api/index.js';
const router = useRouter();
const termStore = useTermStore();

const termName = ref('');
const termDefine = ref('');
const termExample = ref('');
const termCaution = ref('');
const categoryId = ref('');
const categories = ref([]);
const goBack = () => {
  router.back();
};

const submitForm = async () => {
  if (
    !termName.value ||
    !termDefine.value ||
    !termExample.value ||
    !termCaution.value ||
    !categoryId.value
  ) {
    alert('모든 필드를 입력해주세요.');
    return;
  }

  try {
    await termStore.addTerm({
      termName: termName.value,
      termDefine: termDefine.value,
      termExample: termExample.value,
      termCaution: termCaution.value,
      categoryId: categoryId.value,
    });
    goBack();
  } catch (error) {
    alert('용어 추가 중 오류가 발생했습니다.');
    console.error(error);
  }
};

onMounted(async () => {
  try {
    const res = await api.get('/api/category-manager'); // 엔드포인트 확인
    categories.value = res.data;
  } catch (error) {
    console.error('카테고리 로딩 실패:', error);
  }
});
</script>
