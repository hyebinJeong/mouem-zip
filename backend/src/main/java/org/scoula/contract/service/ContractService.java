package org.scoula.contract.service;

import lombok.RequiredArgsConstructor;
import org.scoula.contract.domain.ContractDTO;
import org.scoula.contract.mapper.ContractMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractMapper contractMapper;

    @Transactional
    public void saveContract(ContractDTO contractDTO) {
        // ✅ 계약서 + 특약 JSON 한 번에 저장
        contractMapper.insertContract(contractDTO);
    }

    public List<ContractDTO> getContractListByUserId(int userId) {
        return contractMapper.selectContractsByUserId(userId);
    }

    public ContractDTO getContractById(int contractId) {
        return contractMapper.selectContractById(contractId);
    }

}
