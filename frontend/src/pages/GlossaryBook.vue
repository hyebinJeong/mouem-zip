<script>
import TermCard from '@/components/TermCard.vue'
import TermModal from '@/components/TermModal.vue'
import TermViewModal from '@/components/TermViewModal.vue'
import { fetchDictionary } from '@/api/termApi.js'

export default {
  name: 'TermView',
  components: { TermCard, TermModal, TermViewModal },
  data() {
    return {
      showTermModal: false, // 모달 제어용
      keyword: '',
      filter: '',
      selectedTerm: null,
      terms: [] // 백엔드 데이터 저장
    }
  },
  computed: {
    filteredTerms() {
      return this.terms.filter(term =>
          (!this.filter || term.categoryName === this.filter) &&
          term.termName.includes(this.keyword)  // 백엔드 필드명 적용
      )
          .slice() // 원본 배열 수정 방지
          .sort((a, b) => a.termName.localeCompare(b.termName, 'ko')) // 가나다순 정렬
    },
    categories() {
      const allCategories = this.terms.map(term => term.categoryName)
      return [...new Set(allCategories)]
    }
  },
  methods: {
    openModal(term) {
      this.selectedTerm = term
    },
    async loadDictionary() {
      try {
        const response = await fetchDictionary()
        this.terms = response.data
      } catch (error) {
        console.error('사전 데이터 불러오기 실패', error)
      }
    }
  },
  mounted() {
    this.loadDictionary()
  }
}
</script>

<template>

  <div class="term-view container mt-5">
    <!-- 검색창 -->
    <div class="row justify-content-center mb-3">
      <div class="col-md-8 d-flex justify-content-center">
        <input
            type="text"
            v-model="keyword"
            class="form-control rounded-pill px-4"
            placeholder="검색할 용어를 입력하세요"
            style="width: 66%;"
        />
      </div>
    </div>

    <!-- 카테고리 필터 -->
    <div class="row mb-4">
      <div class="col text-end">
        <select v-model="filter" class="form-select w-auto d-inline-block" style="margin-right: 11rem;">
          <option value="">전체</option>
          <option v-for="cat in categories" :key="cat" :value="cat">
            {{ cat }}
          </option>
        </select>
      </div>
    </div>

    <!-- 카드 리스트 -->
    <div class="row justify-content-center g-4 row-cols-1 row-cols-sm-2 row-cols-md-3">
      <div class="col" v-for="item in filteredTerms" :key="item.termId">
        <TermCard :term="item" @open="openModal" />
      </div>
    </div>

    <!-- 모달 -->
    <TermModal :term="selectedTerm" @close="selectedTerm = null" />
  </div>
</template>

<style scoped>
.term-view {
  padding-bottom: 40px;
  padding-left: 8rem;
  padding-right: 8rem;
}
@media (min-width: 1400px) {
  .term-view {
    padding-left: 12rem;
    padding-right: 12rem;
  }
}
</style>