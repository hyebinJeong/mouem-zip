# 🏡 HomeBuddy - 부동산 리스크 분석 통합 리포트 시스템

**HomeBuddy**는 부동산 거래 시 고려해야 할 등기부 위험요소, 전세가율, 체크리스트를 통합 분석한 최종 리포트를 제공하고,
참고 계약서 작성 기능과 부동산 용어집을 통해 사용자의 안전하고 이해도 높은 거래를 지원하는 웹 서비스입니다.

---

## 🗂️ 프로젝트 개요

- **주제**: 부동산 리스크 요소(등기부 위험, 전세가율, 체크리스트)를 통합 분석하고, 리포트·계약서·용어집 기능을 통해 안전한 부동산 거래를 지원하는 웹 서비스 개발
- **프로젝트 기간**: 2025.07.09 ~ 2025.08.21 (6주)
- **담당 기간**: 2025.07.09 ~ 2025.08.04
- **역할**: 정혜빈 – 최종 리포트 전체 기능 구현 (프론트 + 백엔드 + 데이터 흐름 설계)

---

## 👨‍👩‍👧‍👦 팀 구성

| 이름 | 담당 |
|------|------|
| 고두환 | 로그인, 마이페이지 |
| 정경조 | 등기부등본 분석, 결과 시각화 |
| 이승예 | 전세가율 분석, 결과 시각화 |
| 송용욱 | 관리자 페이지 |
| 문경서 | 메인페이지 헤더, 계약서 체크리스트 |
| 정혜빈 | 최종 진단 결과, 계약 가이드 |
| 윤준식 | 메인페이지 바디, 참고 계약서, 특약 관리 |
| 이가은 | 용어집 |

---

## 🛠️ 기술 스택

- **Frontend**: Vue.js, Vite, Axios, Chart.js, vue-chartjs, Bootstrap
- **Backend**: Spring Framework (Legacy), MyBatis, Gradle
- **Database**: MySQL
- **기타**: Swagger, GitHub, Notion, Slack

---

## 나의 역할

### frontend

```
src/
├── api/
│   └── finalReport.js                         # FinalReport API 통신 함수 정의
│
├── components/
│   └── final-report/
│       ├── charts/
│       │   ├── BarChart.vue                   # 바 차트 컴포넌트 (전세가율 등에서 사용)
│       │   └── DonutChart.vue                 # 도넛 차트 컴포넌트 (등급 시각화)
│       │
│       ├── DiagnosisGradeInfoModal.vue       # 등급 설명 모달
│       ├── FinalChecklist.vue                # 체크리스트 결과 뷰
│       ├── FinalGrade.vue                    # 전체 등급 표시 뷰
│       ├── FinalJeonse.vue                   # 전세가율 분석 카드 래퍼
│       └── FinalJeonseCard.vue               # 전세가율 분석 개별 카드
│
├── composables/
│   └── final-report/
│       ├── useGradeColor.js                  # 등급(안전/주의 등)별 색상 매핑 
│       └── useNavigation.js                  # 버튼 클릭 시 페이지 이동 로직 
│
├── pages/
│   ├── FinalReportPage.vue                   # 최종 리포트 메인 페이지
│   └── GuidebookPage.vue                     # 계약 외 가이드 페이지

```
### backend

```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── org/
│       │       └── scoula/
│       │           └── finalreport/
│       │               ├── controller/
│       │               │   └── FinalReportController.java          # REST API 요청 처리
│       │               │
│       │               ├── dto/
│       │               │   ├── FinalReportDTO.java                # 응답용 DTO
│       │               │   ├── FinalReportInsertDTO.java          # 등록용 DTO
│       │               │   └── FinalReportRawDTO.java             # 원시 데이터 (JOIN용)
│       │               │
│       │               ├── mapper/
│       │               │   └── FinalReportMapper.java             # MyBatis 매퍼 인터페이스
│       │               │
│       │               ├── scheduler/
│       │               │   └── FinalReportScheduler.java          # 스케줄러 (50일 후 status 변경)
│       │               │
│       │               └── service/
│       │                   ├── FinalReportService.java            # 서비스 인터페이스
│       │                   └── FinalReportServiceImpl.java        # 서비스 구현체
│       │
│       └── resources/
│           └── org/
│               └── scoula/
│                   └── finalreport/
│                       └── mapper/
│                           └── FinalReportMapper.xml             # SQL 쿼리 매핑 파일 (MyBatis)

```

---

