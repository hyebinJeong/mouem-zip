<script setup>
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const contract = ref({
  contractName: '',
  lessorName: '',
  lesseeName: '',
  address: '',
  landCategory: '',
  landArea: '',
  buildingUsage: '',
  buildingArea: '',
  leasedPart: '',
  leasedArea: '',
  deposit: '',
  downPayment: '',
  balance: '',
  maintenanceCost: '',
  leaseStart: '',
  leaseEnd: '',
  specialClauses: [],
});

const mergedSpecialTerms = ref([]);
const isLoadingPDF = ref(false);

function formatCurrency(value) {
  if (!value && value !== 0) return '';
  return Number(value).toLocaleString() + '원';
}
function formatArea(value) {
  if (!value && value !== 0) return '';
  return `${value}㎡`;
}
function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  if (isNaN(date)) return value;
  return `${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`;
}

onMounted(async () => {
  try {
    const contractId = route.params.id || route.query.id;
    if (!contractId || !auth.isLoggedIn) return;

    const res = await axios.get(`/api/contract/${contractId}`, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });

    const data = res.data;
    contract.value = { ...contract.value, ...data };
    mergedSpecialTerms.value = [...(data.specialClauses || [])];
  } catch (error) {
    console.error('계약서 조회 실패:', error.response?.status, error.response?.data);
  }
});

async function downloadPDF() {
  const pdfArea = document.getElementById('pdf-area');
  if (!pdfArea) return;

  isLoadingPDF.value = true;
  const excludes = document.querySelectorAll('.exclude-pdf');
  excludes.forEach((el) => (el.style.visibility = 'hidden'));

  try {
    const canvas = await html2canvas(pdfArea, { scale: 2 });
    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF('p', 'mm', 'a4');

    const pageWidth = pdf.internal.pageSize.getWidth();
    const pageHeight = pdf.internal.pageSize.getHeight();
    const imgWidth = pageWidth;
    const imgHeight = (canvas.height * imgWidth) / canvas.width;

    let heightLeft = imgHeight;
    let position = 0;

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
    heightLeft -= pageHeight;

    while (heightLeft > 0) {
      position -= pageHeight;
      pdf.addPage();
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight);
      heightLeft -= pageHeight;
    }

    pdf.save(`${contract.value.contractName || '계약서'}.pdf`);
  } catch (e) {
    console.error('PDF 생성 오류:', e);
  } finally {
    excludes.forEach((el) => (el.style.visibility = 'visible'));
    isLoadingPDF.value = false;
  }
}
</script>

