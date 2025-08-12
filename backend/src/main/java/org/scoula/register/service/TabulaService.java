package org.scoula.register.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.scoula.register.domain.RegistryRating;
import org.scoula.register.domain.dto.RegisterAnalysisResponse;
import org.scoula.register.domain.dto.RegisterDTO;
import org.scoula.register.mapper.RegisterMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class TabulaService {
    private final RegisterMapper registerMapper;
    private final ObjectMapper objectMapper;
    // 등기부등본 내 표 추출 메서드
    public List<List<String>> extractTable(InputStream inputStream) throws Exception {
        List<List<String>> extractedData = new ArrayList<>();

        try (PDDocument document = PDDocument.load(inputStream)) {
            ObjectExtractor extractor = new ObjectExtractor(document);
            SpreadsheetExtractionAlgorithm sea = new SpreadsheetExtractionAlgorithm();
            PageIterator pi = extractor.extract();

            while (pi.hasNext()) {
                Page page = pi.next();
                List<Table> tables = sea.extract(page);

                for (Table table : tables) {
                    List<List<RectangularTextContainer>> rows = table.getRows();

                    for (List<RectangularTextContainer> cells : rows) {
                        List<String> rowData = new ArrayList<>();
                        for (RectangularTextContainer cell : cells) {
                            rowData.add(cell.getText().replace("\r", " ").trim());
                        }
                        extractedData.add(rowData);
                    }
                }
            }
        }

        return extractedData;
    }

    // 등기부등본 테이블에 저장
    public int saveAnalysis(int userId, String address, RegisterAnalysisResponse response, String registryName, RegistryRating registryRating, String fileName, long totalPriorAmount) throws Exception {
        String risks = objectMapper.writeValueAsString(response);

        RegisterDTO dto = new RegisterDTO();
        dto.setUserId(userId);
        dto.setAddress(address);
        dto.setRisks(risks);
        dto.setRegistryName(registryName);
        dto.setRegistryRating(registryRating);
        dto.setFileName(fileName);
        dto.setTotalPriorAmount(totalPriorAmount);

        registerMapper.insertRegister(dto);

        return dto.getRegistryId();
    }

    public RegisterDTO findByRegisterId(Integer registerId) {
        return registerMapper.selectByRegisterId(registerId);
    }

    // 문자열로 저장된 json Java 객체로 역직렬화
    public RegisterAnalysisResponse convertDTOToResponse(RegisterDTO dto) throws JsonProcessingException {
        return objectMapper.readValue(dto.getRisks(), RegisterAnalysisResponse.class);
    }

    // 유저 번호로 분석된 등기부 목록 찾기(상태 true인 목록만)
    public List<RegisterDTO> findByUserId(Integer userId) {
        return registerMapper.selectByUserId(userId);
    }
    
    // 50일 이상 지난 등기부 분석 기록 비활성화
    @Scheduled(cron = "0 0 3 * * *")    // 매일 새벽 3시에 실행
    public void disableOldRegistryStatus() {
        registerMapper.updateOldRegistryStatus();
    }
}
