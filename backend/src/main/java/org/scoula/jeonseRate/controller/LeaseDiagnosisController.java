package org.scoula.jeonseRate.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.scoula.jeonseRate.domain.JeonseAnalysisVO;
import org.scoula.jeonseRate.dto.AddressInfoDTO;
import org.scoula.jeonseRate.dto.JeonseRateDTO;
import org.scoula.jeonseRate.enums.KosisRegionCode;
import org.scoula.jeonseRate.enums.KosisRegionDistrictCode;
import org.scoula.jeonseRate.enums.SafetyGrade;
import org.scoula.jeonseRate.mapper.JeonseAnalysisMapper;
import org.scoula.jeonseRate.service.AddressService;
import org.scoula.jeonseRate.service.DealSearchService;
import org.scoula.jeonseRate.service.KosisJeonseRateService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Api(tags = "jeonse-rate-analysis-controller")
@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class LeaseDiagnosisController {

    private final AddressService addressService;
    private final DealSearchService dealSearchService;
    private final KosisJeonseRateService kosisJeonseRateService;
    private final JeonseAnalysisMapper jeonseAnalysisMapper;


    @ApiOperation(value = "Analyze Jeonse Rate", notes = "Analyze the jeonse ratio based on address and deposit")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Analysis success (results in response body)"),
            @ApiResponse(code = 400, message = "Missing address or request data"),
            @ApiResponse(code = 404, message = "No matching real estate deal found")
    })
    @PostMapping("/leasePer")
    public ResponseEntity<?> analyzeLease(@RequestBody JeonseRateDTO request) {
        String keyword = request.getAddress();
        int registerId = request.getRegisterId();

        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.badRequest().body("주소가 없습니다.");
        }

        // 1. 주소 정보 조회
        AddressInfoDTO addressInfo = addressService.lookupAddress(keyword);

        // 2. 최근 6개월 (yyyyMM) 목록 생성
        List<String> recentMonths = getRecentMonths(6);

        // 3. 법정동코드 앞 5자리
        String lawdCode = addressInfo.getAdmCd().substring(0, 5);

        // 4. 지번 주소 (예: 595-28)
        String jibun  = addressInfo.getJibunAddr();

        // 5. 건물명
        String buildingName = addressInfo.getBdNm();
        
        // 6. 전용 면적
        Double targetArea = request.getExcluUseAr();

        // 6. 해당 주소에 대한 매매가 평균 조회
        Optional<JeonseRateDTO> averageDealPriceOpt = dealSearchService.getDealAmount(
                lawdCode, jibun, buildingName, recentMonths, targetArea
        );

        // 조회된 매물이 없을 경우 메시지 반환
        if (averageDealPriceOpt.isEmpty()) {
            SafetyGrade grade = SafetyGrade.판단보류;
            JeonseAnalysisVO vo = new JeonseAnalysisVO();
            vo.setRegistryId(registerId);
            vo.setUserId(request.getUserId());
            vo.setExpectedSellingPrice(-1);
            vo.setDeposit(request.getJeonsePrice());
            vo.setJeonseRatio(-1);
            vo.setRegionAvgJeonseRatio(-1);
            vo.setJeonseRatioRating(grade);
            jeonseAnalysisMapper.insertJeonseAnalysis(vo);
            return ResponseEntity.ok(Map.of(
                    "status", "NO_MATCHING_DEAL",
                    "message", "해당 주소에 대한 유사 매물 정보를 찾을 수 없습니다.",
                    "grade", grade.name(),
                    "jeonsePrice", request.getJeonsePrice(),
                    "expectedSalePrice", -1,
                    "jeonseRate", -1,
                    "avgKosisRate", -1,
                    "deviation", -1,
                    "admCd", addressInfo.getAdmCd(),
                    "jibunAddr", addressInfo.getJibunAddr()
            ));
        }

        // 7. 전세가율 계산
        int averageDealPrice = averageDealPriceOpt.get().getAreaAVGPrice();            // 평균 매매가 (단위: 만원)
        int jeonse = request.getJeonsePrice();                          // 입력 전세 보증금
        double jeonseRate = ((double) jeonse / averageDealPrice) * 100; // 전세가율
        int roundedJeonseRate = (int) Math.round(jeonseRate);           // 전세가율 반올림하여 정수 처리

        // 8. KOSIS 지역 코드 조회: 구 → 시 fallback 구조
        Optional<String> guCodeOpt = KosisRegionDistrictCode.findBySiDoAndSgg(addressInfo.getSiNm(), addressInfo.getSggNm())
                .map(KosisRegionDistrictCode::getCode);

        Optional<String> siCodeOpt = KosisRegionCode.findCodeFromRawRegionName(addressInfo.getSiNm());

        List<Map<String, Object>> kosisData = Collections.emptyList();

        // 8-1. 구 단위 코드 먼저 시도
        if (guCodeOpt.isPresent()) {
            kosisData = kosisJeonseRateService.fetchKosisData(averageDealPriceOpt, guCodeOpt.get());
        }

        // 8-2. 구 코드 결과가 없으면 시도 단위로 fallback
        if ((kosisData == null || kosisData.isEmpty()) && siCodeOpt.isPresent()) {
            kosisData = kosisJeonseRateService.fetchKosisData(averageDealPriceOpt, siCodeOpt.get());
        }

        // 8-3. 둘 다 없으면 판단보류 처리
        if (kosisData == null || kosisData.isEmpty()) {
            SafetyGrade grade = SafetyGrade.판단보류;

            JeonseAnalysisVO vo = new JeonseAnalysisVO();
            vo.setRegistryId(registerId);
            vo.setUserId(request.getUserId());
            vo.setExpectedSellingPrice(averageDealPrice);
            vo.setDeposit(jeonse);
            vo.setJeonseRatio(roundedJeonseRate);
            vo.setRegionAvgJeonseRatio(-1);
            vo.setJeonseRatioRating(grade);
            jeonseAnalysisMapper.insertJeonseAnalysis(vo);

            return ResponseEntity.ok(Map.of(
                    "status", "NO_KOSIS_DATA",
                    "message", "KOSIS 통계 데이터를 찾을 수 없습니다.",
                    "grade", grade.name(),
                    "jeonsePrice", jeonse,
                    "expectedSalePrice", averageDealPrice,
                    "jeonseRate", roundedJeonseRate,
                    "avgKosisRate", -1,
                    "deviation", -1,
                    "admCd", addressInfo.getAdmCd(),
                    "jibunAddr", addressInfo.getJibunAddr()
            ));
        }


        double avgKosisRate = kosisData.stream()
                .map(m -> Double.parseDouble(m.get("DT").toString()))
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        // 10. 편차 계산 및 등급 판별
        int deviation = roundedJeonseRate - (int) Math.round(avgKosisRate);
        SafetyGrade grade = SafetyGrade.fromDeviation(kosisData.isEmpty() ? null : deviation);

        //11. DB 저장
        JeonseAnalysisVO vo = new JeonseAnalysisVO();
        vo.setRegistryId(registerId);                 // 등기부등본 번호
        vo.setUserId(request.getUserId());            // 유저 번호
        vo.setExpectedSellingPrice(averageDealPrice); // 평균 매매가
        vo.setDeposit(jeonse);                        // 사용자가 입력한 전세보증금
        vo.setJeonseRatio(roundedJeonseRate);         // 전세가율
        vo.setRegionAvgJeonseRatio((int) Math.round(avgKosisRate)); // 지역 평균 전세가율
        vo.setJeonseRatioRating(grade); // 등급

        jeonseAnalysisMapper.insertJeonseAnalysis(vo);

        return ResponseEntity.noContent().build();
    }

    //최근 N개월에 해당하는 "yyyyMM" 형식의 문자열 목록 반환
    private List<String> getRecentMonths(int count) {
        List<String> months = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 0; i < count; i++) {
            LocalDate target = now.minusMonths(i);
            months.add(String.format("%d%02d", target.getYear(), target.getMonthValue()));
        }
        return months;
    }

    // 예상 전세가율 조회
    @ApiOperation(
            value = "Get Jeonse Rate Result",
            notes = "Retrieve previously saved jeonse rate analysis result using registry ID"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully retrieved jeonse rate"),
            @ApiResponse(code = 404, message = "Jeonse rate not found for the given registry ID (data not saved or not analyzed yet)")
    })
    @GetMapping("/result")
    public ResponseEntity<?> getJeonseAnalysisResult(@RequestParam("registerId") int registerId) {
        Integer ratio  = jeonseAnalysisMapper.findJeonseRatioByRegisterId(registerId);

        if (ratio == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("전세가율 정보가 없습니다.");
        }

        return ResponseEntity.ok(Map.of("jeonseRate", ratio));
    }
}
