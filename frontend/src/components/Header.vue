<template>
  <div class="container">
    <div class="top-bar">
      <div class="logo-wrapper" @click="goHome">
        <img class="logo" :src="logo" alt="HomeBuddy Logo" />
      </div>

      <div class="hamburger" @click="toggleMenu">
        <span></span>
        <span></span>
        <span></span>
      </div>

      <div
          class="menu-container"
          ref="menuContainerRef"
          :class="{ 'menu-open': isMenuOpen }"
          :style="{ color: fontColor }"
      >
        <div class="menu-item" @click="goSafetyCheck">매물 안전성 진단</div>
        <div class="menu-item" @click="goChecklist">체크리스트</div>

        <!-- 계약 가이드 -->
        <div
            class="menu-item dropdown"
            @mouseenter="!isMobile && (showContractGuide = true)"
            @mouseleave="!isMobile && (showContractGuide = false)"
            @click="toggleContractGuide"
        >
          계약 가이드

          <!-- 데스크탑 전용 드롭다운 -->
          <ul class="dropdown-list" v-if="!isMobile && showContractGuide">
            <li @click.stop="goReferenceContract">참고계약서 작성</li>
            <li @click.stop="goReferenceGuidebook">참고 가이드북</li>
          </ul>
        </div>

        <div class="menu-item" @click="goGlossary">용어해설집</div>

        <button
            v-if="userId"
            class="mypage"
            :style="{ color: fontColor }"
            @click="goMyPage"
        >
          마이페이지
        </button>
        <button
            v-else
            class="mypage"
            :style="{ color: fontColor }"
            @click="goLogin"
        >
          로그인
        </button>
      </div>

      <!-- 모바일 전용 드롭다운 패널 -->
      <div
          v-if="isMobile && showContractGuide"
          class="dropdown-panel"
          :style="dropdownStyle"
      >
        <ul>
          <li @click.stop="goReferenceContract">참고계약서 작성</li>
          <li @click.stop="goReferenceGuidebook">참고 가이드북</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import logo from '@/assets/homebuddylogo.png';

const props = defineProps({
  fontColor: String,
});

const router = useRouter();
const showContractGuide = ref(false);
const isMenuOpen = ref(false);
const isMobile = ref(window.innerWidth <= 768);

const auth = useAuthStore();
const userId = computed(() => auth.userId);

const menuContainerRef = ref(null);
const dropdownPos = ref({ top: 0, right: 0 });

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
  if (!isMenuOpen.value) {
    showContractGuide.value = false;
  }
};

const toggleContractGuide = () => {
  if (isMobile.value) {
    const rect = menuContainerRef.value.getBoundingClientRect();
    dropdownPos.value.top = rect.top + 130;
    dropdownPos.value.right = window.innerWidth - rect.left + 10;
    showContractGuide.value = !showContractGuide.value;
  }
};

const dropdownStyle = computed(() => ({
  position: 'fixed',
  top: `${dropdownPos.value.top}px`,
  right: `${dropdownPos.value.right}px`,
}));

const updateIsMobile = () => {
  isMobile.value = window.innerWidth <= 768;
  if (!isMobile.value) {
    showContractGuide.value = false;
  }
};

onMounted(() => {
  window.addEventListener('resize', updateIsMobile);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateIsMobile);
});

const goHome = () => router.push('/');
const goSafetyCheck = () => router.push('/agreement');
const goChecklist = () => router.push('/checklist');
const goReferenceContract = () => router.push('/referencecontracts');
const goReferenceGuidebook = () => router.push('/reference-guidebook');
const goGlossary = () => router.push('/glossary');
const goMyPage = () => router.push('/my');
const goLogin = () => router.push('/login');
</script>

<style scoped>
.container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  border-bottom: 1.5px solid #eaeaea;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 95%;
  height: 130px;
  box-sizing: border-box;
  position: relative;
}

.logo-wrapper {
  width: 250px;
  display: flex;
  height: 120px;
  justify-content: flex-start;
  align-items: center;
  cursor: pointer;
}

.logo-wrapper:hover {
  transform: scale(1.03);
  transition: transform 0.3s ease;
}

.logo {
  width: 200px;
  height: auto;
}

.menu-container {
  width: 75%;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-left: 50px;
  color: inherit;
}

.menu-item {
  position: relative;
  font-weight: bolder;
  font-size: 20px;
  font-family: Regular;
  height: 2.5em;
  display: flex;
  align-items: center;
  cursor: pointer;
  z-index: 2;
  user-select: none;
  margin-right: 80px;
}

.mypage {
  background-color: transparent;
  border: none;
  font-weight: bolder;
  font-size: 20px;
  cursor: pointer;
  z-index: 3;
  margin-left: auto;
}

.mypage:hover {
  color: #007bff;
  transition: all 0.1s ease-in-out;
}

.menu-item:hover {
  color: #007bff;
  border-bottom: 2px solid #007bff;
  transition: all 0.1s ease-in-out;
}

.hamburger {
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  margin-left: auto;
  cursor: pointer;
}

.hamburger span {
  width: 25px;
  height: 3px;
  background-color: #333;
  border-radius: 3px;
}

/* 데스크탑 전용 드롭다운 */
.dropdown-list {
  background-color: #ececec;
  font-size: 18px;
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  width: 170px;
  margin: 0;
  padding: 0;
  list-style: none;
  z-index: 10;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  border-radius: 5px;
  overflow: hidden;
}

.dropdown-list li {
  font-weight: normal;
  height: 3em;
  display: flex;
  align-items: center;
  justify-content: center;
  color: black;
  background-color: transparent;
  cursor: pointer;
}

.dropdown-list li:hover {
  background-color: white;
}

/* 모바일 전용 드롭다운 패널 */
.dropdown-panel {
  width: 180px;
  background: white;
  border: 1px solid #eaeaea;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  border-radius: 5px;
  z-index: 2000;
}

.dropdown-panel ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.dropdown-panel li {
  padding: 10px;
  cursor: pointer;
  text-align: center;
  background-color: #f7f9fc;
  border-bottom: 1px solid #f0f0f0;
}

.dropdown-panel li:hover {
  background-color: #f5f5f5;
}

@media screen and (max-width: 768px) {
  .hamburger {
    display: flex;
  }

  .menu-container {
    display: none;
    flex-direction: column;
    align-items: center;
    background-color: #f7f9fc;
    position: absolute;
    top: 130px;
    right: 0;
    width: 50%;
    padding: 10px;
    border-top: 1px solid #eaeaea;
    z-index: 999;
  }

  .menu-container.menu-open {
    display: flex;
  }

  .menu-item {
    margin: 10px 0;
    font-size: 16px;
  }

  .mypage {
    margin: 10px 0 0 0;
    font-size: 16px;
    padding: 10px;
    align-self: center;
  }
}
</style>
