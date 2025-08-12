import { defineStore } from 'pinia';
import api from '@/api';
import { useAuthStore } from '@/stores/auth';

export const useSpecialContractsStore = defineStore('specialContracts', {
  state: () => ({
    contracts: [],
    loading: false,
    error: null,
  }),

  actions: {
    async fetchContracts() {
      this.loading = true;
      this.error = null;
      try {
        const res = await api.get('/api/specialcontracts-manager');
        this.contracts = res.data ?? [];
      } catch (err) {
        this.error = err;
        console.error('특약사항 목록 조회 오류:', err);
      } finally {
        this.loading = false;
      }
    },

    async fetchContractById(id) {
      try {
        const res = await api.get(`/api/specialcontracts-manager/${id}`);
        return res.data;
      } catch (err) {
        this.error = err;
        console.error('특약사항 단건 조회 오류:', err);
        return null;
      }
    },

    getContractById(id) {
      return this.contracts.find((c) => c.contractId === id);
    },

    async addContract(newContract) {
      try {
        const auth = useAuthStore();
        await api.post('/api/specialcontracts-manager', newContract, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        await this.fetchContracts();
      } catch (err) {
        this.error = err;
        console.error('특약사항 추가 오류:', err);
        throw err;
      }
    },

    async updateContract(id, updatedContract) {
      try {
        const auth = useAuthStore();
        await api.put(`/api/specialcontracts-manager/${id}`, updatedContract, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        await this.fetchContracts();
      } catch (err) {
        this.error = err;
        console.error('특약사항 수정 오류:', err);
        throw err;
      }
    },

    async deleteContract(id) {
      try {
        const auth = useAuthStore();
        await api.delete(`/api/specialcontracts-manager/${id}`, {
          headers: { Authorization: `Bearer ${auth.token}` },
        });
        this.contracts = this.contracts.filter((c) => c.contractId !== id);
      } catch (err) {
        this.error = err;
        console.error('특약사항 삭제 오류:', err);
        throw err;
      }
    },
  },
});
