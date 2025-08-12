<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import axios from 'axios';

const router = useRouter();
const auth = useAuthStore();

// 폼 데이터
const contractName = ref('');
const lessor = ref('');
const lessee = ref('');
const address = ref('');
const contractAmount = ref('');
const deposit = ref('');
const rent = ref('');
const structure = ref('');
const maintenanceFee = ref('');
const startDate = ref('');
const endDate = ref('');
const special = ref(['']);
const landCategory = ref('');
const landArea = ref('');
const buildingArea = ref('');
const leasePart = ref('');
const leaseArea = ref('');

// 숫자를 억/만원/원 단위로 변환해주는 함수 (버그 수정)
const formatPrice = (value) => {
  if (value === null || value === undefined || String(value).trim() === '') return '0원';
  const s = String(value).replace(/,/g, '').trim();
  if (s === '') return '0원';
  const num = Number(s);
  if (Number.isNaN(num) || num === 0) return '0원';

  const won = Math.abs(Math.floor(num));
  const billion = Math.floor(won / 100000000); // 억 단위
  const million = Math.floor((won % 100000000) / 10000); // 만단위
  const rest = won % 10000; // 나머지 원

  const parts = [];
  if (billion > 0) parts.push(`${billion}억`);
  if (million > 0) parts.push(`${million}만`);
  if (rest > 0) parts.push(`${rest.toLocaleString()}원`);
  return parts.join(' ') || '0원';
};

// ✅ 모달 상태
const showLandModal = ref(false);
const showBuildingModal = ref(false);

// ✅ 토지 지목 리스트
const landCategories = [
  '전',
  '답',
  '과수원',
  '목장용지',
  '임야',
  '광천지',
  '염전',
  '대',
  '공장용지',
  '학교용지',
  '주차장',
  '주유소용지',
  '창고용지',
  '도로',
  '철도용지',
  '제방',
  '하천',
  '구거',
  '유지',
  '양어장',
  '수도용지',
  '공원',
  '체육용지',
  '유원지',
  '종교용지',
  '사적지',
  '묘지',
  '잡종지',
];

// ✅ 건물 용도 리스트
const buildingUsages = [
  '단독주택',
  '공동주택',
  '제1종 근린생활시설',
  '제2종 근린생활시설',
  '문화 및 집회시설',
  '종교시설',
  '판매시설',
  '운수시설',
  '의료시설',
  '교육연구시설',
  '노유자시설',
  '수련시설',
  '운동시설',
  '업무시설',
  '숙박시설',
  '위락시설',
  '관광휴게시설',
  '제1종 산업시설',
  '제2종 산업시설',
  '창고시설',
  '위험물저장및처리시설',
  '자원순환관련시설',
  '교정 및 군사시설',
  '방송통신시설',
  '발전시설',
  '묘지관련시설',
  '공원시설',
];

// ✅ 모달 동작
function selectLandCategory(item) {
  landCategory.value = item;
  showLandModal.value = false;
}
function selectBuildingUsage(item) {
  structure.value = item;
  showBuildingModal.value = false;
}

// 세션스토리지 저장/복원
function saveContractToSession() {
  const contractData = {
    contractName: contractName.value,
    lessor: lessor.value,
    lessee: lessee.value,
    address: address.value,
    contractAmount: contractAmount.value,
    deposit: deposit.value,
    rent: rent.value,
    structure: structure.value,
    maintenanceFee: maintenanceFee.value,
    startDate: startDate.value,
    endDate: endDate.value,
    special: special.value,
    landCategory: landCategory.value,
    landArea: landArea.value,
    buildingArea: buildingArea.value,
    leasePart: leasePart.value,
    leaseArea: leaseArea.value,
  };
  sessionStorage.setItem('contractData', JSON.stringify(contractData));
}

// 새로고침 감지
const isHardReload = () => {
  const navType = performance.getEntriesByType('navigation')[0]?.type;
  return navType === 'reload' || navType === 'navigate';
};

