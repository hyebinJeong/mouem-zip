<script setup>
import { onMounted } from 'vue';
import DonutChart from './charts/DonutChart.vue';
import axios from 'axios';
import { ref } from 'vue';
import { getGradeColor } from '@/composables/final-report/useGradeColor';

const props = defineProps({
  registry: { type: String, default: '' },
  jeonse: { type: String, default: '' },
  checklist: { type: String, default: '' },
});
</script>

<template>
  <section class="FinalGrade" aria-labelledby="final-grade-title">
    <!-- 스크린 리더용 숨김 제목-->
    <h2 id="final-grade-title" class="visually-hidden">종합 등급</h2>
    <div class="donut-wrap">
      <div class="donut-item" v-if="registry">
        <DonutChart
          :grade="registry"
          :color="getGradeColor(registry) || '#ddd'"
          label="등기부등본"
        />
      </div>
      <div class="donut-item" v-if="jeonse">
        <DonutChart
          :grade="jeonse"
          :color="getGradeColor(jeonse) || '#ddd'"
          label="전세가율"
        />
      </div>
      <div class="donut-item" v-if="checklist">
        <DonutChart
          :grade="checklist"
          :color="getGradeColor(checklist) || '#ddd'"
          label="체크리스트"
        />
      </div>
    </div>
  </section>
</template>

<style scoped>
.FinalGrade {
  display: flex;
  justify-content: center;
  padding: 1rem;
}

.donut-wrap {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  justify-items: center;
  gap: 4rem 6rem;
  margin: 1rem 0;
}

.donut-item {
  width: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

@media (max-width: 768px) {
  .donut-wrap {
    grid-template-columns: 1fr;
    gap: 1rem 1rem;
  }
}
</style>
