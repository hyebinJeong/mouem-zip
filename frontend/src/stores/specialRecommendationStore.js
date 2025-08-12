import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '@/api/index.js';

export const useSpecialRecommendationStore = defineStore(
  'specialRecommendation',
  () => {
    const recommendations = ref([]);

    const fetchRecommendations = async () => {
      try {
        const response = await api.get('/api/recommendation');
        recommendations.value = response.data;
      } catch (error) {
        console.error('특약 추천 항목 불러오기 실패:', error);
      }
    };

    return {
      recommendations,
      fetchRecommendations,
    };
  }
);
