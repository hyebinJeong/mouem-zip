package org.scoula.register.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterUtils {
    // 다음 장으로 넘어간 셀 정보 합치기
    public static List<List<String>> mergeRowsWithCanceled(List<List<String>> tableData) {
        List<List<String>> result = new ArrayList<>();
        // 표의 헤더인지 확인
        List<String> headerRow = List.of("순위번호", "등 기 목 적", "접 수", "등 기 원 인", "권리자 및 기타사항");

        for (int i = 0; i < tableData.size(); i++) {
            List<String> currentRow = tableData.get(i);
            // 정확히 헤더와 일치하거나, 헤더 키워드가 대부분 포함된 행은 무시
            long headerCount = headerRow.stream().filter(currentRow::contains).count();
            boolean isHeaderLike = headerCount >= 3;

            if (isHeaderLike) {
                continue; // 헤더 행은 무시
            }

            boolean isNewRow = currentRow.get(0) != null
                    && !currentRow.get(0).isBlank()
                    && !currentRow.get(0).equals("순위번호");

            // 순번이 있는 경우: 일반 등기 행으로 간주
            if (isNewRow) {
                // 복사해서 추가
                result.add(new ArrayList<>(currentRow));
            } else {
                // 순번이 없는 경우: 기말소/해지 등기일 가능성
                if (!result.isEmpty()) {
                    List<String> lastRow = result.get(result.size() - 1);
                    for (int j = 0; j < currentRow.size(); j++) {
                        String val = currentRow.get(j).trim();
                        if (!val.isEmpty()) {
                            String origin = lastRow.get(j);
                            lastRow.set(j, origin + val);
                        }
                    }
//                    System.out.println("합쳐진 행 (" + (result.size() - 1) + "): " + lastRow);
                }
            }
        }

        return result;
    }

    public static String extractDate(String text) {
        // 숫자 사이 한글 제거
        String cleanedText = text.replaceAll("(?<=\\d)(?!(년|월|일))[가-힣]+(?=\\d)", "");

        Pattern pattern = Pattern.compile("\\d{4}년\\d{1,2}월\\d{1,2}일");
        Matcher matcher = pattern.matcher(cleanedText);

        if (matcher.find()) {
            String result = matcher.group();
            return result;
        } else {
            return null;
        }
    }

    public static String extractCreditorOrRightHolder(String text) {
        if (text == null) {
            return null;
        }

        // 탐색할 키워드 목록
        String[] keywords = {
                "채권자", "권리자", "근저당권자", "가등기권자", "수탁자", "전세권자", "소유자"
        };

        int minIdx = Integer.MAX_VALUE;
        String foundKeyword = null;

        // 가장 먼저 등장하는 키워드 찾기
        for (String keyword : keywords) {
            int idx = text.indexOf(keyword);
            if (idx != -1 && idx < minIdx) {
                minIdx = idx;
                foundKeyword = keyword;
            }
        }

        // 해당 키워드가 없으면 null 반환
        if (foundKeyword == null) {
            return null;
        }

        String after = text.substring(minIdx + foundKeyword.length()).trim();

        // 사업자번호 또는 주민번호와 같은 숫자 패턴 기준으로 자르기
        Pattern pattern = Pattern.compile("^(.+?)\\s+(\\d{6}-\\d{7}|\\d{6}-\\d{1}\\*{6})");
        Matcher matcher = pattern.matcher(after);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 위 패턴이 없을 경우: 띄어쓰기 기준 첫 단어만
        String[] tokens = after.split("\\s+");
        if (tokens.length > 0) {
            return tokens[0].trim();
        }

        return after;
    }

    // 괄호 앞 자르기
    public static String trimAfterParenthesis(String text) {
        if (text == null) return null;
        int idx = text.indexOf('(');
        return (idx != -1) ? text.substring(0, idx).trim() : text.trim();
    }

    // 공백 없애기
    public static String normalizeText(String text) {
        if (text == null) return null;
        return text.replaceAll("\\s+", ""); // 모든 공백 제거
    }

    public static List<String> extractCanceledRanks(String registrationPurpose) {
        List<String> ranks = new ArrayList<>();

        // "5-1번", "5-2번", "7번" 등 패턴 찾기
        Pattern pattern = Pattern.compile("(\\d+(?:-\\d+)?)[ ]?번");
        Matcher matcher = pattern.matcher(registrationPurpose);

        while (matcher.find()) {
            ranks.add(matcher.group(1).trim()); // "5-1", "5-2" 등
        }

        return ranks;
    }

    // 금액 추출
    public static String extractAmountByKeywords(String text) {
        if (text == null) return null;

        // 내부에 키워드 목록 고정
        List<String> keywords = List.of("청구금액", "전세금", "채권최고액", "금액");

        for (String keyword : keywords) {
            int idx = text.indexOf(keyword);
            if (idx != -1) {
                String raw = extractAfterKeyword(text, keyword);  // 기존 함수 재사용
                return extractNumberOnly(raw);                    // 숫자만 추출
            }
        }
        return null;
    }

    // 텍스트에서 특정 키워드 뒤에 오는 정보를 추출
    public static String extractAfterKeyword(String text, String keyword) {
        if (text == null) return null;

        int idx = text.indexOf(keyword);
        if (idx == -1) return null;

        int start = idx + keyword.length();
        String after = text.substring(start).trim();

        // 공백, 쉼표, 줄바꿈 등을 기준으로 끊기
        int end = after.indexOf(" ");
        if (end == -1) end = after.length();

        return after.substring(0, end);
    }

    // 금액 부분 숫자만 남김
    public static String extractNumberOnly(String text) {
        if (text == null) return null;
        // 숫자와 쉼표만 남기고 모두 제거
        String cleaned = text.replaceAll("[^0-9,]", "");
        // 쉼표 제거
        cleaned = cleaned.replaceAll(",", "");
        return cleaned;
    }

    // 동·호·층 추출
    private static final Pattern DETAIL_PATTERN = Pattern.compile("(?:(\\d+)\\s*동)?\\s*[- ]?\\s*(\\d+)\\s*호?");

    public static Map<String, String> parseDetailWithFloor(String detail) {
        Matcher matcher = DETAIL_PATTERN.matcher(detail.trim());
        Map<String, String> result = new HashMap<>();

        if (matcher.find()) {
            String dong = matcher.group(1) != null ? matcher.group(1) : "";
            String ho = matcher.group(2);

            result.put("동", dong);
            result.put("호", ho);
            result.put("층", extractFloor(ho));
        } else {
            result.put("동", "");
            result.put("호", detail.trim());
            result.put("층", extractFloor(detail));
        }
        return result;
    }

    // 호에서 층 추출
    public static String extractFloor(String ho) {
        String digits = ho.replaceAll("\\D", "");
        String floorNum = "";
        if (digits.length() >= 3) {
            floorNum = digits.substring(0, digits.length() - 2);
        } else if (digits.length() == 2) {
            floorNum = digits.substring(0, 1);
        }
        return floorNum.isEmpty() ? "" : floorNum + "층";
    }

    // 면적 정보 가져오기
    public static String getArea(List<List<String>> tableData, String detail) {
        // detail에서 층 정보 추출
        Map<String, String> detailInfo = parseDetailWithFloor(detail);
        String floor = detailInfo.get("층");
//        System.out.println("층 정보: " + floor);

        for (List<String> row : tableData) {
            if (row.size() < 4) continue;
            String buildingNumber = normalizeText(row.get(2));
            String buildingDetails = normalizeText(row.get(3));

            // 건물번호에 층 정보(제1층 or 1층)가 포함되어 있는지 확인
            if (buildingNumber.contains(floor) || buildingNumber.contains("제" + floor)) {
                // 면적 추출
                Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)m2?");
                Matcher matcher = pattern.matcher(buildingDetails);
                if (matcher.find()) {
                    return matcher.group(1); // 면적 값만 반환
                }
            }
        }
        return null;
    }
}
