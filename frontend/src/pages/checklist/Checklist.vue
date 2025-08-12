<template>
  <div class="checklist-page">
    <div class="wrapper">
      <div class="container">
        <h1 class="main-title">
          계약 전에서 놓치는 부분이 없도록
          <span class="blue-text">체크리스트</span>를 통해 도와드릴게요.
        </h1>

        <div class="content-box">
          <div class="slider-wrapper">
            <button class="arrow left" @click="prevSlide">
              <svg viewBox="0 0 24 24" width="50" height="50" fill="#888">
                <polygon points="15,5 7,12 15,19" />
              </svg>
            </button>

            <div class="slider">
              <img
                :src="images[currentIndex]"
                class="slider-img"
                @click="openModal"
              />
            </div>

            <button class="arrow right" @click="nextSlide">
              <svg viewBox="0 0 24 24" width="50" height="50" fill="#888">
                <polygon points="9,5 17,12 9,19" />
              </svg>
            </button>
          </div>

          <div class="checklist-area">
            <div class="checklist-box">
              <ul class="checklist">
                <li v-for="(item, index) in checklistItems" :key="index">
                  <label :for="`todo-${index}`" class="item">
                    <input
                      type="checkbox"
                      :id="`todo-${index}`"
                      class="hidden"
                      v-model="checked[index]"
                    />
                    <label :for="`todo-${index}`" class="cbx">
                      <svg width="14px" height="12px" viewBox="0 0 14 12">
                        <polyline points="1 7.6 5 11 13 1"></polyline>
                      </svg>
                    </label>

                    <span
                        v-if="!checked[index]"
                        class="risk-indicator"
                        :class="{
                        red: item.riskColor === 'red',
                        yellow: item.riskColor === 'yellow',
                        green: item.riskColor === 'green',
                      }"
                    ></span>

                    <label :for="`todo-${index}`" class="cbx-lbl">
                      {{ item.text }}
                      <span v-if="!checked[index]" v-html="item.span"></span>
                    </label>
                  </label>
                </li>
              </ul>
            </div>
            <div class="risk-legend">
              <span class="risk-indicator red"></span> 중요
              <span class="risk-indicator yellow"></span> 주의
              <span class="risk-indicator green"></span> 참조
            </div>
            <div class="button-row">
              <button class="next-button" @click="goNext">확인 완료</button>
            </div>
          </div>
        </div>
      </div>

      <Buddy @open-dictionary="openDictionaryModal" />

      <div v-if="showModal" class="image-modal" @click.self="closeModal">
        <img :src="images[currentIndex]" class="modal-img" />
      </div>

      <TermViewModal v-if="showDictionaryModal" @close="closeDictionaryModal" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Buddy from '@/components/BuddyHelper.vue';
import checklistItems from '@/stores/checklistStore2.js';
import TermViewModal from '@/components/TermViewModal.vue';
import { useAuthStore } from '@/stores/auth';
const route = useRoute();
const userId = Number(route.query.userId);
const registryId = Number(route.query.registryId);

const showDictionaryModal = ref(false);
const openDictionaryModal = () => {
  showDictionaryModal.value = true;
};
const closeDictionaryModal = () => {
  showDictionaryModal.value = false;
};

const router = useRouter();
const auth = useAuthStore();
const images = [
  new URL('@/assets/표준계약서1.png', import.meta.url).href,
  new URL('@/assets/표준계약서2.png', import.meta.url).href,
  new URL('@/assets/표준계약서3.png', import.meta.url).href,
];

const currentIndex = ref(0);
const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % images.length;
};
const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + images.length) % images.length;
};

const checked = ref(Array(checklistItems.length).fill(false));

