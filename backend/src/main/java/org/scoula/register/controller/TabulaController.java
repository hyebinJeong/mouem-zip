package org.scoula.register.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.register.domain.RegistryRating;
import org.scoula.register.domain.dto.*;
import org.scoula.register.service.*;
import org.scoula.register.util.RegisterRatingEvaluator;
import org.scoula.register.util.RegisterUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/safety-check")
public class TabulaController {

    private final TabulaService tabulaService;
    private final AwsS3Service awsS3Service;
    private final MortgageServiceImpl mortgageServiceImpl;
    private final SeizureServiceImpl seizureServiceImpl;
    private final ProvisionalSeizureServiceImpl provisionalSeizureServiceImpl;
    private final AuctionServiceImpl auctionServiceImpl;
    private final ProvisionalRegistrationServiceImpl provisionalRegistrationServiceImpl;
    private final InjunctionServiceImpl injunctionServiceImpl;
    private final JeonseRightServiceImpl jeonseRightServiceImpl;
    private final TrustServiceImpl trustServiceImpl;

    @PostMapping
    public ResponseEntity<?> analyzeRegistry(@RequestParam("userId") Integer userId, @RequestParam("file") MultipartFile file, @RequestParam("address") String address, @RequestParam("detail") String detail, @RequestParam("jeonsePrice") String jeonsePrice, @RequestParam("registryName") String registryName) {
        try {
            // S3 업로드
            String uploadedFileName = awsS3Service.uploadFile(file);

            List<List<String>> table = tabulaService.extractTable(file.getInputStream());

            RegisterAnalysisResponse response = new RegisterAnalysisResponse();
            response.setMortgageInfos(mortgageServiceImpl.extractMortgageInfos(table));
            response.setSeizureInfos(seizureServiceImpl.extractSeizureInfos(table));
            response.setProvisionalSeizureInfos(provisionalSeizureServiceImpl.extractProvisionalSeizureInfos(table));
            response.setAuctionInfos(auctionServiceImpl.extractAuctionInfos(table));
            response.setProvisionalRegistrationInfos(provisionalRegistrationServiceImpl.extractProvisionalRegistrationInfos(table));
            response.setInjunctionInfos(injunctionServiceImpl.extractInjunctions(table));
            response.setJeonseRightInfos(jeonseRightServiceImpl.extractJeonseRightInfos(table));
            response.setTrustInfos(trustServiceImpl.extractTrustInfos(table));

            // 위험 등급 평가
            long depositInWon = 0L; // try 밖에서 선언
            try {
                int depositInManWon = Integer.parseInt(jeonsePrice);
                depositInWon = depositInManWon * 10_000L;
                // depositInWon 으로 계산 진행
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            RiskEvaluationResult data = RegisterRatingEvaluator.evaluateRiskLevel(response, depositInWon);
            RegistryRating registryRating = data.getRating();
            long totalPriorAmount = data.getTotalPriorAmount();

            // 면적 추출
            String area = RegisterUtils.getArea(table, detail);
//            System.out.println("면적 값: " + area);
            int registerId = tabulaService.saveAnalysis(userId, address, response, registryName, registryRating, uploadedFileName, totalPriorAmount);

            Map<String, Object> result = new HashMap<>();
            result.put("registerId", registerId);
            result.put("area", area);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{registerId}")
    public ResponseEntity<?> getAnalysisByRegisterId(@PathVariable("registerId") Integer registerId) {
        try {
            RegisterDTO dto = tabulaService.findByRegisterId(registerId);
            RegisterAnalysisResponse response = tabulaService.convertDTOToResponse(dto);

            // 필터링 처리 (canceled=false만 남김)
            response.setMortgageInfos(
                    response.getMortgageInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setSeizureInfos(
                    response.getSeizureInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setProvisionalSeizureInfos(
                    response.getProvisionalSeizureInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setAuctionInfos(
                    response.getAuctionInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setProvisionalRegistrationInfos(
                    response.getProvisionalRegistrationInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setInjunctionInfos(
                    response.getInjunctionInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setJeonseRightInfos(
                    response.getJeonseRightInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );
            response.setTrustInfos(
                    response.getTrustInfos().stream()
                            .filter(item -> !item.isCanceled())
                            .toList()
            );

            String fileUrl = awsS3Service.getFileUrl(dto.getFileName());

            Map<String, Object> result = new HashMap<>();
            result.put("analysis", response);
            result.put("address", dto.getAddress());
            result.put("rating", dto.getRegistryRating());
            result.put("fileUrl", fileUrl);
            result.put("totalPriorAmount", dto.getTotalPriorAmount());

            return ResponseEntity.ok(result);
        } catch (NullPointerException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("분석 결과를 찾을 수 없습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}