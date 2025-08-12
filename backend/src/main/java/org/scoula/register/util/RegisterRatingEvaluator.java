package org.scoula.register.util;

import org.scoula.register.domain.RegistryRating;
import org.scoula.register.domain.dto.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RegisterRatingEvaluator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년M월d일");

    public static RiskEvaluationResult evaluateRiskLevel(RegisterAnalysisResponse response, long myDeposit) {
        LocalDate now = LocalDate.now();

        boolean hasCanceledWithin1Year = false;
        boolean hasCanceledWithin2Years = false;

        boolean hasActiveAuction = false;
        boolean hasOtherActiveRights = false; // 금액 합산 제외된 권리 중 말소 안 된 것

        long totalPriorAmount = 0; // 선순위 채권 총액

        // 각 리스트에 대해 처리
        for (List<? extends DatedCanceledItem> infoList : getAllInfoLists(response)) {
            for (DatedCanceledItem item : infoList) {
                String dateStr = item.getDate();

                if (dateStr == null || dateStr.isBlank()) {
                    // 날짜 정보 없으면 건너뛰기
                    continue;
                }

                LocalDate date = LocalDate.parse(item.getDate(), formatter);

                if (!item.isCanceled()) {
                    // 경매 체크
                    if (item instanceof AuctionDTO) {
                        if (!item.isCanceled()) {
                            hasActiveAuction = true;
                        }
                        continue;
                    }

                    // 금액 합산: DTO별로 타입 확인
                    if (item instanceof JeonseRightDTO) {
                        totalPriorAmount += parseAmount(((JeonseRightDTO) item).getDeposit());
                    } else if (item instanceof MortgageDTO) {
                        totalPriorAmount += parseAmount(((MortgageDTO) item).getMaxClaimAmount());
                    } else if (item instanceof ProvisionalSeizureDTO) {
                        totalPriorAmount += parseAmount(((ProvisionalSeizureDTO) item).getMaxClaimAmount());
                    } else if (item instanceof SeizureDTO
                            || item instanceof InjunctionDTO
                            || item instanceof TrustDTO
                            || item instanceof ProvisionalRegistrationDTO) {
                        // 금액 필드는 없지만, 말소 안 된 권리가 있으면 위험 신호
                        hasOtherActiveRights = true;
                    }
                } else {
                    // 말소된 권리 중 최근 1년, 2년 내 내역 체크
                    long days = java.time.temporal.ChronoUnit.DAYS.between(date, now);
                    if (days <= 365) {
                        hasCanceledWithin1Year = true;
                    } else if (days <= 730) {
                        hasCanceledWithin2Years = true;
                    }
                }
            }
        }

//        System.out.println("선순위 채권 총액: " + totalPriorAmount);
//        System.out.println("보증금: " + myDeposit);

        // 경매 내역 있으면 무조건 위험
        RegistryRating rating;

        if (hasActiveAuction) {
            rating = RegistryRating.위험;
        } else {
            // 말소 안 된 권리가 하나라도 있으면 위험도 평가 시작
            boolean hasAnyActiveRights = hasOtherActiveRights || totalPriorAmount > 0;

            if (hasAnyActiveRights) {
                // 선순위 채권 / 보증금 비율
                double ratio = (double) totalPriorAmount / myDeposit;

                if (ratio >= 1.0) { // 100% 이상
                    rating = RegistryRating.위험;
                } else if (ratio >= 0.4) { // 40% 이상 100% 미만
                    rating = RegistryRating.주의;
                } else if (ratio > 0) { // 0 초과 40% 미만
                    rating = RegistryRating.보통;
                } else {
                    rating = RegistryRating.안전;
                }
            } else {
                // 말소 안 된 권리 없으면 말소 내역 기준으로 판단
                if (hasCanceledWithin1Year) rating = RegistryRating.주의;
                else if (hasCanceledWithin2Years) rating = RegistryRating.보통;
                else rating = RegistryRating.안전;
            }
        }

        return new RiskEvaluationResult(rating, totalPriorAmount);
    }

    private static List<List<? extends DatedCanceledItem>> getAllInfoLists(RegisterAnalysisResponse response) {
        return List.of(
                response.getMortgageInfos(),
                response.getSeizureInfos(),
                response.getProvisionalSeizureInfos(),
                response.getAuctionInfos(),
                response.getProvisionalRegistrationInfos(),
                response.getInjunctionInfos(),
                response.getJeonseRightInfos(),
                response.getTrustInfos()
        );
    }

    private static long parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return 0L;
        String numeric = amountStr.replaceAll("[^0-9]", "");
        if (numeric.isEmpty()) return 0L;
        return Long.parseLong(numeric);
    }
}

