package org.scoula.checklist.service;

import org.scoula.checklist.domain.dto.ChecklistDTO;
import org.scoula.checklist.mapper.ChecklistMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChecklistServiceImpl implements ChecklistService {

    private final ChecklistMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public ChecklistServiceImpl(ChecklistMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void saveChecklist(ChecklistDTO dto) {
        try {
            String checkedJson = objectMapper.writeValueAsString(dto.getChecked());
            String checklistRating = calculateRating(dto.getChecked());
            mapper.insertChecklist(dto.getRegistryId(), dto.getUserId(), checkedJson, checklistRating);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("체크리스트 저장 실패", e);
        }
    }

    private String calculateRating(List<Boolean> checkedList) {
        // 위험 판정 항목 인덱스 (0-based)
        int[] dangerIndices = {0, 3, 4, 7, 8};
        int[] cautionIndices = {6, 1};
        int[] normalIndices = {2, 5};

        // 위험 체크
        for (int idx : dangerIndices) {
            if (!checkedList.get(idx)) {
                return "위험";
            }
        }

        // 주의 체크
        for (int idx : cautionIndices) {
            if (!checkedList.get(idx)) {
                return "주의";
            }
        }

        // 보통 체크
        for (int idx : normalIndices) {
            if (!checkedList.get(idx)) {
                return "보통";
            }
        }

        // 모두 true일 경우
        return "안전";
    }


}