// 입력 제어: 정수 또는 소수 허용(allowDecimal)
const allowOnlyNumbers = (event, modelRef, allowDecimal = false) => {
  // 원본값(붙여넣기 포함)
  let value = String(event.target.value || '').replace(/,/g, '').trim();

  if (allowDecimal) {
    // 숫자와 점(.)만 허용, 점은 하나만
    value = value.replace(/[^0-9.]/g, '');
    const firstDot = value.indexOf('.');
    if (firstDot !== -1) {
      // 첫 번째 점 이후 모든 점 제거
      value = value.slice(0, firstDot + 1) + value.slice(firstDot + 1).replace(/\./g, '');
      // '.'로 시작하면 앞에 0 붙이기
      if (value.startsWith('.')) value = '0' + value;
    }
  } else {
    // 정수만 허용
    value = value.replace(/\D/g, '');
  }

  modelRef.value = value;
  // 입력 표시 동기화
  event.target.value = value;
};

const allowOnlyText = (event, modelRef) => {
  const value = event.target.value;
  if (/^[ㄱ-ㅎ가-힣a-zA-Z\s]*$/.test(value)) {
    modelRef.value = value;
  } else {
    const sanitized = value.replace(/[^ㄱ-ㅎ가-힣a-zA-Z\s]/g, '');
    modelRef.value = sanitized;
    event.target.value = sanitized;
  }
};

onMounted(() => {
  const fromSpecialPage = sessionStorage.getItem('fromSpecialPage') === 'true';
  const contractData = sessionStorage.getItem('contractData');
  const script = document.createElement('script');
  script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
  document.body.appendChild(script);

  if (isHardReload() && !fromSpecialPage) {
    // 초기화
    contractName.value = '';
    lessor.value = '';
    lessee.value = '';
    address.value = '';
    contractAmount.value = '';
    deposit.value = '';
    rent.value = '';
    structure.value = '';
    maintenanceFee.value = '';
    startDate.value = '';
    endDate.value = '';
    special.value = [''];
    landCategory.value = '';
    landArea.value = '';
    buildingArea.value = '';
    leasePart.value = '';
    leaseArea.value = '';
    sessionStorage.removeItem('contractData');
    sessionStorage.removeItem('selectedClauses');
  } else if (contractData) {
    const data = JSON.parse(contractData);
    contractName.value = data.contractName || '';
    lessor.value = data.lessor || '';
    lessee.value = data.lessee || '';
    address.value = data.address || '';
    contractAmount.value = data.contractAmount || '';
    deposit.value = data.deposit || '';
    rent.value = data.rent || '';
    structure.value = data.structure || '';
    maintenanceFee.value = data.maintenanceFee || '';
    startDate.value = data.startDate || '';
    endDate.value = data.endDate || '';
    special.value =
      Array.isArray(data.special) && data.special.length > 0
        ? data.special
        : [''];
    landCategory.value = data.landCategory || '';
    landArea.value = data.landArea || '';
    buildingArea.value = data.buildingArea || '';
    leasePart.value = data.leasePart || '';
    leaseArea.value = data.leaseArea || '';
  }

  // 특약 선택 합치기
  const selected = JSON.parse(
    sessionStorage.getItem('selectedClauses') || '[]'
  );
  const newClauses = selected.map((clause) => clause.text).filter(Boolean);
  newClauses.forEach((clause) => {
    if (!special.value.includes(clause)) {
      special.value.unshift(clause);
    }
  });

  sessionStorage.removeItem('fromSpecialPage');
});

// 입력 감시해서 세션에 저장
watch(
  [
    contractName,
    lessor,
    lessee,
    address,
    contractAmount,
    deposit,
    rent,
    structure,
    maintenanceFee,
    startDate,
    endDate,
    special,
    landCategory,
    landArea,
    buildingArea,
    leasePart,
    leaseArea,
  ],
  saveContractToSession,
  { deep: true }
);

