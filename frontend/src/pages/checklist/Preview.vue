<template>
  <div class="wrapper">
    <div class="container">
      <div v-if="properties.length > 0">
        <h1 class="main-title">
          매물 안전성 분석을 받은 매물이 있어요.<br />
          어떤 매물의 체크리스트를 확인해볼까요?
        </h1>

        <div class="table-wrapper">
          <table class="info-table">
            <thead>
              <tr>
                <th>진단 매물</th>
                <th>생성일</th>
                <th>남은 기간</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(item, index) in properties"
                :key="index"
                @click="!isExpired(item) && handleRowClick(item)"
                :class="{
                  'clickable-row': !isExpired(item),
                  'disabled-row': isExpired(item),
                }"
              >
                <td>{{ item.registryName }}</td>
                <td>{{ item.createdAt }}</td>
                <td
                  class="expires-text"
                  :style="{ color: isExpired(item) ? '#999' : '#1a80e5' }"
                >
                  {{ item.expiresIn }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="checklist-with-property">
          <h2 class="main-title">사전 체크용으로 체크리스트만 확인할래요</h2>
          <p class="small-text">
            안전 등급 분석에 포함되지 않고, 확인만 가능해요.
          </p>
          <div class="button-wrapper">
            <button @click="goToChecklist" class="blue-button">
              체크리스트 확인하러가기
            </button>
          </div>
        </div>
      </div>

      <!-- 분석 매물이 없을 때 -->
      <div class="noexistcontainer" v-else>
        <div class="noexistwrapper">
          <img
            src="@/assets/buddypink.png"
            alt="버디"
            class="buddy-character"
          />
          <h1 class="main-title">
            매물 안전성 분석을 받은 매물이 아직 없어요.<br />
            매물 안전성 진단을 먼저 추천드릴게요!
          </h1>
        </div>

        <div class="button-wrapper">
          <button @click="goToAgreement" class="blue-button2">
            매물 안전성 진단하러 가기
          </button>
        </div>

        <div class="checklist-preview">
          <h3 class="sub-title">
            사전체크용으로 체크리스트만 확인할 수도 있어요.
          </h3>
          <p class="small-text">
            매물 진단 후 체크리스트를 완성하면 정확한 안전 등급을
            제공해드려요.<br />
            아래 버튼은 진단 없이 체크리스트만 미리 볼 수있어요.
          </p>
          <div class="text-button-wrapper">
            <button @click="goToChecklist" class="text-button">
              체크리스트 확인하러가기
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import api from '@/api/index.js'; // API 호출을 위한 axios 인스턴스

const router = useRouter();
const properties = ref([]);

const auth = useAuthStore();
const userId = auth.userId || null; // 로그인하지 않은 경우 null
//const userId = 1;

onMounted(async () => {
  if (!userId) return; // userId 없으면 요청 안함

  try {
    const registryRes = await api.get(
      `http://localhost:8080/api/registry/user/${userId}`
    );
    const registryList = registryRes.data;

    const today = new Date();

    properties.value = registryList.map((item) => {
      const createdDate = new Date(item.analysisDate);
      const diffTime =
        createdDate.getTime() + 50 * 24 * 60 * 60 * 1000 - today.getTime();
      const remainingDays = Math.max(
        0,
        Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      );

      return {
        registryId: item.registryId,
        registryName: item.registryName,
        createdAt: createdDate.toLocaleDateString('ko-KR'),
        expiresIn:
          remainingDays > 0
            ? `${remainingDays}일 후에 만료됩니다.`
            : '만료되었습니다.',
        userId: userId,
      };
    });
  } catch (error) {
    console.error('데이터 불러오기 실패:', error);
  }
});

function goToChecklist() {
  router.push('/checklist/checklist');
}

function handleRowClick(item) {
  router.push({
    path: '/checklist/checklist',
    query: {
      userId: userId,
      registryId: item.registryId,
    },
  });
}

function isExpired(item) {
  return item.expiresIn === '만료되었습니다.';
}

function goToAgreement() {
  router.push('/agreement');
}
</script>

<style scoped>
.wrapper {
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px 100px;
}

.container {
  width: 100%;
  max-width: 800px;
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  padding: 32px;
}

.main-title {
  font-size: 32px;
  font-weight: bold;
  color: #222;
  line-height: 1.5;
}

.table-wrapper {
  border: 1px solid #ddd;
  border-radius: 8px;
  overflow: hidden;
  width: 80%;
  margin: 10px 0 60px 0;
}

.info-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.info-table thead {
  background-color: #f4f4f4;
  color: #555;
  text-align: left;
}

.info-table th,
.info-table td {
  padding: 12px;
  border-top: 1px solid #eee;
}

.clickable-row {
  cursor: pointer;
  transition: background-color 0.2s;
}

.clickable-row:hover {
  background-color: #f0f8ff;
}

.small-text {
  font-size: 14px;
  color: #888;
}

.button-wrapper {
  margin-top: 10px;
}

.blue-button {
  background-color: #1a80e5;
  width: 288px;
  height: 59px;
  font-size: 14px;
  color: white;
  font-weight: 600;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s;
}

.blue-button2 {
  background-color: #1a80e5;
  width: 288px;
  height: 59px;
  border: none;
  font-size: 17px;
  color: #fff;
  font-weight: 600;
  margin-bottom: 30px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-top: 30px;
}

.blue-button:hover {
  background-color: #2563eb;
}

.expires-text {
  color: #1a80e5;
  font-weight: 500;
}
.disabled-row {
  color: #999;
  background-color: #ffffff;
  cursor: not-allowed;
}

.disabled-row:hover {
  background-color: #ffffff !important;
}
.noexistwrapper {
  display: flex;
  margin-top: 50px;
}
.buddy-character {
  height: 120px;
  align-items: flex-end;
  margin-right: 20px;
  animation: buddyJump 1s ease forwards, buddyWave 2s ease-in-out infinite 1s;
  transform-origin: bottom center;
}

.sub-title {
  font-size: 20px;
  font-weight: 600;
  color: #4a4a4a;
  margin-top: 30px;
  margin-bottom: 10px;
}

.text-button-wrapper {
  margin-bottom: 60px;
}

.text-button {
  background: none;
  border: none;
  color: #1a80e5;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  padding: 0;
  text-decoration: none;
}

.text-button:hover {
  text-decoration: underline;
}
.checklist-preview {
  margin-top: 30px;
}
.noexistcontainer {
  align-items: center;
  display: flex;
  flex-direction: column;
}

@keyframes buddyJump {
  0% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10px);
  }
  50% {
    transform: translateY(0);
  }
  70% {
    transform: translateY(-5px);
  }
  100% {
    transform: translateY(0);
  }
}

@keyframes buddyWave {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(2deg);
  }
  50% {
    transform: rotate(0deg);
  }
  75% {
    transform: rotate(-2deg);
  }
  100% {
    transform: rotate(0deg);
  }
}
</style>
