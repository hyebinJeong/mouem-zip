<template>
  <div class="p-4" style="background-color: #f7f9fc; min-height: 70vh">
    <!-- 헤더 -->
    <div class="d-flex align-items-center mb-3">
      <button class="btn btn-link me-2 p-0" @click="goBack" title="뒤로가기">
        <i class="fas fa-arrow-left text-secondary"></i>
      </button>
      <h5 class="fw-bold mb-0">용어 관리</h5>
    </div>

    <p class="text-muted">전체 용어 목록을 확인하고 관리하는 페이지입니다.</p>

    <!-- 검색창 -->
    <div class="mb-3 d-flex justify-content-end">
      <div class="input-group" style="width: 35%">
        <input
          type="text"
          class="form-control rounded-pill"
          placeholder="검색할 용어를 입력하세요"
          v-model="search"
        />
      </div>
    </div>

    <!-- 테이블 -->
    <table class="table align-middle">
      <thead>
        <tr>
          <th style="width: 30%">단어</th>
          <th style="width: 50%">단어 설명</th>
          <th style="width: 20%">카테고리</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="term in paginatedTerms"
          :key="term.termId"
          @click="goToEdit(term.termId)"
          style="cursor: pointer"
        >
          <td>{{ term.termName }}</td>
          <td>{{ truncateText(term.termDefine, 30) }}</td>
          <td>
            <span
              class="badge"
              :style="{ backgroundColor: term.categoryColor, color: '#000' }"
            >
              {{ term.categoryName }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 페이지네이션 (15개 초과일 때만 표시) -->
    <div
      class="d-flex justify-content-center mt-3"
      v-if="filteredTerms.length > itemsPerPage"
    >
      <nav>
        <ul class="pagination mb-0">
          <!-- 맨 앞으로 -->
          <li class="page-item" :class="{ disabled: currentPage === 1 }">
            <button class="page-link" @click="goToPage(1)">«</button>
          </li>

          <!-- 현재 페이지 기준 ±2 -->
          <li
            v-for="page in visiblePages"
            :key="page"
            class="page-item"
            :class="{ active: currentPage === page }"
          >
            <button class="page-link" @click="goToPage(page)">
              {{ page }}
            </button>
          </li>

          <!-- 맨 뒤로 -->
          <li
            class="page-item"
            :class="{ disabled: currentPage === totalPages }"
          >
            <button class="page-link" @click="goToPage(totalPages)">»</button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- 추가 버튼 -->
    <div class="text-end mt-4">
      <router-link to="/category/term/add" class="btn btn-primary"
        >추가</router-link
      >
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useTermStore } from '@/stores/termStore';

const router = useRouter();
const search = ref('');
const currentPage = ref(1);
const itemsPerPage = 15;

const termStore = useTermStore();

onMounted(() => {
  termStore.fetchTerms();
});

const filteredTerms = computed(() => {
  const terms = Array.isArray(termStore.terms) ? termStore.terms : [];
  return terms.filter(
    (term) =>
      term.termName?.includes(search.value) ||
      term.termDefine?.includes(search.value)
  );
});

const totalPages = computed(() =>
  Math.ceil(filteredTerms.value.length / itemsPerPage)
);

const paginatedTerms = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredTerms.value.slice(start, start + itemsPerPage);
});

const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text;
};

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const visiblePages = computed(() => {
  const pages = [];
  const start = Math.max(1, currentPage.value - 2);
  const end = Math.min(totalPages.value, currentPage.value + 2);
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  return pages;
});

const goToEdit = (id) => {
  router.push(`/category/term/edit/${id}`);
};

const goBack = () => {
  router.back();
};

watch(search, () => {
  currentPage.value = 1;
});
</script>

<style scoped>
.badge {
  font-size: 0.9rem;
  padding: 0.4em 0.6em;
  border-radius: 0.4rem;
}
button.btn-link {
  text-decoration: none;
}
.pagination .page-link {
  cursor: pointer;
}
</style>