// 특약 입력 제어
const addSpecialTerm = () => special.value.push('');
const removeSpecialTerm = (index) => {
  if (special.value.length > 1) special.value.splice(index, 1);
  else special.value = [''];
};

// 숫자 변환 헬퍼 (안전)
const toInt = (v) => {
  if (v === null || v === undefined) return 0;
  const s = String(v).replace(/,/g, '').trim();
  if (s === '') return 0;
  const n = parseInt(s, 10);
  return Number.isNaN(n) ? 0 : n;
};
const toFloat = (v) => {
  if (v === null || v === undefined) return 0;
  const s = String(v).replace(/,/g, '').trim();
  if (s === '') return 0;
  const n = parseFloat(s);
  return Number.isNaN(n) ? 0 : n;
};

// ✅ 제출 로직
const onSubmit = async () => {
  // 필수 입력 검증
  const requiredFieldsMap = {
    '계약서 이름': contractName.value,
    '임대인': lessor.value,
    '임차인': lessee.value,
    '소재지': address.value,
    '계약금': contractAmount.value,
    '보증금': deposit.value,
    '잔금': rent.value,
    '건물 구조·용도': structure.value,
    '관리비': maintenanceFee.value,
    '임대 시작일': startDate.value,
    '임대 종료일': endDate.value,
    '토지 지목': landCategory.value,
    '토지 면적': landArea.value,
    '건물 면적': buildingArea.value,
    '임차할 부분': leasePart.value,
    '임차할 면적': leaseArea.value,
  };

  const missing = Object.keys(requiredFieldsMap).find(
    (k) => requiredFieldsMap[k] === null || requiredFieldsMap[k] === undefined || String(requiredFieldsMap[k]).trim() === ''
  );
  if (missing) {
    alert(`${missing}을(를) 입력해주세요.`);
    return;
  }

  // 특약 최소 1개
  const validSpecials = special.value.filter(
    (term) => term && term.trim() !== ''
  );
  if (validSpecials.length === 0) {
    alert('특약사항을 최소 1개 이상 입력하거나 선택해주세요.');
    return;
  }
  
  // 보증금 = 계약금 + 잔금 (정수 비교)
  const depositNum = toInt(deposit.value);
  const downPaymentNum = toInt(contractAmount.value); // 계약금
  const balanceNum = toInt(rent.value); // 잔금
  if (depositNum !== downPaymentNum + balanceNum) {
    alert('보증금은 계약금과 잔금의 합과 같아야 합니다.');
    return;
  }

  // 면적 비교 (소수점 허용)
  const landAreaNum = toFloat(landArea.value);
  const buildingAreaNum = toFloat(buildingArea.value);
  const leaseAreaNum = toFloat(leaseArea.value);

  if (!(landAreaNum >= buildingAreaNum && buildingAreaNum >= leaseAreaNum)) {
    alert('토지면적 ≥ 건물면적 ≥ 임대면적 이어야 합니다.');
    return;
  }

  try {
    // ✅ 백엔드 DTO와 맞춘 payload
    const payload = {
      contractName: contractName.value,
      lessorName: lessor.value,
      lesseeName: lessee.value,
      address: address.value,
      landCategory: landCategory.value,
      landArea: landAreaNum,
      buildingUsage: structure.value,
      buildingArea: buildingAreaNum,
      leasedPart: leasePart.value,
      leasedArea: leaseAreaNum,
      deposit: depositNum,
      downPayment: downPaymentNum,
      balance: balanceNum,
      maintenanceCost: toInt(maintenanceFee.value),
      leaseStart: startDate.value,
      leaseEnd: endDate.value,
      specialClauses: validSpecials, // ✅ JSON 배열
    };

    const res = await axios.post('/api/contract', payload, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });

    alert('계약서가 저장되었습니다.');
    const contractId = res.data; // 백엔드에서 생성된 contractId 반환
    router.push({
      name: 'ReferenceContractSuccess',
      query: { id: contractId, from: 'list' },
    });
  } catch (err) {
    console.error('계약서 저장 중 오류 발생:', err);
    alert('저장에 실패했습니다.');
  }

  saveContractToSession();
};

