<script setup>
import { Chart, ArcElement } from 'chart.js';
import { Doughnut } from 'vue-chartjs';
import { ref, watch } from 'vue';

Chart.register(ArcElement);

const props = defineProps({
  grade: { type: String, default: '' },
  color: { type: String, default: '#ddd' },
  label: { type: String, default: '' },
});

const chartData = ref(null);
const chartOptions = ref(null);

watch(
  () => [props.grade, props.color],
  () => {
    if (!props.grade || !props.color) {
      chartData.value = null;
      chartOptions.value = null;
      return;
    }

    chartData.value = {
      labels: [],
      datasets: [
        {
          data: [100],
          backgroundColor: [props.color],
          borderWidth: 0,
          cutout: '90%',
        },
      ],
    };

    chartOptions.value = {
      responsive: false,
      plugins: {
        legend: { display: false },
        tooltip: { enabled: false },
        datalabels: { display: false },
      },
    };
  },
  { immediate: true }
);
</script>

<template>
  <figure class="DonutChart" v-if="chartData && chartOptions">
    <div class="canvas-container">
      <Doughnut
        :data="chartData"
        :options="chartOptions"
        :width="180"
        :height="180"
      />
      <div class="donut-center-text" :style="{ color: props.color }">
        {{ props.grade }}
      </div>
    </div>
    <figcaption class="donut-label">{{ props.label }}</figcaption>
  </figure>
</template>

<style scoped>
.DonutChart {
  position: relative;
  margin: 0 auto 2rem auto;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2rem;
}

.donut-center-text {
  position: absolute;
  top: 35%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.2rem;
  font-weight: bold;
}

.donut-label {
  text-align: center;
  margin-top: 0.75rem;
  font-size: 1.3rem;
  font-weight: 700;
  color: #000;
}
</style>
