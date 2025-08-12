<template>
  <div class="p-4" style="background-color: #f7f9fc; min-height: 70vh">
    <div class="d-flex align-items-center mb-3">
      <i
        class="fas fa-arrow-left me-2"
        style="cursor: pointer"
        @click="goBack"
      ></i>
      <h5 class="fw-bold mb-0">용어 수정</h5>
    </div>
    <p class="text-muted">
      선택한 단어에 대해 확인하고 수정 및 관리할 수 있는 페이지입니다.
    </p>

    <form v-if="loaded" @submit.prevent="submitForm" class="mt-4">
      <div class="mb-3">
        <label class="form-label fw-semibold">단어</label>
        <input v-model="termName" type="text" class="form-control" required />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">정의</label>
        <input v-model="termDefine" type="text" class="form-control" required />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">예시</label>
        <input
          v-model="termExample"
          type="text"
          class="form-control"
          required
        />
      </div>

      <div class="mb-3">
        <label class="form-label fw-semibold">주의</label>
        <input
          v-model="termCaution"
          type="text"
          class="form-control"
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
        <button type="submit" class="btn btn-primary">수정</button>
        <button type="button" class="btn btn-danger ms-2" @click="deleteTerm">
          삭제
        </button>
      </div>
    </form>

    <div v-else>로딩 중...</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useTermStore } from '@/stores/termStore';
import api from '@/api/index.js';
const router = useRouter();
const route = useRoute();
const termStore = useTermStore();

const id = route.params.id;

const termName = ref('');
const termDefine = ref('');
const termExample = ref('');
const termCaution = ref('');
const categoryId = ref('');
const categories = ref([]);
const loaded = ref(false);

const goBack = () => {
  router.back();
};

const loadCategories = async () => {
  try {
    const res = await api.get('/api/category-manager');
    categories.value = res.data;
  } catch (error) {
    console.error('카테고리 로딩 실패:', error);
  }
};

const loadTerm = async () => {
  try {
    const t = await termStore.fetchTermById(id); // 여기 수정
    if (t) {
      termName.value = t.termName ?? '';
      termDefine.value = t.termDefine ?? '';
      termExample.value = t.termExample ?? '';
      termCaution.value = t.termCaution ?? '';
      categoryId.value = t.categoryId ?? '';
      console.log('Loaded term ID:', id);
    }
    loaded.value = true;
  } catch (error) {
    console.error('용어 로딩 실패:', error);
    loaded.value = true;
    console.log('Failed loading term ID:', id);
  }
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
    await termStore.updateTerm(id, {
      termName: termName.value,
      termDefine: termDefine.value,
      termExample: termExample.value,
      termCaution: termCaution.value,
      categoryId: categoryId.value,
    });
    goBack();
  } catch (error) {
    alert('용어 수정 중 오류가 발생했습니다.');
  }
};

const deleteTerm = async () => {
  if (confirm('정말 이 용어를 삭제하시겠습니까?')) {
    try {
      await termStore.deleteTerm(id);
      goBack();
    } catch (error) {
      alert('삭제 중 오류가 발생했습니다.');
    }
  }
};

onMounted(async () => {
  await loadCategories();
  await loadTerm();
});
</script>
