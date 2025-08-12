<script setup>
import Header from '../../components/Header.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import fileIcon from '@/assets/fileicon.png';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';
import { onMounted } from 'vue';

const router = useRouter();
const auth = useAuthStore();
const contracts = ref([]);

onMounted(async () => {
  if (!auth.isLoggedIn) return;

  const res = await axios.get('/api/contract/list', {
    headers: { Authorization: `Bearer ${auth.token}` },
  });

  contracts.value = res.data.map((item) => ({
    contractId: item.contractId,
    title: item.contractName,
    date: item.createdAt
      ? new Date(item.createdAt).toISOString().split('T')[0]
      : '',
  }));
});

function onContractClick(item) {
  router.push({
    name: 'ReferenceContractSuccess',
    query: { id: item.contractId, from: 'myPage' },
  });
}
</script>

<template>
  <div class="contract-page">
    <main class="content">
      <h2 class="page-title">참고용 계약서 목록</h2>
      <ul class="contract-list">
        <li
          v-for="(item, idx) in contracts"
          :key="idx"
          class="contract-item clickable"
          @click="onContractClick(item)"
        >
          <div class="icon-box">
            <img :src="fileIcon" alt="file icon" class="file-icon" />
          </div>
          <div class="contract-info">
            <p class="contract-title">{{ item.title }}</p>
            <p class="contract-sub" v-if="item.date">{{ item.date }}</p>
          </div>
        </li>
      </ul>
    </main>
  </div>
</template>

<style scoped>
.contract-page {
  font-family: 'Pretendard', sans-serif;
}

.content {
  max-width: 900px;
  margin: 40px auto;
  padding: 0 5vw;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 27px;
  margin-top: 8px;
}

.page-subtilte {
  font-size: 18px;
  font-weight: 700;
}

.contract-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contract-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.contract-item.clickable {
  cursor: pointer;
  transition: background 0.2s;
  margin-left: -13px;
  margin-right: -13px;
  padding-left: 12px;
  padding-right: 12px;
  border-radius: 7px;
}
.contract-item.clickable:hover {
  background: #f1f1f1;
}

.icon-box {
  margin-right: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 45px;
  height: 45px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #f8f9fa;
}

.file-icon {
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.contract-info {
  display: flex;
  flex-direction: column;
}

.contract-title {
  font-size: 16px;
  font-weight: 600;
  position: relative;
  top: 17px;
}

.contract-sub {
  font-size: 13px;
  color: #888;
  position: relative;
  top: 1px;
}
</style>
