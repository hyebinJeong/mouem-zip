# 기존 테이블 삭제 밑에 주석 풀고 진행하시면 됩니다.
#  -- 외래 키 제약 해제
# SET FOREIGN_KEY_CHECKS = 0;
#
#  -- DROP TABLE (역순으로 삭제)
#  DROP TABLE IF EXISTS seizures;
#  DROP TABLE IF EXISTS provisional_seizures;
#  DROP TABLE IF EXISTS auction;
#  DROP TABLE IF EXISTS provisional_registrations;
#  DROP TABLE IF EXISTS trust;
#  DROP TABLE IF EXISTS provisional_disposition;
#  DROP TABLE IF EXISTS jeonse_right;
#  DROP TABLE IF EXISTS mortgages;
#  DROP TABLE IF EXISTS contract;
#  DROP TABLE IF EXISTS register_pdf;
#  DROP TABLE IF EXISTS final_report;
#  DROP TABLE IF EXISTS check_list;
#  DROP TABLE IF EXISTS property_address;
#  DROP TABLE IF EXISTS register_analysis_result;
#  DROP TABLE IF EXISTS registry_record;
#  DROP TABLE IF EXISTS special_contracts;
#  DROP TABLE IF EXISTS term;
#  DROP TABLE IF EXISTS category;
#  DROP TABLE IF EXISTS users;
#
#  -- 외래 키 제약 복원
#  SET FOREIGN_KEY_CHECKS = 1;

-- 수정 후 테이블
DROP TABLE IF EXISTS contract_special_clause;
DROP TABLE IF EXISTS final_report;
DROP TABLE IF EXISTS checklist;
DROP TABLE IF EXISTS jeonse_analysis;
DROP TABLE IF EXISTS term;
DROP TABLE IF EXISTS register_pdf;
DROP TABLE IF EXISTS contract;
DROP TABLE IF EXISTS special_clause;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS registry_analysis;
DROP TABLE IF EXISTS users;
# SELECT * from users;
-- ============================================
-- 1. 사용자 관련
-- ============================================
CREATE TABLE users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       kakao_id VARCHAR(100) UNIQUE NOT NULL,
                       name VARCHAR(50) NOT NULL,
                       email VARCHAR(100),
                       role VARCHAR(50) DEFAULT 'ROLE_USER',
                       state boolean DEFAULT TRUE
);

