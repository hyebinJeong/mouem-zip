<template>
  <div class="wrapper">
    <h2 class="headline">
      매물 안전성 분석, <strong>딱 <span class="blue">다섯 가지</span></strong
      >만 해요.
    </h2>
    <p class="sub">보증금, 주소를 입력하고 등기부등본만 올리면 끝!</p>

    <div class="input-group">
      <label>1. 전세 보증금을 입력해주세요. (단위: 만원)</label>
      <input
        v-model.number="jeonsePrice"
        type="number"
        min="0"
        placeholder="금액을 입력해주세요. 예시) 30000 (3억)"
        class="input-box"
      />
      <p class="tip" v-if="jeonsePrice" style="margin-top: 4px">
        입력하신 금액: <strong>{{ formatPrice(jeonsePrice) }}</strong>
      </p>
    </div>

    <div class="input-group">
      <label>2. 주소를 입력해주세요.</label>
      <div class="address-box">
        <input
          v-model="selectedAddress"
          placeholder="도로명 또는 지번 주소"
          readonly
          class="input-box"
        />
        <button @click="openPostcode">주소 검색</button>
      </div>
    </div>
    <div class="input-group">
      <label>3. 상세 주소</label>
      <input v-model="detail" placeholder="101동 101호" class="input-box" />
    </div>

    <div class="input-group">
      <label>4. 등기부등본을 업로드해주세요.</label>
      <div
        class="upload-box"
        :class="{ dragover: isDragOver }"
        @click="$refs.fileInput.click()"
        @dragover.prevent="onDragOver"
        @dragenter.prevent="onDragEnter"
        @dragleave="onDragLeave"
        @drop.prevent="onDrop"
      >
        <input
          type="file"
          ref="fileInput"
          accept=".pdf,application/pdf"
          @change="handleFileUpload"
          style="display: none"
        />
        <span>{{
          file?.name ||
          '여기를 눌러 파일을 선택하거나 첨부할 파일을 끌어다 놓으세요'
        }}</span>
      </div>
    </div>

    <div class="input-group">
      <label>5. 매물 이름</label>
      <p class="tip">
        설정한 이름은 체크리스트 진단 매물 선택 시, 마이페이지에서 최종 리포트와
        계약서 조회 시 사용됩니다.
      </p>
      <input
        v-model="registryName"
        placeholder="매물 이름을 설정해주세요."
        class="input-box"
      />
    </div>

    <button
      class="submit-btn"
      :disabled="isSubmitting"
      :class="{ disabled: isSubmitting }"
      @click="handleClick"
    >
      {{ isSubmitting ? '분석 중...' : '분석 시작하기' }}
    </button>
  </div>
  <div
    v-if="showManualInput"
    class="modal-backdrop"
    @click.self="showManualInput = false"
  >
    <div class="modal-box">
      <p class="modal-message">
        면적을 읽어오던 도중 오류가 발생했습니다.<br />
        면적을 직접 입력해주세요.
      </p>
      <div class="unit-input">
        <input
          v-model.number="manualArea"
          type="number"
          step="0.01"
          min="0"
          inputmode="decimal"
          placeholder="면적 입력 (예: 56.88)"
          class="modal-input"
        />
        <span class="unit">㎡</span>
      </div>
      <div class="modal-buttons">
        <button class="modal-btn cancel" @click="showManualInput = false">
          취소
        </button>
        <button class="modal-btn save" @click="saveManualArea">저장</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/stores/auth';
