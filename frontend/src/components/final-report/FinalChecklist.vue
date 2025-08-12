<script setup>
import checklistStore from '@/stores/checklistStore';
import { computed } from 'vue';

const props = defineProps({
  checked: { type: Array, required: true },
});

const uncheckedItems = computed(() => {
  return checklistStore.filter((_, index) => !props.checked?.[index]);
});
</script>

<template>
  <section class="FinalChecklist" aria-labelledby="unchecked-title">
    <!-- 스크린 리더용 숨김 제목 -->
    <h2 id="unchecked-title" class="visually-hidden">체크하지 않은 항목</h2>
    <article
      v-for="(item, index) in uncheckedItems"
      :key="index"
      class="checkedlist-box p-3 rounded mb-3 text-start"
      role="region"
      aria-label="체크하지 않은 항목"
    >
      <p class="mb-0" v-html="item"></p>
    </article>
  </section>
</template>

<style scoped>
.FinalChecklist {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  margin: 0 auto;
  padding: 0 1rem;
  max-width: 960px;
}

.checkedlist-box {
  width: 100%;
  max-width: 800px;
  background-color: #fffcd4;
  padding: 1rem 1.25rem;
  border-radius: 8px;
  text-align: left;
  font-size: 1rem;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  border: 1px solid #fef08a;
  box-sizing: border-box;
}

@media (max-width: 768px) {
  .checkedlist-box {
    max-width: 95vw;
    padding: 1rem;
  }
}
</style>
