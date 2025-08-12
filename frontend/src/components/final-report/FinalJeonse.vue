<script setup>
import BarChart from './charts/BarChart.vue';

const props = defineProps({
  jeonseRatio: { type: Number, default: 0 },
  regionAvgJeonseRatio: { type: Number, default: 0 },
});
</script>

<template>
  <section class="FinalJeonse" aria-label="전세가율 비교 막대 차트">
    <BarChart
      :jeonseRatio="jeonseRatio"
      :regionAvgJeonseRatio="regionAvgJeonseRatio"
    />
  </section>
</template>

<style scoped>
/* .FinalJeonse {
} */
</style>