const goNext = async () => {
  if (!registryId) {
    router.push('/');
    return;
  }

  const payload = { userId, registryId, checked: checked.value };

  try {
    const res = await fetch('http://localhost:8080/api/checklist', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${auth.token}`,
      },
      body: JSON.stringify(payload),
    });

    if (!res.ok) throw new Error('전송 실패');
    alert('체크리스트 저장 완료!');
    router.push({
      path: '/final-report',
      query: { userId, registryId },
    });
  } catch (e) {
    console.error(e);
    alert('저장에 실패했습니다.');
  }
};

const showModal = ref(false);
const openModal = () => {
  showModal.value = true;
};
const closeModal = () => {
  showModal.value = false;
};

const onEscPress = (e) => {
  if (e.key === 'Escape') closeModal();
};

onMounted(() => window.addEventListener('keydown', onEscPress));
onBeforeUnmount(() => window.removeEventListener('keydown', onEscPress));
</script>

<style lang="scss" scoped>
$main: #1a80e5;

.checklist-page {
  .wrapper {
    width: 100%;
    min-height: calc(100vh - 140px);
    display: flex;
    justify-content: center;
  }

  .container {
    width: 100%;
    max-width: 1200px;
    padding: 20px;
    box-sizing: border-box;
  }

  .main-title {
    font-size: 24px;
    font-weight: bold;
    color: #222;
    text-align: center;
    margin: 20px 0;
  }

  .blue-text {
    color: $main;
    margin-left: 6px;
  }

  .content-box {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 70px;
  }

  .slider-wrapper {
    position: relative;
    max-width: 600px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24px;
    flex: none;
    max-height: calc(100vh - 300px);

    .arrow {
      position: absolute;
      top: 50%;
      transform: translateY(-50%);
      border: none;
      cursor: pointer;
      z-index: 2;
      padding: 8px;
      border-radius: 50%;
      transition: background 0.2s ease;
      background-color: white;

      svg {
        width: 32px;
        height: 32px;
        fill: #9e9e9e;
      }

      &.left {
        left: -50px;
      }

      &.right {
        right: -50px;
      }
    }
  }

  .slider {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;

    .slider-img {
      max-width: 100%;
      max-height: calc(100vh - 300px);
      border-radius: 10px;
      cursor: zoom-in;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      object-fit: contain;
    }
  }

  .checklist-area {
    flex: none;
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    max-width: 800px;

    .risk-legend {
      margin-bottom: 16px;
      font-size: 15px;
      font-weight: 600;
      color: #444;
      user-select: none;
      display: flex;
      gap: 20px;
      align-items: center;

      .risk-indicator {
        width: 16px;
        height: 16px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 6px;
        flex-shrink: 0;
      }

      .risk-indicator.red {
        background-color: #ff6161;
      }

      .risk-indicator.yellow {
        background-color: yellow;
      }

      .risk-indicator.green {
        background-color: #72ff5d;
      }
    }
  }

  .checklist-box {
    background-color: #f7f9fc;
    padding: 20px;
    border-radius: 16px;
    width: 100%;
    max-width: 1000px;
    min-height: 300px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    margin-bottom: 30px;

    .checklist {
      margin-top: 10px;

      li {
        margin-bottom: 16px;
        display: flex;
        align-items: center;
        font-size: 17px;
        gap: 12px;
        line-height: 2.2;
      }

      .item {
        display: flex;
        align-items: center;
        user-select: none;
      }

      /* 체크박스 커스텀 */
      .cbx {
        position: relative;
        width: 18px;
        height: 18px;
        margin-right: 6px;
        border: 1px solid #c8ccd4;
        border-radius: 3px;
        cursor: pointer;

        svg {
          position: relative;
          top: -10px;
          transform: scale(0);
          fill: none;
          stroke-linecap: round;
          stroke-linejoin: round;

          polyline {
            stroke-width: 3;
            stroke: $main;
          }
        }

        &:before {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          width: 20px;
          height: 20px;
          margin: -10px 0 0 -10px;
          border-radius: 50%;
          background: $main;
          transform: scale(0);
        }

        &:after {
          content: '';
          position: absolute;
          top: 5px;
          left: 5px;
          width: 2px;
          height: 2px;
          border-radius: 2px;
          box-shadow: 0 -18px 0 $main, 12px -12px 0 $main, 18px 0 0 $main,
            12px 12px 0 $main, 0 18px 0 $main, -12px 12px 0 $main,
            -18px 0 0 $main, -12px -12px 0 $main;
          transform: scale(0);
        }
      }

      .cbx-lbl {
        position: relative;
        cursor: pointer;
        transition: color 0.3s ease;
        margin-left: 10px;

        &:after {
          content: '';
          position: absolute;
          top: 50%;
          left: 0;
          width: 0;
          height: 1px;
          background: #9098a9;
        }
      }

      input[type='checkbox'] {
        display: none;

        &:checked + .cbx {
          border-color: transparent;

          svg {
            transform: scale(1.5);
            transition: all 0.4s ease;
            transition-delay: 0.1s;
          }

          &:before {
            transform: scale(1);
            opacity: 0;
            transition: all 0.3s ease;
          }

          &:after {
            transform: scale(1);
            opacity: 0;
            transition: all 0.6s ease;
          }
        }

        &:checked ~ .cbx-lbl {
          color: #9098a9;

          &:after {
            width: 100%;
            transition: all 0.4s ease;
          }
        }
      }

      /* 위험도 컬러 원 스타일 */
      .risk-indicator {
        display: inline-block;
        width: 14px;
        height: 14px;
        border-radius: 50%;
        margin-left: 8px;
        flex-shrink: 0;
      }
      .risk-indicator.red {
        background-color: #ff6161;
      }
      .risk-indicator.yellow {
        background-color: yellow;
      }
      .risk-indicator.green {
        background-color: #72ff5d;
      }
    }
  }

  .button-row {
    display: flex;
    justify-content: flex-end;
    width: 90%;
    max-width: 800px;
  }

  .next-button {
    background-color: $main;
    color: white;
    font-weight: 600;
    font-size: 16px;
    padding: 14px 32px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s;

    &:hover {
      background-color: #2563eb;
    }
  }

  .image-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background-color: rgba(0, 0, 0, 0.7);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;

    .modal-img {
      max-width: 90%;
      max-height: 90%;
      border-radius: 10px;
      box-shadow: 0 0 12px rgba(0, 0, 0, 0.5);
      cursor: zoom-out;
    }
  }
}
</style>
