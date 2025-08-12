<template>
  <div class="p-4" style="background-color: #f7f9fc; min-height: 70vh">
    <div class="d-flex align-items-center mb-3">
      <button
        class="btn btn-link me-2 p-0 arrow-button"
        @click="$router.back()"
      >
        <i class="fas fa-arrow-left" style="color: #6c757d"></i>
      </button>
      <h5 class="fw-bold mb-0">카테고리 관리</h5>
    </div>
    <p class="text-muted">
      전체 카테고리 목록을 확인하고 관리하는 페이지입니다.
    </p>

    <!-- 검색창 -->
    <div class="mb-3 d-flex justify-content-end">
      <div class="input-group" style="width: 35%">
        <input
          type="text"
          class="form-control rounded-pill"
          placeholder="검색할 카테고리를 입력하세요"
          v-model="search"
        />
      </div>
    </div>

    <!-- 테이블 -->
    <table class="table align-middle">
      <thead>
        <tr>
          <th style="width: 30%">카테고리</th>
          <th style="width: 50%">카테고리 설명</th>
          <th style="width: 20%">카테고리 색상</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="item in paginatedCategories"
          :key="item.categoryId"
          @click="goToEdit(item.categoryId)"
          style="cursor: pointer"
        >
          <td class="text-body">{{ item.categoryName }}</td>
          <td>{{ truncateText(item.description, 30) }}</td>
          <td>
            <span
              class="d-inline-block rounded-circle"
              :style="{
                backgroundColor: item.categoryColor,
                width: '20px',
                height: '20px',
              }"
            ></span>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 페이지네이션 -->
    <div
      class="d-flex justify-content-center mt-3"
      v-if="filteredCategories.length > itemsPerPage"
    >
      <nav>
        <ul class="pagination mb-0">
          <!-- 맨 앞으로 -->
          <li class="page-item" :class="{ disabled: currentPage === 1 }">
            <button class="page-link" @click="goToPage(1)">«</button>
          </li>

          <!-- 페이지 번호 -->
          <li
            class="page-item"
            v-for="page in visiblePages"
            :key="page"
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
      <router-link to="/category/add" class="btn btn-primary">추가</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useCategoryStore } from '@/stores/categoryStore';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const search = ref('');
const currentPage = ref(1);
const itemsPerPage = 15;

const categoryStore = useCategoryStore();

onMounted(() => {
  categoryStore.fetchCategories();
});

const filteredCategories = computed(() =>
  categoryStore.categories.filter(
    (c) =>
      c.categoryName.toLowerCase().includes(search.value.toLowerCase()) ||
      c.description.toLowerCase().includes(search.value.toLowerCase())
  )
);

const totalPages = computed(() =>
  Math.ceil(filteredCategories.value.length / itemsPerPage)
);

const paginatedCategories = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredCategories.value.slice(start, start + itemsPerPage);
});

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
  router.push(`/category/edit/${id}`);
};

const truncateText = (text, maxLength) => {
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text;
};

watch(search, () => {
  currentPage.value = 1;
});
</script>

<style scoped>
.pagination .page-link {
  cursor: pointer;
}
</style>