import { ref, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const auth = useAuthStore();

const selectedAddress = ref('');
const roadAddress = ref('');
const jibunAddress = ref('');
const jeonsePrice = ref('');
const detail = ref('');
const file = ref(null);
const registryName = ref('');
const isSubmitting = ref(false);

const manualArea = ref('');
const areaValue = ref('');
const showManualInput = ref(false);
const registerIdRef = ref(null);

const isDragOver = ref(false);

const onDragOver = (e) => {
  isDragOver.value = true;
};
const onDragEnter = (e) => {
  isDragOver.value = true;
};
const onDragLeave = (e) => {
  isDragOver.value = false;
};

const onDrop = (e) => {
  isDragOver.value = false;
  const dt = e.dataTransfer;
  if (!dt || !dt.files || !dt.files.length) return;

  const picked = dt.files[0];
  if (
    !/pdf$/i.test(picked.type) &&
    !picked.name.toLowerCase().endsWith('.pdf')
  ) {
    alert('PDF 파일만 업로드할 수 있어요.');
    return;
  }
  file.value = picked; // 기존 file ref
};

// 브라우저 기본 “파일 열기” 동작 방지(전역)
const preventDefaults = (e) => {
  e.preventDefault();
};
onMounted(() => {
  window.addEventListener('dragover', preventDefaults);
  window.addEventListener('drop', preventDefaults);
});
onUnmounted(() => {
  window.removeEventListener('dragover', preventDefaults);
  window.removeEventListener('drop', preventDefaults);
});

const openPostcode = () => {
  new window.daum.Postcode({
    oncomplete: function (data) {
      const userType = data.userSelectedType;
      if (userType === 'R' && data.roadAddress) {
        roadAddress.value = data.roadAddress;
        selectedAddress.value = data.roadAddress;
      } else if (userType === 'J' && data.jibunAddress) {
        jibunAddress.value = data.jibunAddress;
        selectedAddress.value = data.jibunAddress;
      } else {
        selectedAddress.value = '';
      }
    },
  }).open();
};

const handleFileUpload = (event) => {
  file.value = event.target.files[0];
};

const handleClick = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    await submitForm();
  } catch (e) {
    alert('분석 실패: ' + e.message);
  } finally {
    isSubmitting.value = false;
  }
};

// 숫자를 억/만원 단위로 변환해주는 함수
const formatPrice = (value) => {
  const billion = Math.floor(value / 10000); // 억
  const million = value % 10000; // 나머지 만원

  if (billion > 0 && million > 0) {
    return `${billion}억 ${million.toLocaleString()}만원`;
  } else if (billion > 0) {
    return `${billion}억`;
  } else {
    return `${million.toLocaleString()}만원`;
  }
};

const submitForm = async () => {
  if (
    !file.value ||
    !selectedAddress.value ||
    !detail.value ||
    !jeonsePrice.value ||
    !registryName.value
  ) {
    alert('모든 항목을 입력해주세요.');
    return;
  }

  if (jeonsePrice.value < 1000) {
    alert('보증금은 최소 1,000만원 이상 입력해주세요.');
    return;
  }

  const formData = new FormData();
  formData.append('userId', auth.userId);
  formData.append('file', file.value);
  formData.append('address', selectedAddress.value);
  formData.append('detail', detail.value);
  formData.append('jeonsePrice', jeonsePrice.value);
  formData.append('registryName', registryName.value);

  try {
    // 1단계: 등기부등본 분석
    const response = await fetch('http://localhost:8080/api/safety-check', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${auth.token}`,
      },
      body: formData,
    });

    if (!response.ok) {
      const err = await response.text();
      throw new Error(err);
    }

    const data = await response.json();
    registerIdRef.value = data.registerId;
    const area = data.area;
    if (!area) {
      showManualInput.value = true; // 직접 입력 폼 보이기
      return; // 다음 단계 진행 중단
    } else {
      areaValue.value = area;
      showManualInput.value = false;
      // console.log('받은 area:', areaValue.value);

      await proceedToLeaseAnalysis(registerIdRef.value);
    }
  } catch (e) {
    alert('분석 실패: ' + e.message);
  }
};
// 2단계: 전세가율 분석
const proceedToLeaseAnalysis = async (registerId) => {
  try {
    const leaseRes = await fetch('/api/diagnosis/leasePer', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${auth.token}`,
      },
      body: JSON.stringify({
        registerId: registerId,
        excluUseAr: areaValue.value,
        address: selectedAddress.value,
        jeonsePrice: jeonsePrice.value,
        userId: auth.userId,
      }),
    });

    if (!leaseRes.ok) {
      throw new Error(await leaseRes.text());
    }

    // 3단계: 모든 분석 완료 후 결과 페이지로 이동
    router.push(`/safety-check/${registerId}`);
  } catch (e) {
    alert('전세가율 분석 실패: ' + e.message);
  }
};

// 직접 면적 입력 후 저장 및 다음 단계 진행
const saveManualArea = async () => {
  const raw = String(manualArea.value ?? '').trim();
  // 쉼표 소수점을 점으로 변환 (예: 56,88 -> 56.88)
  const num = Number(raw.replace(',', '.'));
  if (!Number.isFinite(num) || num <= 0) {
    alert('면적은 숫자만 입력 가능하며, 0보다 커야 합니다. (예: 56.88)');
    return;
  }

  // 중복 클릭 방지
  if (isSubmitting.value) return;
  isSubmitting.value = true;

  try {
    areaValue.value = Number(num.toFixed(4));
    showManualInput.value = false;
    // console.log('받은 area:', areaValue.value);
    await proceedToLeaseAnalysis(registerIdRef.value);
  } catch (e) {
    alert('분석 실패: ' + e.message);
  } finally {
    isSubmitting.value = false;
  }
};