// 특약 예시 페이지 이동
const goToSpecialPage = () => {
  sessionStorage.setItem('fromSpecialPage', 'true');
  router.push({ name: 'SpecialContractsRecommendation' });
};

const openPostcode = () => {
  new window.daum.Postcode({
    oncomplete: function (data) {
      const userType = data.userSelectedType;
      if (userType === 'R' && data.roadAddress) {
        address.value = data.roadAddress;
      } else if (userType === 'J' && data.jibunAddress) {
        address.value = data.jibunAddress;
      } else {
        address.value = '';
      }
    },
  }).open();
};
</script>

<template>
  <div class="wrapper">
    <div class="contract-box">
      <h1 class="title">계약서 작성을 위해 필요한 정보를 입력해주세요.</h1>
      <form class="form-grid" @submit.prevent="onSubmit">
        <div class="form-row full">
          <label>계약서 이름</label>
          <input
            v-model="contractName"
            type="text"
            placeholder="계약서 이름을 작성해주세요."
            class="long-input"
          />
        </div>

        <!-- 임대인 + 임차인 -->
        <div class="form-row">
          <div class="half-col horizontal">
            <label>임대인(집주인)</label>
            <input
              v-model="lessor"
              type="text"
              placeholder="성명"
              @input="allowOnlyText($event, lessor)"
            />
          </div>
          <div class="half-col horizontal">
            <label>임차인(세입자)</label>
            <input
              v-model="lessee"
              type="text"
              placeholder="성명"
              @input="allowOnlyText($event, lessee)"
            />
          </div>
        </div>

        <!-- 소재지 -->
        <div class="form-row full">
          <label>소재지</label>
          <div class="input-with-button icon-style">
            <input
              v-model="address"
              type="text"
              placeholder="도로명 주소를 입력해주세요."
              class="long-input"
              readonly
            />
            <button type="button" class="address-btn" @click="openPostcode">
              주소검색
            </button>
          </div>
        </div>

        <!-- 토지 지목 + 토지 면적 -->
        <div class="form-row">
          <div class="half-col horizontal">
            <label>토지 지목</label>
            <input v-model="landCategory" type="text" placeholder="대" />
            <button
              type="button"
              class="icon-btn sel-btn"
              @click="showLandModal = true"
            >
              <i class="bi-geo-alt"></i>
            </button>
          </div>
          <div class="half-col horizontal">
            <label>토지 면적</label>
            <input
              v-model="landArea"
              type="text"
              placeholder="m²"
              @input="allowOnlyNumbers($event, landArea, true)"
            />
          </div>
        </div>

        <!-- 건물 구조·용도 + 건물 면적 -->
        <div class="form-row">
          <div class="half-col horizontal">
            <label>건물 구조·용도</label>
            <input v-model="structure" type="text" placeholder="단독 주택" />
            <button
              type="button"
              class="icon-btn sel-btn"
              @click="showBuildingModal = true"
            >
              <i class="bi-house"></i>
            </button>
          </div>
          <div class="half-col horizontal">
            <label>건물 면적</label>
            <input
              v-model="buildingArea"
              type="text"
              placeholder="m²"
              @input="allowOnlyNumbers($event, buildingArea, true)"
            />
          </div>
        </div>

        <!-- 임차할 부분 + 임차할 면적 -->
        <div class="form-row">
          <div class="half-col horizontal">
            <label>임차할 부분</label>
            <input v-model="leasePart" type="text" placeholder="동·호수" />
          </div>
          <div class="half-col horizontal">
            <label>임차할 면적</label>
            <input
              v-model="leaseArea"
              type="text"
              placeholder="m²"
              @input="allowOnlyNumbers($event, leaseArea, true)"
            />
          </div>
        </div>

        <!-- 보증금 + 계약금 -->
        <div class="form-row">
          <div class="half-col horizontal align-top">
            <label>보증금</label>
            <div class="input-with-tip">
              <input
                  v-model="deposit"
                  type="text"
                  placeholder="원"
                  @input="allowOnlyNumbers($event, deposit)"
              />
              <p class="m-tip" v-show="deposit">
                입력하신 금액: <strong>{{ formatPrice(deposit) }}</strong>
              </p>
            </div>
          </div>

          <div class="half-col horizontal align-top">
            <label>계약금</label>
            <div class="input-with-tip">
              <input
                  v-model="contractAmount"
                  type="text"
                  placeholder="원"
                  @input="allowOnlyNumbers($event, contractAmount)"
              />
              <p class="m-tip" v-show="contractAmount">
                입력하신 금액:
                <strong>{{ formatPrice(contractAmount) }}</strong>
              </p>
            </div>
          </div>
        </div>

        <!-- 잔금 + 관리비 -->
        <div class="form-row">
          <div class="half-col horizontal align-top">
            <label>잔금</label>
            <div class="input-with-tip">
              <input
                  v-model="rent"
                  type="text"
                  placeholder="원"
                  @input="allowOnlyNumbers($event, rent)"
              />
              <p class="m-tip" v-show="rent">
                입력하신 금액:
                <strong>{{ formatPrice(rent) }}</strong>
              </p>
            </div>
          </div>

          <div class="half-col horizontal align-top">
            <label>관리비</label>
            <div class="input-with-tip">
              <input
                  v-model="maintenanceFee"
                  type="text"
                  placeholder="원"
                  @input="allowOnlyNumbers($event, maintenanceFee)"
              />
              <p class="m-tip" v-show="maintenanceFee">
                입력하신 금액:
                <strong>{{ formatPrice(maintenanceFee) }}</strong>
              </p>
            </div>
          </div>
        </div>

        <div class="form-row period-vertical">
          <label>임대차 기간</label>
          <div class="period-column">
            <div class="period-line">
              <input v-model="startDate" type="date" />
              <span>부터</span>
            </div>
            <div class="period-line">
              <input v-model="endDate" type="date" />
              <span>까지</span>
            </div>
          </div>
        </div>

        <div class="form-row full special-terms">
          <label>특약 사항</label>
          <div class="special-input-wrapper">
            <div class="special-list">
              <div
                class="special-input"
                v-for="(term, index) in special"
                :key="index"
              >
                <textarea
                  v-model="special[index]"
                  placeholder="특약 사항을 입력하세요."
                  rows="3"
                ></textarea>
                <div class="btn-group">
                  <button
                    v-if="index === special.length - 1"
                    type="button"
                    class="btn-small add"
                    @click="addSpecialTerm"
                  >
                    <i class="bi bi-plus-lg icon-white"></i>
                  </button>
                  <button
                    type="button"
                    class="btn-small remove"
                    @click="removeSpecialTerm(index)"
                  >
                    <i class="bi bi-dash-lg icon-white"></i>
                  </button>
                </div>
              </div>
            </div>
            <div class="side-controls">
              <button
                type="button"
                class="btn-template"
                @click="goToSpecialPage"
              >
                특약 예시에서 선택하기
              </button>
              <p class="tip">특약사항을 추가해드릴게요.</p>
            </div>
          </div>
        </div>

        <div class="button-group full">
          <button
            type="button"
            class="btn-back"
            @click="router.push({ name: 'home' })"
          >
            뒤로 가기
          </button>
          <button type="submit" class="btn-submit">작성 완료</button>
        </div>

        <!-- ✅ 토지 지목 모달 -->
        <div v-if="showLandModal" class="modal-overlay">
          <div class="modal-content">
            <h3>토지 지목 선택</h3>
            <ul class="list-style">
              <li
                  v-for="(item, i) in landCategories"
                  :key="i"
                  @click="selectLandCategory(item)"
              >
                {{ item }}
              </li>
            </ul>
            <button class="close-btn" @click="showLandModal = false">닫기</button>
          </div>
        </div>

        <!-- ✅ 건물 용도 모달 -->
        <div v-if="showBuildingModal" class="modal-overlay">
          <div class="modal-content">
            <h3>건물 구조·용도 선택</h3>
            <ul class="list-style">
              <li
                v-for="(item, i) in buildingUsages"
                :key="i"
                @click="selectBuildingUsage(item)"
              >
                {{ item }}
              </li>
            </ul>
            <button class="close-btn" @click="showBuildingModal = false">닫기</button>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.wrapper {
  display: flex;
  justify-content: center;
  padding: 60px;
  background-color: white;
}
.contract-box {
  background-color: #f7f9fc;
  border-radius: 16px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.15);
  padding: 70px;
  width: 100%;
  max-width: 1100px;
}
.title {
  font-size: 27px;
  font-weight: bold;
  margin-top: -25px;
  margin-bottom: 55px;
}
.form-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 16px;
  margin-bottom: 20px;
}
.form-row.full {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 0px;
}