### 1. 프론트엔드 구현 (Vue.js 기반)
- `FinalReportPage.vue` 구성: 탭 기반 UI로 등기부, 전세가율, 체크리스트, 등급 요약 렌더링
- `DonutChart`, `FinalChecklist`, `FinalGrade` 등 기능 단위 컴포넌트 개발
- Chart.js + vue-chartjs 조합으로 등급/비율 차트 시각화
- 반응형 UI 및 ARIA 속성 적용으로 접근성 향상
- router 연동 및 navigation composable 설계
- 기존 등기부 분석 컴포넌트에 분석 데이터를 전달하여 최종 리포트 화면과 통합

### 2. 백엔드 구현 (Spring Legacy + MyBatis)
- `/api/reports` 단일 API로 등기부, 전세가율, 체크리스트 통합 조회
- `FinalReportDTO`, `RawDTO`, `InsertDTO` 역할별 분리 설계
- checklist JSON → `List<Boolean>` 파싱 처리 및 조건부 렌더링 연결
- `final_report` 테이블 설계: UNIQUE 제약(user_id + registry_id), soft delete, scheduled 만료 처리 적용

### 3. 통합 데이터 흐름 설계
- 프론트-백엔드 간 통합 데이터 구조 정의 및 연동 구현
- Axios 1회 호출로 전체 분석 결과 조회 가능한 구조로 설계 (등기부, 전세가율, 체크리스트 분석 결과)
- 기존 개별 API 분산 이슈 해결 → 단일 응답 구조 정리

### 4. 컴포넌트 구조 및 재사용성 설계
- `DonutChart.vue`는 등급 타입과 색상을 props로 받아 다양한 등급 시각화에 재사용 가능
- `FinalJeonseCard.vue`는 카드 구성 요소로서 단일 데이터 기준 재사용되며, `FinalJeonse.vue`에서 반복 렌더링
- `useGradeColor.js` composable은 등급별 색상 맵핑을 통일하여 유지보수 편의성 확보

### 5. 협업 방식 및 GitHub 활용
- GitHub Flow 기반 브랜치 전략 사용 (`feature/final-report-backend` → `main` PR)
- GitHub PR 템플릿 기반 커밋 & 코드 리뷰 수행
- 이슈 단위 개발 → 완료 후 PR → 팀 리뷰 → Merge

### 6. 테스트 및 디버깅
- Postman으로 백엔드 `/api/reports` 엔드포인트 테스트 → 다양한 케이스 (존재 X, 중복 저장 등) 검증
- 프론트에서는 dummy mock 데이터를 사용한 chart 단위 테스트
- 스케줄러 로직은 DB에 임의로 과거 `created_at` 데이터를 삽입 후 cron 조건 검증

### 7. 데이터 흐름 요약

1. 사용자가 체크리스트 제출 (`/api/checklist`)
2. 백엔드에서 checklist 저장 + `final_report` 자동 생성
3. `/api/reports?userId=...&registryId=...` API 요청
4. 프론트에서 결과를 받아 `DonutChart`, `FinalChecklist`, `AnalysisCards`, `FinalGrade`에 전달

---

## 📎 API 명세 (Swagger 연동)

| Method | Endpoint | 설명 |
|--------|----------|------|
| `GET` | `/api/reports/{reportId}` | 리포트 단건 조회 (reportId 기반) |
| `GET` | `/api/reports?userId=1&registryId=1` | 유저 ID + 등기부 ID 기반 리포트 조회<br>**Query**: `userId`, `registryId` |
| `POST` | `/api/reports?userId=1&registryId=` | 최종 리포트 자동 저장<br>**Query**: `userId`, `registryId` |

---

## 🧩 ERD 요약

- `final_report`: 리포트 통합 결과 저장 (report_id, user_id, registry_id, status, created_at 등)
- `users`: 사용자 정보
- `registry_analysis`: 등기부 분석
- `jeonse_analysis`: 전세가율 분석
- `checklist`: 체크리스트 분석, checked 컬럼은 JSON 배열로 저장 → ex) [true, false, true, ...]
  
---

## 📁 문서화 / 협업

- Swagger로 API 자동 문서화
- GitHub Projects + Issues로 스프린트 기반 작업 관리
- 공통 컴포넌트 설계로 재사용성 고려

---

## 🔗 GitHub 이슈 기반 개발

GitHub Issues를 통해 문서화했습니다.
👉 [이슈 목록 보기](https://github.com/Gyeongjo119/mouem-zip/issues?q=is%3Aissue+state%3Aclosed+assignee%3AhyebinJeong)

---