-- ============================================
-- 2. 등기부등본 분석 관련
-- ============================================
CREATE TABLE registry_analysis (
                                   registry_id INT AUTO_INCREMENT PRIMARY KEY,
                                   user_id INT,
                                   address VARCHAR(255) NOT NULL,             -- 주소
                                   risks TEXT NOT NULL,                  -- 위험 요소 전체를 JSON으로 저장
                                   registry_name VARCHAR(100) NOT NULL,  -- 등기부등본 이름
                                   registry_rating ENUM('판단보류', '안전', '보통', '주의', '위험') NOT NULL, -- 등기부등본 등급
                                   analysis_date DATETIME DEFAULT CURRENT_TIMESTAMP,               -- 분석일
                                   status BOOLEAN NOT NULL DEFAULT TRUE,
                                   file_name VARCHAR(100) NOT NULL,
                                   total_prior_amount BIGINT NOT NULL,
                                   FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ============================================
-- 3. 전세가율 분석
-- ============================================
CREATE TABLE jeonse_analysis (
                                 registry_id INT PRIMARY KEY,
                                 user_id INT,
                                 expected_selling_price INT NOT NULL,       -- 예상 매매가
                                 deposit INT NOT NULL,                      -- 보증금
                                 jeonse_ratio DECIMAL(5,2) NOT NULL,        -- 전세가율
                                 region_avg_jeonse_ratio DECIMAL(5,2) NOT NULL, -- 지역 평균 전세가율
                                 jeonse_ratio_rating ENUM('판단보류', '안전', '보통', '주의', '위험') NOT NULL,
                                 FOREIGN KEY (registry_id) REFERENCES registry_analysis(registry_id),
                                 FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- ============================================
-- 4. 체크리스트
-- ============================================
CREATE TABLE checklist (
                           registry_id INT PRIMARY KEY,
                           user_id INT,
                           checked TEXT NOT NULL,               -- 체크 여부 전체 JSON으로 저장
                           checklist_rating ENUM('판단보류', '안전', '보통', '주의', '위험') NOT NULL,
                           FOREIGN KEY (user_id) REFERENCES users(user_id),
                           FOREIGN KEY (registry_id) REFERENCES registry_analysis(registry_id)
);

-- ============================================
-- 5. 최종 리포트 및 요약
-- ============================================
CREATE TABLE final_report (
                              report_id INT AUTO_INCREMENT PRIMARY KEY,
                              user_id INT,
                              registry_id INT,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              status BOOLEAN NOT NULL DEFAULT TRUE,
                              FOREIGN KEY (user_id) REFERENCES users(user_id),
                              FOREIGN KEY (registry_id) REFERENCES registry_analysis(registry_id),
                              CONSTRAINT uq_user_registry UNIQUE (user_id, registry_id) -- user_id와 registry_id 조합의 중복 저장 방지
);

-- ============================================
-- 6. 계약서 작성 테이블
-- ============================================
CREATE TABLE contract (
                          contract_id INT AUTO_INCREMENT PRIMARY KEY, -- 계약서 번호 (PK)
                          user_id INT,                                -- 유저 ID (FK)
                          contract_name VARCHAR(100) NOT NULL,        -- 계약서 이름
                          lessor_name VARCHAR(20) NOT NULL,           -- 임대인 성명
                          lessee_name VARCHAR(20) NOT NULL,           -- 임차인 성명
                          address VARCHAR(255) NOT NULL,              -- 소재지
                          land_category VARCHAR(100) NOT NULL,        -- 토지 지목
                          land_area DECIMAL(10,2) NOT NULL,           -- 토지 면적
                          building_usage VARCHAR(100) NOT NULL,       -- 건물 구조/용도
                          building_area DECIMAL(10,2) NOT NULL,       -- 건물 면적
                          leased_part VARCHAR(100) NOT NULL,          -- 임차한 부분
                          leased_area DECIMAL(10,2) NOT NULL,         -- 임차한 면적
                          deposit BIGINT NOT NULL,                    -- 보증금
                          down_payment BIGINT NOT NULL,               -- 계약금
                          balance BIGINT NOT NULL,                    -- 잔금
                          maintenance_cost INT NOT NULL,              -- 관리비
                          lease_start DATE NOT NULL,                  -- 임대 시작일
                          lease_end DATE NOT NULL,                    -- 임대 종료일
                          special_clauses JSON NOT NULL,              -- 특약 전체 (배열/JSON으로 저장)
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (user_id) REFERENCES users(user_id)
);
# ============================================
# 7. 특약
# ============================================
CREATE TABLE special_clause (
                                special_clause_id INT PRIMARY KEY AUTO_INCREMENT,  -- 특약번호(pk)
                                category VARCHAR(50) NOT NULL,                     -- 특약분류
                                importance VARCHAR(10) NOT NULL,                   -- 중요도
                                importance_color VARCHAR(50) NOT NULL,              -- 중요도 색상 코드
                                description TEXT NOT NULL                          -- 특약 설명
);

-- ============================================
-- 8. 계약서 작성 특약
-- ============================================
# CREATE TABLE contract_special_clause (
                                           #                                          contract_id INT,                  -- 계약서 번호
                                           #                                          special_clause_id INT,            -- 특약번호
                                           #                                          PRIMARY KEY (contract_id, special_clause_id),
                                           #                                          FOREIGN KEY (contract_id) REFERENCES contract(contract_id),
                                           #                                          FOREIGN KEY (special_clause_id) REFERENCES special_clause(special_clause_id)
# );

-- ============================================
-- 9. 카테고리
-- ============================================
CREATE TABLE category(
                         category_id INT PRIMARY KEY AUTO_INCREMENT,    -- 카테고리 번호(pk)
                         category_name VARCHAR(100) NOT NULL,           -- 카테고리명
                         description VARCHAR(255) NOT NULL,             -- 카테고리 설명
                         category_color VARCHAR(50) NOT NULL             -- 카테고리 색상
);

-- ============================================
-- 10. 용어
-- ============================================
CREATE TABLE term(
                     term_id INT PRIMARY KEY AUTO_INCREMENT,        -- 용어번호(pk)
                     category_id INT,                      -- 카테고리 번호(fk)
                     term_name VARCHAR(100) NOT NULL,               -- 용어명
                     term_define VARCHAR(255) NOT NULL,             -- 용어 정의
                     term_example VARCHAR(255) NOT NULL,            -- 용어예시
                     term_caution VARCHAR(255) NOT NULL,            -- 용어 주의
                     FOREIGN KEY (category_id) REFERENCES category(category_id)
);

-- 테스트용 코드
# 카테고리
INSERT INTO category (category_name, description, category_color) VALUES
                                                                      ('전세계약과 돈 관련', '전세 계약 과정에서 발생하는 금융, 보증금, 대출 등과 관련된 내용', 'rgba(183, 255, 172, 0.45)'),
                                                                      ('전세권리 / 안전장치 관련', '세입자의 권리 보호 및 안전장치를 위한 제도 및 절차', 'rgba(159, 228, 255, 0.5)'),
                                                                      ('전세 권리 보호 관련', '세입자의 보증금과 권리를 보호하기 위한 법적 수단과 절차', 'rgba(249, 255, 193, 0.7)'),
                                                                      ('전세 계약 기간 및 갱신 관련', '계약 기간 관리, 묵시적 갱신 및 갱신 요구권 등과 관련된 내용', 'rgba(204, 193, 255, 0.6)');

# 용어
-- 전세계약과 돈 관련 (category_id = 1)
INSERT INTO term (term_name, term_define, term_example, term_caution, category_id) VALUES
                                                                                       ('보증금', '전세에서 말하는 핵심 금액. 집을 일정 기간 빌리기 위해 한꺼번에 맡기는 큰 돈. 계약 끝나면 돌려받음.',
                                                                                        '2억 원 보증금을 걸고 2년 전세계약 체결. 만료일에 임대인이 보증금을 반환.',
                                                                                        '계약서에 반환 시점과 조건을 반드시 명확히 기재해야 하며, 반환을 보장할 수단(확정일자, 전입신고 등)을 확보해야 한다.', 1),
                                                                                       ('계약금', '전세계약을 약속하면서 미리 걸어두는 돈. 계약 파기하면 두 배 물어주거나 돌려줌.',
                                                                                        '계약 체결 시 1천만 원 계약금을 지급하고, 해제 시 계약금의 두 배(2천만 원)를 배상.',
                                                                                        '계약 해제 조항(배액배상)과 반환 조건을 사전에 반드시 확인해야 한다.', 1),
                                                                                       ('중도금', '전세 보증금을 나눠낼 때, 계약금과 잔금 사이에 내는 중간 돈. 보통 전세계약은 계약금-잔금이 대부분이지만 금액이 클 때 나누기도 함.',
                                                                                        '총 3억 보증금 중 1억은 계약금, 1억 5천만 원은 중도금으로 지급.',
                                                                                        '중도금 지급 시 등기부등본 변동(근저당권 설정 여부 등)을 재확인해야 한다.', 1),
                                                                                       ('잔금', '전세 보증금의 마지막 금액. 잔금 치러야 집 열쇠 받고 전입 가능.',
                                                                                        '잔금을 모두 지급하고 집 열쇠를 받은 뒤 전입신고 완료.',
                                                                                        '잔금 지급일 이후 즉시 전입신고 및 확정일자를 받아야 안전하다.', 1),
                                                                                       ('장기수선충당금', '아파트에서 큰 수리(지붕, 엘리베이터) 위해 모아두는 돈. 전세계약자는 관리비에 포함돼 내기도 함.',
                                                                                        '관리비 고지서에 장기수선충당금 3만원이 포함되어 있었습니다.',
                                                                                        '해당 금액이 임차인 부담인지 계약 전에 반드시 확인하세요.', 1),
                                                                                       ('공과금', '수도, 전기, 가스 사용 요금. 전세든 월세든 사용량만큼 임차인이 냄.',
                                                                                        '매달 전기와 수도 공과금을 납부했습니다.',
                                                                                        '전 임차인의 체납 여부를 계약 전 반드시 확인하세요.', 1),
                                                                                       ('배액(배액배상)', '계약 파기 시, 계약금의 두 배를 물어주는 제도. 임대인·임차인 서로 책임 방지용.',
                                                                                        '집주인이 계약을 파기해 계약금 두 배를 배상했습니다.',
                                                                                        '배액배상 조항이 계약서에 정확히 기재되어 있는지 확인하세요.', 1),
                                                                                       ('업무보증관계증서 / 공제증서', '공인중개사가 사고 쳤을 때 보상받기 위해 중개사가 가입해두는 증서. 임차인 보호용.',
                                                                                        '중개사무소에서 발급한 공제증서를 계약 전 확인했습니다.',
                                                                                        '유효기간과 보장 범위를 반드시 확인하세요.', 1);

-- 전세권리 / 안전장치 관련 (category_id = 2)
INSERT INTO term (term_name, term_define, term_example, term_caution, category_id) VALUES
                                                                                       ('저당권', '집주인이 은행 대출 받을 때 집에 설정하는 담보. 경매로 넘어갈 위험이 있어 전세계약 전 반드시 확인.',
                                                                                        '등기부등본에서 저당권 2억원이 설정된 것을 확인했습니다.',
                                                                                        '저당권 금액이 보증금보다 크면 보증금 회수에 위험이 있으므로 반드시 확인하세요.', 2),
                                                                                       ('담보권', '돈 못 갚으면 집 같은 재산을 가져갈 수 있는 권리. 저당권 포함.',
                                                                                        '채무자가 빚을 갚지 않아 담보권이 실행되었습니다.',
                                                                                        '담보권 설정 여부와 금액은 등기부등본을 통해 반드시 확인해야 합니다.', 2),
                                                                                       ('전대', '세입자가 또 다른 사람에게 집을 재임대. 전세에서는 임대인 동의 필수.',
                                                                                        '임차인이 임대인 동의 없이 전대한 사실이 드러났습니다.',
                                                                                        '무단 전대는 계약 해지 사유가 되므로 반드시 서면 동의를 받아야 합니다.', 2),
                                                                                       ('임차권', '임차인이 집을 일정 기간 점유할 수 있는 권리. 계약서만 써도 자동 발생.',
                                                                                        '임대차계약 체결로 임차권이 발생했습니다.',
                                                                                        '임차권은 계약으로 자동 발생하므로 권리 범위와 의무 사항을 계약서에 명확히 기재하세요.', 2),
                                                                                       ('임차권 양도', '세입자가 본인의 권리를 다른 사람에게 넘기는 것. 전세계약도 임대인 동의 필요.',
                                                                                        '임차권 양도로 새로운 세입자가 입주했습니다.',
                                                                                        '임대인의 서면 동의 없이 양도할 경우 계약 해지 사유가 될 수 있습니다.', 2),
                                                                                       ('임차인', '전세 보증금 맡기고 집 빌린 사람. 세입자.',
                                                                                        '임차인은 보증금을 맡기고 2년간 거주했습니다.',
                                                                                        '임차인은 계약 내용에 따라 의무(차임 납부, 원상복구)를 부담하며 권리(대항력, 우선변제권)도 보유합니다.', 2),
                                                                                       ('임대인', '전세 보증금 받고 집 빌려주는 사람. 집주인.',
                                                                                        '임대인이 계약 조건 변경을 요구했습니다.',
                                                                                        '임대인의 신용 상태 및 소유권 여부는 계약 전 반드시 확인해야 합니다.', 2),
                                                                                       ('소유주', '집의 진짜 주인. 등기부등본 상 이름 올라간 사람.',
                                                                                        '등기부등본에서 소유주를 확인했습니다.',
                                                                                        '소유주와 계약 당사자가 일치하는지 확인하세요.', 2);

-- 전세 권리 보호 관련 (category_id = 3)
INSERT INTO term (term_name, term_define, term_example, term_caution, category_id) VALUES
                                                                                       ('대항력', '전입신고 + 실제 거주(점유)하면 새 집주인 와도 계약 끝날 때까지 살 권리.',
                                                                                        '전입신고와 실제 입주를 완료해 대항력을 확보했습니다.',
                                                                                        '전입신고를 늦게 하거나 실제 거주하지 않으면 대항력이 발생하지 않습니다.', 3),
                                                                                       ('우선변제권', '전입신고 + 확정일자 받아두면, 집 경매 넘어가도 보증금 먼저 돌려받을 권리.',
                                                                                        '확정일자 덕분에 경매 시 보증금을 우선 변제받았습니다.',
                                                                                        '확정일자는 반드시 계약 직후 받아야 하며, 미루면 순위에서 밀려 보증금 반환이 어려워질 수 있습니다.', 3),
                                                                                       ('확정일자', '계약서에 주민센터(동사무소)가 날짜 도장 찍어주는 것. 보증금 순위 보호에 필수.',
                                                                                        '동주민센터에서 확정일자를 부여받았습니다.',
                                                                                        '확정일자가 없으면 보증금이 일반 채권으로 취급되어 순위가 밀릴 수 있습니다.', 3),
                                                                                       ('선순위 확정일', '나보다 먼저 확정일자 받은 사람. 보증금 우선 순위가 앞서 있음.',
                                                                                        '선순위 확정일자가 있는 세입자가 있음을 확인했습니다.',
                                                                                        '선순위 확정일자가 있으면 보증금 전액 반환이 어렵거나 일부만 받을 수 있으므로 반드시 확인하세요.', 3),
                                                                                       ('주택의 점유', '실제로 들어가서 살고 있는 상태. 짐 들여놓으면 점유.',
                                                                                        '입주 후 가구를 들여놓고 점유를 시작했습니다.',
                                                                                        '점유 사실을 입증할 자료(전기·수도 사용량, 우편물 등)를 확보해야 합니다.', 3),
                                                                                       ('등기사항증명서', '등기부등본. 집주인 이름, 저당권(담보) 있는지 확인 가능. 전세계약 전 필수 확인.',
                                                                                        '인터넷 등기소에서 등기사항증명서를 발급받았습니다.',
                                                                                        '등기부등본 확인 없이 계약하면 소유자가 아닌 사람과 계약하거나 담보 설정 사실을 놓칠 수 있습니다.', 3),
                                                                                       ('다가구주택 확정일자 현황', '다가구에서 세입자가 여러 명일 때, 각자 언제 확정일자 받았는지 확인. 우선변제권에 영향.',
                                                                                        '다가구 확정일자 현황을 확인했습니다.',
                                                                                        '선순위 세입자가 많으면 보증금 반환이 어려워질 수 있어 반드시 확인해야 합니다.', 3),
                                                                                       ('부여일', '확정일자 받은 날짜. 우선변제 순위 기준.',
                                                                                        '부여일이 빠른 순서대로 변제 우선권이 부여됩니다.',
                                                                                        '부여일이 늦으면 순위가 밀려 보증금을 제대로 받지 못할 수 있습니다.', 3);

-- 전세 계약 기간 및 갱신 관련 (category_id = 4)
INSERT INTO term (term_name, term_define, term_example, term_caution, category_id) VALUES
                                                                                       ('임대차기간 개시일', '계약에서 정한 실제 입주일. 이때부터 계약 기간(2년)과 보증금 반환 기산.',
                                                                                        '임대차기간 개시일에 맞춰 전입했습니다.',
                                                                                        '개시일과 계약 체결일을 혼동하지 말고, 계약서에 명확히 표기해야 분쟁을 예방할 수 있습니다.', 4),
                                                                                       ('존속기간', '계약 유지되는 기간. 전세는 보통 2년.',
                                                                                        '존속기간 동안 임차인은 안정적으로 거주했습니다.',
                                                                                        '존속기간 종료 후 묵시적 갱신 여부를 확인하지 않으면 예상치 못한 계약 연장이 발생할 수 있습니다.', 4),
                                                                                       ('임대차 신고필증상', '전세계약 신고하면 나오는 신고 확인서. 계약 내용 공적으로 증명.',
                                                                                        '임대차 신고필증을 발급받았습니다.',
                                                                                        '신고된 내용(임대인·임차인 정보, 보증금, 기간 등)이 실제 계약과 일치하는지 반드시 확인해야 합니다.', 4),
                                                                                       ('차임증액청구', '전세는 보통 차임 없지만, 만약 보증부월세(반전세)라면 월세 인상 요구를 의미. 1년에 5% 이상 못 올림.',
                                                                                        '차임증액청구 제한 규정을 확인했습니다.',
                                                                                        '차임 인상은 법정 제한(연 1회, 5% 이내)을 초과할 수 없으며, 이를 위반하면 무효입니다.', 4),
                                                                                       ('계약갱신요구권', '계약 만료 전에 한 번 더 "2년 더 살겠다" 요구할 수 있는 권리. 집주인이 특별 사유 없으면 거절 못함.',
                                                                                        '계약갱신요구권을 행사해 2년 연장했습니다.',
                                                                                        '계약갱신요구권 행사는 기한(6개월~2개월 전)을 지켜야 하며, 거절 사유가 있는 경우 예외가 발생할 수 있습니다.', 4);






-- 특약 예시 데이터
INSERT INTO special_clause (category, importance, importance_color, description) VALUES
-- 중요 (빨강)
('대출', '높음', '#FF0000', '전세자금 대출이 불가능할 경우, 계약금은 전액 반환한다.'),
('보증보험', '높음', '#FF0000', '임대인은 임차인의 보증보험 가입에 필요한 절차에 적극 협조하여, 임대인 또는 집의 하자로 인해 보증보험 가입이 안 될 경우 계약은 무효로 하고, 임대인은 낸 돈 전부를 즉시 돌려준다.'),
('등기부등본', '높음', '#FF0000', '임대인은 잔금 지급일 익일까지 담보권이나 전세권 등 새로운 권리를 설정하지 않는다.'),
('등기부등본', '높음', '#FF0000', '임대인은 잔금 지급일 또는 보증보험 가입 예정일까지 소유권 이전 등기를 하지 않는다.'),
('세금', '높음', '#FF0000', '임대인은 국세 및 지방세, 근저당권 이자 체납이 없음을 고지하며, 임차인이 세금 체납 여부를 확인할 수 있도록 적극 협조한다. 만약 체납이 확인되면 잔금일 전까지 모두 상환하고, 이를 어기면 계약은 무효이며 계약금은 즉시 반환한다.'),
('매매 고지', '높음', '#FF0000', '임대인은 임대차 기간 중 집을 팔게 될 경우, 임차인에게 반드시 고지한다.'),
('보증금 반환', '높음', '#FF0000', '임차인이 계약 만료시 임대인은 타 임차인 임대여부와 관계없이 보증금을 즉시 전액 반환한다.'),

-- 중간 (노랑)
('재건축·철거', '중간', '#FFA500', '임대인은 임대차 기간 중 재건축/철거/용도변경 계획이 있는 경우 계약 체결 시 이를 명시하며, 사전 고지 없이 일방적으로 실행하지 않는다.'),
('명도/이사', '중간', '#FFA500', '매도인은 잔금일에 명도하며, 기한 내 미이행 시 지연 1일당 10만원의 배상금을 매수인에게 지급한다.'),
('수리/하자보수', '중간', '#FFA500', '입주 전/후 발생한 누수, 곰팡이, 보일러, 배관 등 시설물 하자는 임대인이 수리한다.'),
('중도 해지', '중간', '#FFA500', '임차인이 계약 기간 중 퇴거할 경우 직접 신규 세입자를 구하고 임대인의 동의를 받아야 하며, 새 계약 체결 시점부터 보증금 반환한다.'),
('계약 갱신', '중간', '#FFA500', '계약 만료일 6개월 전~1개월 전까지 갱신 거절 통보 없으면 계약은 자동 연장된다. 임대인이 계약 갱신을 거절하는 경우, 임차인이 새로운 거주지를 찾을 수 있도록 최소 2개월 이상의 유예기간을 보장한다.'),
('관리비/세금 정산', '중간', '#FFA500', '계약일 이전 발생한 모든 관리비 및 세금(국세·지방세 포함)은 임대인이 정산한다. 이후 발생분은 임차인 부담한다.'),

-- 참고 (초록)
('원상복구', '낮음', '#00FF00', '임차인은 퇴거 시 원상복구하되, 벽지, 페인트 등 통상적 사용에 의한 손상은 제외한다.'),
('공과금', '낮음', '#00FF00', '공동 전기, 청소, 경비 등 관리비는 임차인 부담, 수선유지비는 임대인 부담, 전기, 수도, 가스는 실사용량 기준 임차인 부담한다.'),
('전대 금지', '낮음', '#00FF00', '주택은 주거용으로만 사용하며, 임차인은 임대인의 사전 서면 동의 없이 제 3자에게 무단 전대(재임대) 할 수 없다.'),
('법적 분쟁', '낮음', '#00FF00', '본 계약은 공인중개사 없이 직거래로 체결되었으며, 발생할 수 있는 분쟁은 민법 및 주택임대차보호법에 따른다.');