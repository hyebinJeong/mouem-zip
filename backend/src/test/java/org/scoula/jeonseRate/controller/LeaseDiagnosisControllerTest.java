package org.scoula.jeonseRate.controller;

import org.junit.jupiter.api.Test;
import org.scoula.jeonseRate.domain.JeonseAnalysisVO;
import org.scoula.jeonseRate.dto.AddressInfoDTO;
import org.scoula.jeonseRate.dto.JeonseRateDTO;
import org.scoula.jeonseRate.enums.SafetyGrade;
import org.scoula.jeonseRate.mapper.JeonseAnalysisMapper;
import org.scoula.jeonseRate.service.AddressService;
import org.scoula.jeonseRate.service.DealSearchService;
import org.scoula.jeonseRate.service.KosisJeonseRateService;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LeaseDiagnosisControllerTest {

    // DealSearchService - 면적 필터링 검증
    class DealServiceWithAreaCheck extends DealSearchService {
        public DealServiceWithAreaCheck() { super(null, null); }

        @Override
        public Optional<JeonseRateDTO> getDealAmount(
                String lawdCode, String jibun, String buildingName, List<String> recentMonths, Double targetArea) {
            // 면적이 제대로 전달되는지 검증
            assertEquals("123", jibun);
            assertNull(buildingName);
            assertNotNull(targetArea);
            assertEquals(84.03, targetArea); // 테스트에서 넣은 값과 같은지 확인
            System.out.println("테스트에서 받은 면적: " + targetArea);

            JeonseRateDTO dto = new JeonseRateDTO();
            dto.setAreaAVGPrice(200000);
            dto.setBuildingType("아파트");
            return Optional.of(dto);
        }
    }

    // AddressService - 건물명 없게 처리
    class NoBuildingNameAddressService extends AddressService {
        public NoBuildingNameAddressService() { super(null); }
        @Override
        public AddressInfoDTO lookupAddress(String keyword) {
            return new AddressInfoDTO("11710", "123", "서울특별시", null, "송파구");
        }
    }

    // DealSearchService - 지번만으로 처리되었는지 확인
    class DealServiceExpectJibunOnly extends DealSearchService {
        public DealServiceExpectJibunOnly() { super(null, null); }
        @Override
        public Optional<JeonseRateDTO> getDealAmount(String lawdCode, String jibun, String buildingName, List<String> recentMonths, Double targetArea) {
            assertNull(buildingName);
            assertEquals("123", jibun);
            JeonseRateDTO dto = new JeonseRateDTO();
            dto.setAreaAVGPrice(200000);
            dto.setBuildingType("아파트");
            return Optional.of(dto);
        }
    }

    // DealSearchService - 매물 없음
    class DealServiceNoDeal extends DealSearchService {
        public DealServiceNoDeal() { super(null, null); }
        @Override
        public Optional<JeonseRateDTO> getDealAmount(String lawdCode, String jibun, String buildingName, List<String> recentMonths, Double targetArea) {
            return Optional.empty();
        }
    }

    // KOSIS - 구 → 시 fallback
    class FallbackKosisService extends KosisJeonseRateService {
        List<String> logs = new ArrayList<>();
        public FallbackKosisService() { super(null); }
        @Override
        public List<Map<String, Object>> fetchKosisData(Optional<JeonseRateDTO> deal, String objL2) {
            logs.add(objL2);
            System.out.println("호출된 KOSIS 코드: " + objL2);
            if (objL2.equals("a7020203")) return List.of(); // 구 실패
            if (objL2.equals("a7")) return List.of(Map.of("DT", "60.0")); // 시도 성공
            return List.of();
        }
    }

    // KOSIS - 구, 시 모두 실패
    class KosisNoData extends KosisJeonseRateService {
        public KosisNoData() { super(null); }
        @Override
        public List<Map<String, Object>> fetchKosisData(Optional<JeonseRateDTO> deal, String objL2) {
            return List.of();
        }
    }

    // TestMapper
    class TestMapper implements JeonseAnalysisMapper {
        boolean insertCalled = false;
        JeonseAnalysisVO savedVo;

        @Override
        public void insertJeonseAnalysis(JeonseAnalysisVO vo) {
            insertCalled = true;
            savedVo = vo;
            System.out.println("Insert됨: 전세가율=" + vo.getJeonseRatio() + ", 지역평균=" + vo.getRegionAvgJeonseRatio() + ", 등급=" + vo.getJeonseRatioRating());
        }

        @Override
        public int findJeonseRatioByRegisterId(int registerId) {
            return 60;
        }
    }

    @Test
    void 건물명_없을때_지번기반으로_조회_가능해야함() {
        System.out.println("\n테스트: 건물명 없을 때 지번 기반 조회");
        TestMapper mapper = new TestMapper();
        LeaseDiagnosisController controller = new LeaseDiagnosisController(
                new NoBuildingNameAddressService(),
                new DealServiceExpectJibunOnly(),
                new KosisNoData(),
                mapper
        );

        JeonseRateDTO request = new JeonseRateDTO();
        request.setAddress("서울 송파");
        request.setJeonsePrice(100000);
        request.setRegisterId(100);
        request.setUserId(200);

        ResponseEntity<?> res = controller.analyzeLease(request);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals(50, mapper.savedVo.getJeonseRatio());
    }

    @Test
    void 실거래가_없으면_판단보류() {
        System.out.println("\n테스트: 실거래가 없음 → 판단보류");
        LeaseDiagnosisController controller = new LeaseDiagnosisController(
                new NoBuildingNameAddressService(),
                new DealServiceNoDeal(),
                new KosisNoData(),
                new TestMapper()
        );

        JeonseRateDTO req = new JeonseRateDTO();
        req.setAddress("서울 송파");
        req.setJeonsePrice(123000);
        req.setRegisterId(300);
        req.setUserId(10);

        ResponseEntity<?> res = controller.analyzeLease(req);
        Map<?, ?> body = (Map<?, ?>) res.getBody();

        assertEquals(200, res.getStatusCodeValue());
        assertEquals(SafetyGrade.판단보류.name(), body.get("grade"));
        assertEquals(-1, body.get("jeonseRate"));
    }

    @Test
    void KOSIS_구단위_실패시_시단위_fallback_조회() {
        System.out.println("\n테스트: 구단위 실패 → 시도 fallback");
        FallbackKosisService kosis = new FallbackKosisService();
        TestMapper mapper = new TestMapper();

        LeaseDiagnosisController controller = new LeaseDiagnosisController(
                new NoBuildingNameAddressService(),
                new DealServiceExpectJibunOnly(),
                kosis,
                mapper
        );

        JeonseRateDTO req = new JeonseRateDTO();
        req.setAddress("서울 송파");
        req.setJeonsePrice(150000);
        req.setRegisterId(301);
        req.setUserId(999);

        ResponseEntity<?> res = controller.analyzeLease(req);
        assertEquals(204, res.getStatusCodeValue());

        // 실제 호출된 로그가 둘 다 포함되었는지 확인
        assertTrue(kosis.logs.contains("a7020203")); // 구단위 코드
        assertTrue(kosis.logs.contains("a7"));       // 시도 fallback
    }

    @Test
    void KOSIS_시구_모두_실패시_판단보류() {
        System.out.println("\n테스트: KOSIS 구/시 모두 실패 → 판단보류");
        TestMapper mapper = new TestMapper();
        LeaseDiagnosisController controller = new LeaseDiagnosisController(
                new NoBuildingNameAddressService(),
                new DealServiceExpectJibunOnly(),
                new KosisNoData(),
                mapper
        );

        JeonseRateDTO req = new JeonseRateDTO();
        req.setAddress("서울 송파");
        req.setJeonsePrice(150000);
        req.setRegisterId(302);
        req.setUserId(303);

        ResponseEntity<?> res = controller.analyzeLease(req);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals(SafetyGrade.판단보류, mapper.savedVo.getJeonseRatioRating());
    }

    @Test
    void 전세가율_계산_및_등급_판별_정확성_검증() {
        System.out.println("\n테스트: 전세가율 계산 및 등급 판단 정확성 검증");
        // KOSIS 서비스 - 평균 53.9% 반환
        KosisJeonseRateService kosis = new KosisJeonseRateService(null) {
            @Override
            public List<Map<String, Object>> fetchKosisData(Optional<JeonseRateDTO> deal, String objL2) {
                return List.of(Map.of("DT", "53.9"));
            }
        };

        // 실거래가 평균 1억 2500만, 타입: 아파트
        DealSearchService deal = new DealSearchService(null, null) {
            @Override
            public Optional<JeonseRateDTO> getDealAmount(String lawdCode, String jibun, String buildingName, List<String> recentMonths, Double targetArea) {
                JeonseRateDTO dto = new JeonseRateDTO();
                dto.setAreaAVGPrice(125000); // 만원 단위
                dto.setBuildingType("아파트");
                return Optional.of(dto);
            }
        };

        // 주소 (지번만 존재)
        AddressService address = new AddressService(null) {
            @Override
            public AddressInfoDTO lookupAddress(String keyword) {
                return new AddressInfoDTO("11710", "88", "서울특별시", null, "송파구");
            }
        };

        // 결과 저장 확인용 Mapper
        TestMapper mapper = new TestMapper();

        LeaseDiagnosisController controller = new LeaseDiagnosisController(
                address,
                deal,
                kosis,
                mapper
        );

        JeonseRateDTO req = new JeonseRateDTO();
        req.setAddress("서울 송파");
        req.setJeonsePrice(80000);   // 보증금 8000만
        req.setRegisterId(88);
        req.setUserId(77);

        ResponseEntity<?> res = controller.analyzeLease(req);

        // 기대값 검증
        assertEquals(204, res.getStatusCodeValue()); // 정상 응답 (204)
        assertTrue(mapper.insertCalled);             // DB 저장 호출됨
        assertEquals(64, mapper.savedVo.getJeonseRatio());         // 전세가율
        assertEquals(54, mapper.savedVo.getRegionAvgJeonseRatio()); // 지역 평균 (반올림)
        assertEquals(SafetyGrade.위험, mapper.savedVo.getJeonseRatioRating()); // 등급
    }

    @Test
    void 면적_값이_정상적으로_전달되어야함() {
        System.out.println("\n테스트: 면적 값 전달 확인");
        TestMapper mapper = new TestMapper();
        LeaseDiagnosisController controller = new LeaseDiagnosisController(
                new NoBuildingNameAddressService(),
                new DealServiceWithAreaCheck(),
                new KosisNoData(),
                mapper
        );

        JeonseRateDTO req = new JeonseRateDTO();
        req.setAddress("서울 송파");
        req.setJeonsePrice(100000);
        req.setRegisterId(100);
        req.setUserId(200);
        req.setExcluUseAr(84.03); // ✅ 면적 세팅

        ResponseEntity<?> res = controller.analyzeLease(req);
        assertEquals(200, res.getStatusCodeValue());
    }
}
