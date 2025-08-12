import { defineStore } from 'pinia';
import api from '@/api';
import { useAuthStore } from '@/stores/auth';

export const useCategoryStore = defineStore('category', {
  state: () => ({
    categories: [],
    loading: false,
    error: null,
  }),
  actions: {
    async fetchCategories() {
      this.loading = true;
      this.error = null;
      try {
        const res = await api.get('/api/category-manager');
        this.categories = res.data ?? [];
      } catch (err) {
        this.error = err;
        console.error('카테고리 목록 조회 오류:', err);
      } finally {
        this.loading = false;
      }
    },

    async addCategory(newCategory) {
      try {
        const auth = useAuthStore();
        const res = await api.post('/api/category-manager', newCategory, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        this.categories.push(res.data);
      } catch (err) {
        this.error = err;
        console.error('카테고리 추가 오류:', err);
        throw err;
      }
    },

    async updateCategory(id, updatedCategory) {
      try {
        const auth = useAuthStore();
        await api.put(`/api/category-manager/${id}`, updatedCategory, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        const i = this.categories.findIndex((c) => c.categoryId === id);
        if (i !== -1)
          this.categories[i] = { categoryId: id, ...updatedCategory };
      } catch (err) {
        this.error = err;
        console.error('카테고리 수정 오류:', err);
        throw err;
      }
    },

    async deleteCategory(id) {
      try {
        const auth = useAuthStore();
        await api.delete(`/api/category-manager/${id}`, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        this.categories = this.categories.filter((c) => c.categoryId !== id);
      } catch (err) {
        this.error = err;
        console.error('카테고리 삭제 오류:', err);
        throw err;
      }
    },

    getCategoryById(id) {
      return this.categories.find((c) => c.categoryId === id);
    },
  },
});
