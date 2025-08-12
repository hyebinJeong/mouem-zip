<template>
  <div class="p-4" style="background-color: #f7f9fc; min-height: 70vh">
    <!-- 뒤로가기 -->
    <div class="d-flex align-items-center mb-3">
      <button class="btn btn-link me-2 p-0" @click="goBack" title="뒤로가기">
        <i class="fas fa-arrow-left text-secondary"></i>
      </button>
      <h5 class="fw-bold mb-0">특약사항 관리</h5>
    </div>

    <p class="text-muted">
      전체 특약사항 목록을 확인하고 관리하는 페이지입니다.
    </p>

    <!-- 검색창 -->
    <div class="mb-3 d-flex justify-content-end">
      <div class="input-group" style="width: 35%">
        <input
          type="text"
          class="form-control rounded-pill"
          placeholder="검색할 특약사항을 입력하세요"
          v-model="search"
        />
      </div>
    </div>

    <!-- 테이블 -->
    <table class="table align-middle">
      <thead>
        <tr>
          <th style="width: 30%">특약사항 구분</th>
          <th style="width: 50%">특약사항 세부내용</th>
          <th style="width: 20%">중요도</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="contract in paginatedContracts"
          :key="contract.special_clause_id"
          @click="goToEdit(contract.specialClauseId)"
          style="cursor: pointer"
        >
          <td class="text-body">{{ contract.category }}</td>
          <td>{{ truncateText(contract.description, 30) }}</td>
          <td>
            <div class="d-flex align-items-center gap-2">
              <span :class="importanceDot(contract.importance)"></span>
              <span>{{ importanceLabel(contract.importance) }}</span>
            </div>
          </td>
        </tr>
      </tbody>
    </table>

    <!-- 페이지네이션 -->
    <div class="d-flex justify-content-center mt-4" v-if="totalPages > 1">
      <nav>
        <ul class="pagination">
          <!-- 맨 앞으로 -->
          <li class="page-item" :class="{ disabled: currentPage === 1 }">
            <button
              class="page-link"
              @click="changePage(1)"
              :disabled="currentPage === 1"
            >
              «
            </button>
          </li>

          <!-- 페이지 번호 -->
          <li
            v-for="page in totalPages"
            :key="page"
            class="page-item"
            :class="{ active: currentPage === page }"
          >
            <button class="page-link" @click="changePage(page)">
              {{ page }}
            </button>
          </li>

          <!-- 맨 뒤로 -->
          <li
            class="page-item"
            :class="{ disabled: currentPage === totalPages }"
          >
            <button
              class="page-link"
              @click="changePage(totalPages)"
              :disabled="currentPage === totalPages"
            >
              »
            </button>
          </li>
        </ul>
      </nav>
    </div>

    <!-- 추가 버튼 -->
    <div class="text-end mt-4">
      <router-link to="/category/special/add" class="btn btn-primary">
        추가
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useSpecialContractsStore } from '@/stores/specialcontractsStore';

const router = useRouter();
const search = ref('');
const currentPage = ref(1);
const itemsPerPage = 15;

const store = useSpecialContractsStore();
const contracts = computed(() => store.contracts);

onMounted(() => {
  store.fetchContracts();
});

const filteredContracts = computed(() =>
  contracts.value.filter(
    (c) =>
      c.category.toLowerCase().includes(search.value.toLowerCase()) ||
      c.description.toLowerCase().includes(search.value.toLowerCase())
  )
);

const totalPages = computed(() =>
  Math.ceil(filteredContracts.value.length / itemsPerPage)
);

const paginatedContracts = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  return filteredContracts.value.slice(start, start + itemsPerPage);
});

const changePage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const goToEdit = (id) => {
  router.push({ name: 'SpecialContractsEdit', params: { id } });
};

const goBack = () => {
  router.back();
};

const importanceDot = (level) => {
  console.log('importance level:', level);
  if (!level) return '';
  const val = level.trim().toLowerCase();
  if (val === '높음') return 'dot red';
  if (val === '중간') return 'dot yellow';
  if (val === '낮음') return 'dot green';
  return '';
};

const importanceLabel = (level) => {
  if (!level) return '';
  const val = level.trim().toLowerCase();
  if (val === '높음') return '높음';
  if (val === '중간') return '중간';
  if (val === '낮음') return '낮음';
  return '';
};

const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.slice(0, maxLength) + '...' : text;
};
</script>

<style scoped>
button.btn-link {
  text-decoration: none;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.red {
  background-color: #ff0000;
}
.yellow {
  background-color: #ffa500;
}
.green {
  background-color: #00ff00;
}

.pagination .page-item.active .page-link {
  background-color: #0d6efd;
  border-color: #0d6efd;
  color: white;
}
</style>