<template>
  <div class="page-wrapper">
    <div v-if="isLoadingPDF" class="loading-overlay">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>

    <div class="contract-document" id="pdf-area">
      <h1 class="document-title">부동산 임대차 계약서</h1>


      <!-- 서두 + 부동산의 표시 -->
      <table class="document-table">
        <tr>
          <td colspan="6" class="section-intro">
            임대인({{ contract.lessorName }})과 임차인({{ contract.lesseeName }})은 아래 표시 부동산에 관하여 다음 계약 내용과 같이 임대차계약을 체결한다.<br><br>
            1. 부동산의 표시
          </td>
        </tr>
        <tr>
          <td class="table-label">소재지</td>
          <td colspan="5">{{ contract.address }}</td>
        </tr>
        <tr>
          <td class="table-label">토지</td>
          <td class="table-label">지목</td>
          <td>{{ contract.landCategory }}</td>
          <td class="table-label">면적</td>
          <td colspan="2">{{ formatArea(contract.landArea) }}</td>
        </tr>
        <tr>
          <td class="table-label">건물</td>
          <td class="table-label">구조·용도</td>
          <td>{{ contract.buildingUsage }}</td>
          <td class="table-label">면적</td>
          <td colspan="2">{{ formatArea(contract.buildingArea) }}</td>
        </tr>
        <tr>
          <td class="table-label">임차할 부분</td>
          <td colspan="5">{{ contract.leasedPart }} ({{ formatArea(contract.leasedArea) }})</td>
        </tr>
      </table>

      <!-- 계약 내용 + 제1조 목적 -->
      <table class="document-table">
        <tr>
          <td colspan="6" class="section-intro">
            2. 계약 내용<br>
            제1조 (목적) 위 부동산의 임대차에 한하여 임대인과 임차인은 합의에 의하여 임차보증금 및 차임을 아래와 같이 지불하기로 한다.
          </td>
        </tr>
        <tr>
          <td class="table-label">보증금</td>
          <td>{{ formatCurrency(contract.deposit) }}</td>
          <td class="table-label">계약금</td>
          <td>{{ formatCurrency(contract.downPayment) }}</td>
          <td class="table-label">잔금</td>
          <td>{{ formatCurrency(contract.balance) }}</td>
        </tr>
        <tr>
          <td class="table-label">관리비</td>
          <td>{{ formatCurrency(contract.maintenanceCost) }}</td>
          <td class="table-label">임대차 기간</td>
          <td colspan="3">{{ formatDate(contract.leaseStart) }} ~ {{ formatDate(contract.leaseEnd) }}</td>
        </tr>
      </table>

      <!-- 제2조~제7조 -->
      <table class="document-table">
        <tr>
          <td colspan="6" class="clause-cell">
            제2조 (존속기간) 임대인은 위 부동산을 임대차 목적대로 사용·수익할 수 있는 상태로 {{ formatDate(contract.leaseStart) }} 까지 임차인에게 인도하며, 임대차 기간은 인도일로부터 {{ formatDate(contract.leaseEnd) }} 까지로 한다.<br><br>
            제3조 (용도변경 및 전대 등) 임차인은 임대인의 동의 없이 위 부동산의 용도나 구조를 변경하거나 전대·임차권 양도 또는 담보제공을 하지 못하며 임대차 목적 이외의 용도로 사용할 수 없다.<br><br>
            제4조 (계약의 해지) 임차인의 차임연체액이 2기의 차임액에 달하거나 제3조를 위반하였을 때 임대인은 즉시 본 계약을 해지할 수 있다.<br><br>
            제5조 (계약의 종료) 임대차계약이 종료된 경우에 임차인은 위 부동산을 원상으로 회복하여 임대인에게 반환한다. 이러한 경우 임대인은 보증금을 임차인에게 반환하고, 연체 임대료 또는 손해배상금이 있을 때는 이를 제하고 그 잔액을 반환한다.<br><br>
            제6조 (계약의 해제) 임차인이 임대인에게 중도금(중도금이 없을 때는 잔금)을 지불하기 전까지, 임대인은 계약금의 배액을 상환하고, 임차인은 계약금을 포기하고 이 계약을 해제할 수 있다.<br><br>
            제7조 (채무불이행과 손해배상) 임대인 또는 임차인이 본 계약상의 내용에 대하여 불이행이 있을 경우 그 상대방은 불이행한 자에 대하여 서면으로 최고하고 계약을 해제할 수 있다. 그리고 계약 당사자는 계약해제에 따른 손해배상을 각각 상대방에 대하여 청구할 수 있다.
          </td>
        </tr>
      </table>

      <!-- 특약사항 -->
      <table class="document-table">
        <tr class="table-header-row"><td colspan="6">[ 특약 사항 ]</td></tr>
        <tr v-if="mergedSpecialTerms.length">
          <td colspan="6">
            <ul class="special-list">
              <li v-for="(clause, idx) in mergedSpecialTerms" :key="idx">{{ idx + 1 }}. {{ clause }}</li>
            </ul>
          </td>
        </tr>
        <tr v-else>
          <td colspan="6" class="no-special">등록된 특약이 없습니다.</td>
        </tr>
      </table>

      <!-- 본 계약 증명 -->
      <table class="document-table">
        <tr>
          <td colspan="6" class="proof-text">
            본 계약을 증명하기 위하여 계약 당사자가 이의 없음을 확인하고 각각 서명·날인 후 임대인, 임차인 매 장마다 간인하여야 하며, 각각 1통씩 보관한다.
          </td>
        </tr>
      </table>

      <!-- 서명란 -->
      <table class="sign-table">
        <tr>
          <td class="sign-name">임대인 성명:</td>
          <td class="sign-value">{{ contract.lessorName }}</td>
          <td class="sign-seal">(서명)</td>
          <td class="sign-name">임차인 성명:</td>
          <td class="sign-value">{{ contract.lesseeName }}</td>
          <td class="sign-seal">(서명)</td>
        </tr>
      </table>
    </div>

    <div class="button-area exclude-pdf">
      <button v-if="route.query.from === 'myPage'" class="btn btn-primary" @click="downloadPDF" :disabled="isLoadingPDF">
        {{ isLoadingPDF ? 'PDF 생성 중...' : '다운로드' }}
      </button>
      <button class="btn btn-secondary" @click="router.push({ name: 'home' })">홈으로 돌아가기</button>
    </div>
  </div>
</template>

<style scoped>
.page-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 80px;
  background-color: #f7f9fc;
}
.contract-document {
  background: #fff;
  border: 2px solid #000;
  padding: 40px;
  max-width: 800px;
  width: 100%;
  box-sizing: border-box;
}
.document-title {
  font-size: 32px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.document-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}
.document-table td {
  border: 1px solid #000;
  padding: 8px;
  font-size: 14px;
  vertical-align: middle;
}
.table-header-row td {
  background: #e9ecef;
  font-weight: bold;
  text-align: center;
}
.table-label {
  background: #f7f9fc;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
}
.section-intro {
  text-align: left;
  line-height: 1.6;
  font-weight: bold;
  font-size: 14px;
}
.clause-cell {
  line-height: 1.6;
}
.special-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.no-special {
  text-align: center;
  padding: 10px;
}
.proof-text {
  text-align: center;
  font-size: 14px;
  line-height: 1.5;
}
.sign-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 20px;
}
.sign-name {
  border: 1px solid #000;
  padding: 12px;
  font-size: 14px;
  text-align: left;
  width: 120px;
  white-space: nowrap;
}
.sign-value {
  border: 1px solid #000;
  padding: 12px;
  font-size: 14px;
  text-align: center;
  width:150px;
}
.sign-seal {
  border: 1px solid #000;
  padding: 12px;
  font-size: 14px;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
}
.button-area {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}
</style>