onMounted(() => {
  const script = document.createElement('script');
  script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
  document.body.appendChild(script);
});
</script>

<style scoped>
.wrapper {
  max-width: 520px;
  margin: 0 auto;
  font-family: sans-serif;
  padding: 30px 20px;
  text-align: left;
}
.headline {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  line-height: 1.4;
  margin-bottom: 8px;
}
.sub {
  text-align: center;
  color: #424242;
  margin-bottom: 30px;
  font-weight: bold;
}
.blue {
  color: #1a80e5;
}
.input-group {
  margin-bottom: 25px;
}
label {
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
}
.input-box::placeholder {
  color: #a0aab8;
}
.input-box:focus {
  outline: none;
  border-color: #1a80e5;
  background-color: #fff;
}
.address-box {
  display: flex;
  gap: 10px;
  width: 100%;
}
.address-box input {
  flex: 1;
}
.tip {
  font-size: 12px;
  color: #888;
  margin-bottom: 8px;
  white-space: nowrap;
}
.input-box {
  width: 100%;
  padding: 18px 20px;
  border: 1px solid #d1dbe8;
  background-color: #f9fbfd;
  font-size: 15px;
  color: #333;
  border-radius: 12px !important; /* !important로 강제 적용 */
  transition: border-color 0.2s ease;
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: textfield;
  box-sizing: border-box;
  outline: none;
}
.upload-box {
  border: 2px dashed #ccc;
  padding: 20px;
  text-align: center;
  border-radius: 12px !important; /* !important로 강제 적용 */
  cursor: pointer;
  color: #888;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-sizing: border-box;
  width: 100%;
}
.upload-box span {
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: block;
}
.upload-box.dragover {
  border-color: #1a80e5;
  background: #f0f6ff;
  color: #1a80e5;
}
.address-box button {
  flex-shrink: 0;
  padding: 10px 14px;
  background-color: #2a7be4;
  color: white;
  border: none;
  border-radius: 12px !important; /* !important로 강제 적용 */
  cursor: pointer;
  box-sizing: border-box;
  outline: none;
}
.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  background-color: #1a80e5;
  color: white;
  border: none;
  border-radius: 12px !important; /* !important로 강제 적용 */
  cursor: pointer;
  margin-top: 20px;
  box-sizing: border-box;
  outline: none;
}
.submit-btn.disabled {
  pointer-events: none;
  opacity: 0.6;
}

/* 면적이 읽히지 않는 경우 모달창 */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-box {
  background: white;
  padding: 30px 25px;
  border-radius: 12px;
  width: 400px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  text-align: center;
}

.modal-message {
  font-size: 16px;
  margin-bottom: 20px;
  color: #333;
  line-height: 1.4;
}

.modal-input {
  width: 100%;
  padding: 12px 14px;
  font-size: 15px;
  border: 1.5px solid #ccc;
  border-radius: 6px;
  margin-bottom: 25px;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.modal-input:focus {
  outline: none;
  border-color: #3b82f6; /* 파란색 포커스 */
  box-shadow: 0 0 6px rgba(59, 130, 246, 0.4);
}

.modal-buttons {
  display: flex;
  justify-content: center;
  gap: 18px;
}

.modal-btn {
  padding: 10px 28px;
  font-size: 15px;
  border-radius: 8px;
  cursor: pointer;
  border: none;
  transition: background-color 0.3s ease;
  min-width: 100px;
}

.modal-btn.cancel {
  background-color: #f3f4f6; /* 연한 회색 */
  color: #555;
}

.modal-btn.cancel:hover {
  background-color: #e5e7eb;
}

.modal-btn.save {
  background-color: #2563eb; /* 진한 파란색 */
  color: white;
  font-weight: 600;
}

.modal-btn.save:hover {
  background-color: #1e40af;
}

.unit-input {
  display: flex;
  align-items: center;
}

.unit-input .modal-input {
  flex: 1;
}

.unit-input .unit {
  margin-left: 6px;
  font-size: 14px;
  color: #555;
}
</style>
