<script setup>
import { Bar } from 'vue-chartjs';
import { Chart, BarElement, CategoryScale, LinearScale } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';

// Chart.js 등록
Chart.register(BarElement, CategoryScale, LinearScale, ChartDataLabels);

const props = defineProps({
  jeonseRatio: { type: Number, default: 0 },
  regionAvgJeonseRatio: { type: Number, default: 0 },
});

const chartData = {
  // x축
  labels: ['지역 평균 전세가율', '예상 전세가율'],
  datasets: [
    {
      // y축
      data: [props.regionAvgJeonseRatio || 0, props.jeonseRatio || 0],
      backgroundColor: ['#007bff', '#fec43f'],
      borderRadius: {
        topLeft: 10,
        topRight: 10,
        bottomLeft: 0,
        bottomRight: 0,
      },
      borderSkipped: false,
      barThickness: 50,
    },
  ],
};

const chartOptions = {
  responsive: true, // 반응형 스타일링

  plugins: {
    datalabels: {
      anchor: 'end',
      align: 'end',
      color: '#fff',
      backgroundColor: '#000',
      font: { weight: 'bold', size: 12 },
      padding: 6,
      formatter: (v) => `${v.toFixed(1)}%`,
    },
  },
  scales: {
    y: {
      display: false,
      beginAtZero: true,
      max: 120, // 라벨까지 잘 보이도록
    },
    x: {
      grid: { display: false }, // x축 선 제거
      ticks: {
        font: { size: 14, weight: 'bold' },
        color: '#000',
      },
    },
  },
};
</script>

<template>
  <figure class="BarChart">
    <Bar :data="chartData" :options="chartOptions" :height="300" />
    <!-- 스크린 리더용 숨김 설명 -->
    <figcaption class="visually-hidden">
      지역 평균 전세가율과 예상 전세가율 비교
    </figcaption>
  </figure>
</template>

<style scoped>
.BarChart {
  width: 100%;
  max-width: 320px;
  height: 300px;
  margin: 0 auto;
}
</style>