.form-row.full label {
  width: 120px;
  flex-shrink: 0;
  margin-bottom: 0;
}

.form-row.full .long-input {
  flex: 1;
}

.half-col.horizontal {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
}

.half-col.horizontal label {
  font-weight: 700;
  width: 120px;
  margin-bottom: 0px;
}


label {
  width: 105px;
  font-weight: 700;
  text-align: left;
  flex-shrink: 0;
  font-size: 16px;
}

input[type='text'],
input[type='date'] {
  flex-grow: 1;
  min-width: 0;
  width: 100%;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 14px;
}

.long-input[type='text'] {
  width: 100%;
}
.period-vertical {
  display: flex;
  align-items: center;
}

.period-column {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.period-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.period-line input[type='date'] {
  width: 275px;
  height: 44px;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 8px;
}

.period-line span {
  font-weight: 600;
  font-size: 15px;
}

.date-line input[type='date'] {
  flex-grow: 1;
  height: 44px;
}

.date-line span {
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
}
.special-terms .special-input-wrapper {
  display: flex;
  gap: 16px;
  flex-grow: 1;
  align-items: flex-start;
}
.special-list {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.special-input {
  display: flex;
  align-items: center;
  gap: 8px;
}
.special-input textarea {
  width: 100%;
  flex-grow: 1;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ccc;
  font-size: 14px;
  resize: vertical;
}
.btn-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.btn-small {
  padding: 6px 12px;
  border-radius: 6px;
  border: none;
  background-color: #ddd;
  cursor: pointer;
  white-space: nowrap;
}
.btn-small.add {
  background-color: #1a80e5;
}
.btn-small.remove {
  background-color: #fe5252;
}

.icon-white {
  color: #fff;
  font-size: 20px;
  text-shadow: 0 0 1px #fff, 0 0 1px #fff;
}

.sel-btn{
  min-width: 45px;
  margin-left: 10px;
}

.sel-btn:hover{
  background-color: rgb(33, 112, 193);
  transform: scale(1.02);
  transition: all 0.1s ease-in-out;
}

.side-controls {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding-top: 6px;
}
.btn-template {
  background-color: #1a80e5;
  color: #ffffff;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 40px;
}

.btn-back {
  background-color: #f0f0f0;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: bold;
  border: none;
  cursor: pointer;
}
.btn-submit {
  background-color: #1a80e5;
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: bold;
  border: none;
  cursor: pointer;
}

.btn-small.add:hover {
  background-color: rgb(33, 112, 193);
  transform: scale(1.05);
  transition: all 0.1s ease-in-out;
}

.btn-small.remove:hover {
  background-color: rgb(218, 81, 81);
  transform: scale(1.05);
  transition: all 0.1s ease-in-out;
}

.btn-template:hover {
  background-color: rgb(33, 112, 193);
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease-in-out;
}

.btn-submit:hover {
  background-color: rgb(33, 112, 193);
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
  transform: scale(1.02);
  transition: all 0.1s ease-in-out;
}

.btn-back:hover {
  background-color: #d6d6d6;
  transform: scale(1.02);
  transition: all 0.1s ease-in-out;
}

.list-style {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 300px;
  overflow-y: auto;
}

.list-style li {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background 0.2s ease, font-weight 0.2s ease;
  font-size: 16px;
}

.list-style li:hover {
  background: #f0f4ff;
  font-weight: bold;
}


/* 반응형: 화면이 768px 이하일 때 (태블릿·모바일) */
@media (max-width: 768px) {
  .form-row.full {
    flex-direction: column;
    align-items: flex-start;
  }

  .form-row.full label {
    width: auto;
    margin-bottom: 8px;
  }

  .form-row.full .long-input {
    width: 100%;
  }

  .form-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .half-col.horizontal {
    width: 100%;
    flex-direction: column;
    align-items: flex-start;
  }

  label {
    width: auto; /* 라벨 고정폭 제거 */
    margin-bottom: 8px;
  }

  .button-group {
    flex-direction: column;
    gap: 12px;
    width: 100%;
  }

  .btn-back,
  .btn-submit {
    width: 100%;
  }
}

/* 모바일(480px 이하) */
@media (max-width: 480px) {
  .contract-box {
    padding: 30px;
  }

  .title {
    font-size: 22px;
    margin-bottom: 30px;
  }

  input[type='text'],
  input[type='date'],
  textarea {
    font-size: 13px;
    padding: 10px;
  }
}
.input-with-button {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.input-with-button.icon-style input {
  flex: 1;
}

.input-with-button.icon-style .icon-btn {
  width: 44px;
  height: 44px;
  padding: 0;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.address-btn {
  background: #1a80e5;
  color: white;
  font-size: 15px;
  font-weight: 500;
  border: none;
  border-radius: 6px;
  padding: 0 15px;    /* 좌우 길게 */
  height: 44px;
  min-width: 100px;   /* 길이 확보 */
  cursor: pointer;
}

.address-btn:hover {
  background-color: rgb(33, 112, 193);
  transform: scale(1.02);
  transition: all 0.1s ease-in-out;
}

.icon-btn {
  background: #1a80e5;
  border: none;
  color: white;
  border-radius: 6px;
  padding: 8px;
  cursor: pointer;
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-content {
  background: #ffffff;
  padding: 30px;
  border-radius: 12px;
  width: 480px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.modal-content h3 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: #1a1a1a;
  text-align: center;
}

.modal-content input[type='text'] {
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  font-size: 14px;
}

.modal-content button {
  background-color: #1a80e5;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  font-size: 16px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.modal-content button:hover {
  background-color: rgb(33, 112, 193);
  transform: scale(1.01);
  transition: all 0.1s ease-in-out;
}

.modal-content ul {
  list-style: none;
  padding: 0;
  margin: 0;
  border: 1px solid #ddd;
  border-radius: 6px;
  max-height: 200px;
  overflow-y: auto;
}

.modal-content ul li {
  padding: 10px 14px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}

.modal-content ul li:last-child {
  border-bottom: none;
}

.modal-content ul li:hover {
  background: #f3f6fb;
}

.close-btn {
  background-color: #1a80e5;
  color: #ffffff;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  width: 100px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s ease;

  display: block;
  margin: 0 auto;
}
.close-btn:hover {
  background-color: #2563eb;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.tip {
  font-size: 12px;
  color: #888;
  margin-bottom: 8px;
  white-space: nowrap;
}
.input-with-tip {
  position: relative;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.m-tip {
  position: absolute;
  top: 100%;
  left: 0;
  font-size: 12px;
  color: #888;
  white-space: nowrap;
  margin-top: 4px; /* input 아래 간격 */
}
</style>
