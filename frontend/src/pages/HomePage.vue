<script setup>
import { useRouter, useRoute } from 'vue-router';
// 라우트, AuthStore, onMounted 임시 사용
import { useAuthStore } from '@/stores/auth';
import { onMounted } from 'vue';
import axios from 'axios';

const route = useRoute();
const auth = useAuthStore();
const router = useRouter();

onMounted(() => {
  const token = route.query.token;
  if (token) {
    auth.login(token); // pinia에 저장
    router.replace('/'); // token 숨기기
  }
});

async function goToAnalysis() {
  if (!auth.isLoggedIn) {
    // JWT가 없으면 로그인 페이지로 이동
    alert('로그인이 필요합니다.');
    router.push('/login');
    return;
  }

  try {
    //  로그인 상태라면 백엔드 권한 확인
    await axios.get('/api/check-access/agreement', {
      headers: { Authorization: `Bearer ${auth.token}` },
    });

    // 200 OK면 진단 페이지로 이동
    router.push('/agreement');
  } catch (err) {
    console.error('접근 권한 없음:', err);
    router.push('/login'); // 인증 실패 시 로그인 페이지로 이동
  }
}
</script>

<template>
  <div class="wrapper">
    <div class="container">
      <!-- 상단 안내 -->
      <div class="header-section">
        <img
          src="@/assets/buddyyellow.png"
          alt="버디"
          class="buddy-character"
        />
        <div class="text-area">
          <h1 class="main-title">
            계약 전 <span class="red-text">리스크</span>,
            <span class="blue-text">HomeBuddy</span>에서 미리
            <span class="green-text">분석</span>하세요.
          </h1>
          <p class="description">
            복잡한 등기부등본 분석, 계약서, 체크리스트, 전세가율 분석까지
            도와드려요.<br />
            잘 모르는 부동산 용어는 용어해설집으로 해결하세요!
          </p>
        </div>
      </div>

      <!-- 타이틀 -->
      <h2 class="section-title">
        <span class="blue-text">HomeBuddy</span>를 이렇게 사용해보세요.
      </h2>

      <!-- 사용 단계 -->
      <div class="steps-section">
        <div class="step-box">
          <img src="@/assets/diagnosis.png" alt="매물 분석" class="step-icon" />
          <br />
          <p class="step-title">매물 안전성 분석</p>
          <br />
          <p class="step-desc">
            이 매물, 괜찮을까?<br />
            등기부등본과 전세가율로<br />
            위험도를 쉽게 확인해보세요.
          </p>
        </div>

        <div class="arrow">▶</div>

        <div class="step-box">
          <img
            src="@/assets/checklist.png"
            alt="체크리스트"
            class="step-icon"
          />
          <br />
          <p class="step-title">체크리스트 제공</p>
          <br />
          <p class="step-desc">
            표준계약서 이미지와 체크리스트로<br />
            놓치기 쉬운 부분들을<br />
            점검할 수 있게 도와드려요.
          </p>
        </div>

        <div class="arrow">▶</div>

        <div class="step-box">
          <img src="@/assets/report.png" alt="리포트" class="step-icon" />
          <br />
          <p class="step-title">계약 리스크 분석 리포트 제공</p>
          <br />
          <p class="step-desc">
            내가 체크한 정보가 곧 나의 이해도!<br />
            매물 안전성과 체크한 내용이 반영된<br />
            종합 진단 등급으로 확인하세요.
          </p>
        </div>
      </div>

      <!-- 분석하러가기 버튼 -->
      <div class="cta-section">
        <button class="blue-button" @click="goToAnalysis">
          매물 안전성 분석하러 가기
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wrapper {
  width: 100vw;
  display: flex;
  justify-content: center;
  padding: 30px 20px;
}

.container {
  width: 100%;
  max-width: 1080px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-section {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
  gap: 20px;
}

.buddy-character {
  height: 120px;
}

.text-area {
  flex: 1;
}

.main-title {
  font-size: 24px;
  font-weight: 600;
  line-height: 1.6;
}

.description {
  font-size: 14px;
  color: #666;
  margin-top: 10px;
  line-height: 1.6;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #222;
  margin-bottom: 30px;
  text-align: center;
  width: 100%;
}

.steps-section {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 50px;
  width: 100%;
}

.step-box {
  background-color: #e9f1f9;
  border-radius: 20px;
  padding: 24px;
  text-align: center;
  width: 280px;
  min-height: 240px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.step-icon {
  height: 100px;
  margin-bottom: 16px;
}

.step-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #222;
}

.step-desc {
  font-size: 14px;
  color: #555;
  line-height: 1.5;
}

.arrow {
  font-size: 32px;
  color: #999;
}

.cta-section {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 60px;
}

.blue-button {
  background-color: #1a80e5;
  color: white;
  font-weight: 600;
  font-size: 16px;
  padding: 14px 28px;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.blue-button:hover {
  background-color: #2563eb;
}

.red-text {
  color: #e45645;
}

.blue-text {
  color: #1a80e5;
}

.green-text {
  color: #28a745;
}
@keyframes stepFadeIn {
  0% {
    opacity: 0;
    transform: translateY(30px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

.step-box {
  opacity: 0;
  animation: stepFadeIn 0.8s ease forwards;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.step-box:nth-of-type(1) {
  animation-delay: 0.25s;
}
.step-box:nth-of-type(3) {
  animation-delay: 0.5s;
}
.step-box:nth-of-type(5) {
  animation-delay: 0.75s;
}

.step-box:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.15);
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

.buddy-character {
  height: 120px;
  animation: buddyJump 1s ease forwards, buddyWave 2s ease-in-out infinite 1s;
  transform-origin: bottom center;
}
</style>
